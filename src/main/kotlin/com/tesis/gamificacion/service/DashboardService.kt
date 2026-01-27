package com.tesis.gamificacion.service

import com.tesis.gamificacion.dto.DashboardResponse
import com.tesis.gamificacion.dto.EstadisticasResumen
import com.tesis.gamificacion.dto.JuegoDisponible
import com.tesis.gamificacion.dto.Logro
import com.tesis.gamificacion.dto.RankingItem
import com.tesis.gamificacion.dto.RankingPosicion
import com.tesis.gamificacion.dto.UsuarioInfo
import com.tesis.gamificacion.repository.PartidaRepository
import com.tesis.gamificacion.repository.UsuarioRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.floor

@Service
class DashboardService(
    private val usuarioRepository: UsuarioRepository,
    private val partidaRepository: PartidaRepository
) {
    private val logger = LoggerFactory.getLogger(DashboardService::class.java)

    @Transactional(readOnly = true)
    fun obtenerDashboard(usuarioId: Long): DashboardResponse {
        try {
            logger.info("🎯 Obteniendo dashboard para usuarioId: {}", usuarioId)

            // Buscar usuario
            val usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow {
                    logger.error("❌ Usuario no encontrado: {}", usuarioId)
                    IllegalArgumentException("Usuario no encontrado con ID: $usuarioId")
                }
            logger.info("✅ Usuario encontrado: {} ({})", usuario.nombre, usuario.codigoJugador)

            // Buscar partidas
            val jugadorIdStr = usuarioId.toString()
            logger.info("🔍 Buscando partidas con jugadorId: {}", jugadorIdStr)

            val partidas = partidaRepository.findByJugadorIdOrderByFechaInicioDesc(jugadorIdStr)
            logger.info("📊 Partidas encontradas: {}", partidas.size)

            val partidasCompletadas = partidas.filter { it.completada }
            logger.info("✅ Partidas completadas: {}", partidasCompletadas.size)

            // Calcular estadísticas
            logger.info("📈 Calculando estadísticas...")
            val puntuacionTotal = partidasCompletadas.sumOf { it.puntuacion ?: 0 }
            val tiempoTotal = partidasCompletadas.sumOf { it.tiempoSegundos ?: 0 }
            val precisionPromedio = if (partidasCompletadas.isNotEmpty()) {
                partidasCompletadas.map { calcularPrecision(it.intentos!!, it.nivel!!) }.average()
            } else 0.0

            logger.info("💯 Puntuación total: {}", puntuacionTotal)
            logger.info("⏱️ Tiempo total: {} segundos", tiempoTotal)
            logger.info("🎯 Precisión promedio: {}%", precisionPromedio)

            // Calcular nivel y experiencia
            val nivel = calcularNivel(puntuacionTotal)
            val experienciaActual = puntuacionTotal
            val experienciaParaSiguiente = calcularExperienciaParaNivel(nivel + 1)
            logger.info("⭐ Nivel calculado: {} ({}XP / {}XP)", nivel, experienciaActual, experienciaParaSiguiente)

            // Obtener ranking
            logger.info("🏆 Calculando ranking...")
            val todasLasPartidas = partidaRepository.findAll()
                .filter { it.completada }
                .groupBy { it.jugadorId }
                .mapValues { it.value.sumOf { partida -> partida.puntuacion!! } }
                .toList()
                .sortedByDescending { it.second }

            logger.info("👥 Total de jugadores en ranking: {}", todasLasPartidas.size)

            val posicionGlobal = todasLasPartidas.indexOfFirst { it.first == jugadorIdStr } + 1
            val totalJugadores = todasLasPartidas.size
            logger.info("📍 Posición global: {} de {}", posicionGlobal, totalJugadores)

            // Obtener top 3
            logger.info("🥇 Obteniendo Top 3...")
            val top3 = todasLasPartidas.take(3).mapIndexed { index, pair ->
                try {
                    logger.debug("Procesando posición {}: jugadorId={}, puntuacion={}", index + 1, pair.first, pair.second)

                    val jugadorIdLong = try {
                        pair.first.toLongOrNull()
                    } catch (e: Exception) {
                        logger.warn("⚠️ No se pudo convertir jugadorId a Long: {}", pair.first)
                        null
                    }

                    val jugador = if (jugadorIdLong != null) {
                        usuarioRepository.findById(jugadorIdLong).orElse(null)
                    } else {
                        null
                    }

                    if (jugador != null) {
                        logger.debug("✅ Jugador #{} encontrado: {}", index + 1, jugador.nombre)
                    } else {
                        logger.warn("⚠️ Jugador #{} no encontrado en BD, usando datos por defecto", index + 1)
                    }

                    RankingItem(
                        posicion = index + 1,
                        nombre = jugador?.nombre ?: "Jugador ${index + 1}",
                        codigoJugador = jugador?.codigoJugador ?: pair.first,
                        puntuacion = pair.second
                    )
                } catch (e: Exception) {
                    logger.error("❌ Error procesando ranking item {}: {}", index + 1, e.message, e)
                    RankingItem(
                        posicion = index + 1,
                        nombre = "Jugador ${index + 1}",
                        codigoJugador = "UNKNOWN",
                        puntuacion = 0
                    )
                }
            }

            // Obtener logros
            logger.info("🏅 Obteniendo logros...")
            val logros = obtenerLogros(usuarioId, partidasCompletadas.size, puntuacionTotal)
            logger.info("✨ Logros obtenidos: {}", logros.size)

            // Obtener juegos disponibles
            logger.info("🎮 Obteniendo juegos disponibles...")
            val juegos = obtenerJuegosDisponibles()
            logger.info("🎯 Juegos disponibles: {}", juegos.size)

            val response = DashboardResponse(
                usuario = UsuarioInfo(
                    nombre = usuario.nombre,
                    codigoJugador = usuario.codigoJugador,
                    nivel = nivel,
                    experiencia = experienciaActual,
                    experienciaParaSiguienteNivel = experienciaParaSiguiente
                ),
                estadisticas = EstadisticasResumen(
                    totalPartidas = partidas.size,
                    partidasCompletadas = partidasCompletadas.size,
                    puntuacionTotal = puntuacionTotal,
                    tiempoTotalMinutos = tiempoTotal / 60,
                    precisionPromedio = precisionPromedio,
                    mejorPuntuacion = partidasCompletadas.maxOfOrNull { it.puntuacion!! }
                ),
                rankingPosicion = RankingPosicion(
                    posicionGlobal = if (posicionGlobal > 0) posicionGlobal else 0,
                    totalJugadores = totalJugadores,
                    top3 = top3
                ),
                logrosRecientes = logros,
                juegosDisponibles = juegos
            )

            logger.info("✅ Dashboard generado exitosamente para usuario: {}", usuario.nombre)
            return response

        } catch (e: IllegalArgumentException) {
            logger.error("❌ Error de argumento inválido: {}", e.message)
            throw e
        } catch (e: Exception) {
            logger.error("❌ Error inesperado al obtener dashboard para usuarioId {}: {}", usuarioId, e.message, e)
            throw IllegalStateException("Error al obtener dashboard: ${e.message}", e)
        }
    }

    private fun calcularNivel(experiencia: Int): Int {
        return try {
            val nivel = floor(experiencia / 500.0).toInt() + 1
            logger.debug("Nivel calculado: {} (experiencia: {})", nivel, experiencia)
            nivel
        } catch (e: Exception) {
            logger.error("Error calculando nivel: {}", e.message)
            1
        }
    }

    private fun calcularExperienciaParaNivel(nivel: Int): Int {
        return try {
            val exp = nivel * 500
            logger.debug("Experiencia para nivel {}: {}", nivel, exp)
            exp
        } catch (e: Exception) {
            logger.error("Error calculando experiencia para nivel: {}", e.message)
            500
        }
    }

    private fun calcularPrecision(intentos: Int, nivel: com.tesis.gamificacion.model.enums.NivelDificultad): Double {
        return try {
            val intentosMinimos = when (nivel) {
                com.tesis.gamificacion.model.enums.NivelDificultad.FACIL -> 6
                com.tesis.gamificacion.model.enums.NivelDificultad.MEDIO -> 8
                com.tesis.gamificacion.model.enums.NivelDificultad.DIFICIL -> 12
            }
            val precision = (intentosMinimos.toDouble() / intentos) * 100
            logger.debug("Precisión: {}% (intentos: {}, mínimos: {})", precision, intentos, intentosMinimos)
            precision
        } catch (e: Exception) {
            logger.error("Error calculando precisión: {}", e.message)
            0.0
        }
    }

    private fun obtenerLogros(usuarioId: Long, partidasCompletadas: Int, puntuacionTotal: Int): List<Logro> {
        return try {
            logger.debug("Calculando logros para usuario {}: {} partidas, {} puntos", usuarioId, partidasCompletadas, puntuacionTotal)
            val logros = mutableListOf<Logro>()

            if (partidasCompletadas >= 1) {
                logros.add(Logro(
                    id = "primera_victoria",
                    nombre = "Primera Victoria",
                    descripcion = "Completaste tu primer juego",
                    icono = "🎯",
                    fechaObtenido = "Reciente",
                    nuevo = partidasCompletadas == 1
                ))
                logger.debug("✅ Logro: Primera Victoria")
            }

            if (partidasCompletadas >= 5) {
                logros.add(Logro(
                    id = "racha_5",
                    nombre = "Racha de 5",
                    descripcion = "Completaste 5 juegos",
                    icono = "🔥",
                    fechaObtenido = "Reciente",
                    nuevo = partidasCompletadas == 5
                ))
                logger.debug("✅ Logro: Racha de 5")
            }

            if (puntuacionTotal >= 2000) {
                logros.add(Logro(
                    id = "maestro_cultural",
                    nombre = "Maestro Cultural",
                    descripcion = "Alcanzaste 2000 puntos",
                    icono = "🌟",
                    fechaObtenido = "Reciente",
                    nuevo = puntuacionTotal >= 2000 && puntuacionTotal < 2500
                ))
                logger.debug("✅ Logro: Maestro Cultural")
            }

            logger.info("Total de logros: {}", logros.size)
            logros.take(3)
        } catch (e: Exception) {
            logger.error("Error obteniendo logros: {}", e.message)
            emptyList()
        }
    }

    private fun obtenerJuegosDisponibles(): List<JuegoDisponible> {
        return try {
            listOf(
                JuegoDisponible(
                    id = "memoria_cultural",
                    nombre = "Memoria Cultural",
                    descripcion = "Encuentra parejas de elementos culturales",
                    icono = "🎮",
                    ruta = "/juegos/memoria-cultural",
                    partidasJugadas = 0,
                    disponible = true
                ),
                JuegoDisponible(
                    id = "misiones",
                    nombre = "Misiones Educativas",
                    descripcion = "Completa desafíos culturales",
                    icono = "📚",
                    ruta = "/juegos/misiones",
                    partidasJugadas = 0,
                    disponible = false
                ),
                JuegoDisponible(
                    id = "exploracion",
                    nombre = "Exploración Ingapirca",
                    descripcion = "Explora el sitio arqueológico",
                    icono = "🗿",
                    ruta = "/juegos/exploracion",
                    partidasJugadas = 0,
                    disponible = false
                )
            )
        } catch (e: Exception) {
            logger.error("Error obteniendo juegos disponibles: {}", e.message)
            emptyList()
        }
    }
}