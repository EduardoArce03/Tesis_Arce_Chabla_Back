package com.tesis.gamificacion.config

import com.tesis.gamificacion.model.entities.ElementoCultural
import com.tesis.gamificacion.model.enums.CategoriasCultural
import com.tesis.gamificacion.repository.ElementoCulturalRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class DataLoader(
    private val elementoCulturalRepository: ElementoCulturalRepository
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (elementoCulturalRepository.count() == 0L) {
            cargarElementosCulturales()
        }
    }

    private fun cargarElementosCulturales() {
        val elementos = listOf(
            // VESTIMENTA - 12 elementos
            ElementoCultural(
                nombreKichwa = "Chumpi",
                nombreEspanol = "Faja",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/chumpi.jpg",
                categoria = CategoriasCultural.VESTIMENTA,
                descripcion = "Faja tejida tradicionalmente usada en la vestimenta andina"
            ),
            ElementoCultural(
                nombreKichwa = "Kushma",
                nombreEspanol = "Túnica",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/kushma.jpg",
                categoria = CategoriasCultural.VESTIMENTA,
                descripcion = "Túnica tradicional de los pueblos andinos"
            ),
            ElementoCultural(
                nombreKichwa = "Lliclla",
                nombreEspanol = "Manta",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/lliclla.jpeg",
                categoria = CategoriasCultural.VESTIMENTA,
                descripcion = "Manta rectangular usada por mujeres andinas"
            ),
            ElementoCultural(
                nombreKichwa = "Chuspa",
                nombreEspanol = "Bolsa",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/chuspa.jpg",
                categoria = CategoriasCultural.VESTIMENTA,
                descripcion = "Pequeña bolsa tejida para guardar hojas de coca"
            ),
            ElementoCultural(
                nombreKichwa = "Anaku",
                nombreEspanol = "Vestido",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/anaku.jpeg",
                categoria = CategoriasCultural.VESTIMENTA,
                descripcion = "Vestido tradicional de las mujeres andinas"
            ),
            ElementoCultural(
                nombreKichwa = "Unkuna",
                nombreEspanol = "Pañuelo",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/unkuna.jpg",
                categoria = CategoriasCultural.VESTIMENTA,
                descripcion = "Pañuelo multiuso usado en ceremonias"
            ),
            ElementoCultural(
                nombreKichwa = "Chullo",
                nombreEspanol = "Gorro",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/chullo.jpg",
                categoria = CategoriasCultural.VESTIMENTA,
                descripcion = "Gorro tejido con orejeras típico de los Andes"
            ),
            ElementoCultural(
                nombreKichwa = "Usuta",
                nombreEspanol = "Sandalias",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/usuta.jpeg",
                categoria = CategoriasCultural.VESTIMENTA,
                descripcion = "Sandalias tradicionales andinas"
            ),
            ElementoCultural(
                nombreKichwa = "Punchukuna",
                nombreEspanol = "Poncho",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/Punchukuna.jpeg",
                categoria = CategoriasCultural.VESTIMENTA,
                descripcion = "Prenda exterior de abrigo"
            ),
            ElementoCultural(
                nombreKichwa = "Sombrero",
                nombreEspanol = "Sombrero",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/sombrero.jpeg",
                categoria = CategoriasCultural.VESTIMENTA,
                descripcion = "Sombrero tradicional andino"
            ),
            ElementoCultural(
                nombreKichwa = "Pacha",
                nombreEspanol = "Bayeta",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/Pacha.jpeg",
                categoria = CategoriasCultural.VESTIMENTA,
                descripcion = "Tela tradicional andina"
            ),
            ElementoCultural(
                nombreKichwa = "Makiwatana",
                nombreEspanol = "Pulsera",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/Makiwatana.jpeg",
                categoria = CategoriasCultural.VESTIMENTA,
                descripcion = "Adorno de mano tradicional"
            ),

            // MÚSICA - 12 elementos
            ElementoCultural(
                nombreKichwa = "Pinkuyllu",
                nombreEspanol = "Pingullo",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/pinkuyllu.jpg",
                categoria = CategoriasCultural.MUSICA,
                descripcion = "Instrumento de viento tradicional andino"
            ),
            ElementoCultural(
                nombreKichwa = "Wankara",
                nombreEspanol = "Tambor",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/wankara.jpg",
                categoria = CategoriasCultural.MUSICA,
                descripcion = "Tambor ceremonial usado en festividades"
            ),
            ElementoCultural(
                nombreKichwa = "Quipa",
                nombreEspanol = "Quena",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/Quipa.jpg",
                categoria = CategoriasCultural.MUSICA,
                descripcion = "Flauta andina hecha de caña o madera"
            ),
            ElementoCultural(
                nombreKichwa = "Antara",
                nombreEspanol = "Zampoña",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/Antara.jpg",
                categoria = CategoriasCultural.MUSICA,
                descripcion = "Instrumento de viento tipo flauta de pan"
            ),
            ElementoCultural(
                nombreKichwa = "Charana",
                nombreEspanol = "Charango",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/charango.jpg",
                categoria = CategoriasCultural.MUSICA,
                descripcion = "Instrumento de cuerda similar a una guitarra pequeña"
            ),
            ElementoCultural(
                nombreKichwa = "Pututo",
                nombreEspanol = "Caracol",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/Pututu.jpg",
                categoria = CategoriasCultural.MUSICA,
                descripcion = "Instrumento de viento hecho de caracol marino"
            ),
            ElementoCultural(
                nombreKichwa = "Tinya",
                nombreEspanol = "Tamborcillo",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/tinya.jpg",
                categoria = CategoriasCultural.MUSICA,
                descripcion = "Pequeño tambor ceremonial"
            ),
            ElementoCultural(
                nombreKichwa = "Runa Taki",
                nombreEspanol = "Canto del Pueblo",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/runataki.jpg",
                categoria = CategoriasCultural.MUSICA,
                descripcion = "Música tradicional vocal andina"
            ),
            ElementoCultural(
                nombreKichwa = "Wankar",
                nombreEspanol = "Tambor Grande",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/wankara.jpg",
                categoria = CategoriasCultural.MUSICA,
                descripcion = "Tambor de gran tamaño"
            ),
            ElementoCultural(
                nombreKichwa = "Runa Tinya",
                nombreEspanol = "Caja",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/tinya.jpeg",
                categoria = CategoriasCultural.MUSICA,
                descripcion = "Instrumento de percusión"
            ),
            ElementoCultural(
                nombreKichwa = "Kena",
                nombreEspanol = "Quena Grande",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/kena.jpeg",
                categoria = CategoriasCultural.MUSICA,
                descripcion = "Flauta de mayor tamaño"
            ),
            ElementoCultural(
                nombreKichwa = "Charango",
                nombreEspanol = "Charango Andino",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/charango.png",
                categoria = CategoriasCultural.MUSICA,
                descripcion = "Instrumento de cuerdas típico"
            ),

            // LUGARES - 12 elementos
            ElementoCultural(
                nombreKichwa = "Ingapirka",
                nombreEspanol = "Muro del Inca",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/ingapirca.jpg",
                categoria = CategoriasCultural.LUGARES,
                descripcion = "Importante sitio arqueológico Cañari-Inca"
            ),
            ElementoCultural(
                nombreKichwa = "Hatun Rumi",
                nombreEspanol = "Piedra Grande",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/hatunrumi.jpg",
                categoria = CategoriasCultural.LUGARES,
                descripcion = "Lugar sagrado de grandes piedras ceremoniales"
            ),
            ElementoCultural(
                nombreKichwa = "Yacu Mama",
                nombreEspanol = "Madre Agua",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/yacumama.jpg",
                categoria = CategoriasCultural.LUGARES,
                descripcion = "Lagunas sagradas de los Andes"
            ),
            ElementoCultural(
                nombreKichwa = "Apu Urku",
                nombreEspanol = "Montaña Sagrada",
                imagenUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=400",
                categoria = CategoriasCultural.LUGARES,
                descripcion = "Montañas consideradas divinidades protectoras"
            ),
            ElementoCultural(
                nombreKichwa = "Tampu",
                nombreEspanol = "Tambo",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/apurku.jpeg",
                categoria = CategoriasCultural.LUGARES,
                descripcion = "Posada o refugio del camino inca"
            ),
            ElementoCultural(
                nombreKichwa = "Kulunchu",
                nombreEspanol = "Adoratorio",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/kulunchu.jpg",
                categoria = CategoriasCultural.LUGARES,
                descripcion = "Lugar de adoración y ceremonias"
            ),
            ElementoCultural(
                nombreKichwa = "Chakana Ñan",
                nombreEspanol = "Camino de la Cruz del Sur",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/chakanañan.jpeg",
                categoria = CategoriasCultural.LUGARES,
                descripcion = "Caminos ceremoniales alineados astronómicamente"
            ),
            ElementoCultural(
                nombreKichwa = "Ushnu",
                nombreEspanol = "Plataforma Ceremonial",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/ushnu.jpg",
                categoria = CategoriasCultural.LUGARES,
                descripcion = "Plataforma ceremonial inca"
            ),
            ElementoCultural(
                nombreKichwa = "Urku",
                nombreEspanol = "Montaña",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/urku.jpg",
                categoria = CategoriasCultural.LUGARES,
                descripcion = "Montaña sagrada"
            ),
            ElementoCultural(
                nombreKichwa = "Yaku",
                nombreEspanol = "Laguna",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/yaku.jpg",
                categoria = CategoriasCultural.LUGARES,
                descripcion = "Cuerpo de agua sagrado"
            ),
            ElementoCultural(
                nombreKichwa = "Pukara",
                nombreEspanol = "Fortaleza",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/pukara.jpeg",
                categoria = CategoriasCultural.LUGARES,
                descripcion = "Construcción defensiva"
            ),
            ElementoCultural(
                nombreKichwa = "Chakra",
                nombreEspanol = "Terraza Agrícola",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/chakra.jpg",
                categoria = CategoriasCultural.LUGARES,
                descripcion = "Sistema de agricultura en terrazas"
            ),

            // FESTIVIDADES - 12 elementos
            ElementoCultural(
                nombreKichwa = "Inti Raymi",
                nombreEspanol = "Fiesta del Sol",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/inti.jpg",
                categoria = CategoriasCultural.FESTIVIDADES,
                descripcion = "Celebración del solsticio de invierno"
            ),
            ElementoCultural(
                nombreKichwa = "Pawkar Raymi",
                nombreEspanol = "Fiesta del Florecimiento",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/pawkar.jpeg",
                categoria = CategoriasCultural.FESTIVIDADES,
                descripcion = "Celebración del equinoccio de primavera"
            ),
            ElementoCultural(
                nombreKichwa = "Killa Raymi",
                nombreEspanol = "Fiesta de la Luna",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/killa.jpeg",
                categoria = CategoriasCultural.FESTIVIDADES,
                descripcion = "Celebración dedicada a la luna y la feminidad"
            ),
            ElementoCultural(
                nombreKichwa = "Kapak Raymi",
                nombreEspanol = "Fiesta del Señor",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/kapakRaymi.jpeg",
                categoria = CategoriasCultural.FESTIVIDADES,
                descripcion = "Celebración del solsticio de verano"
            ),
            ElementoCultural(
                nombreKichwa = "Aymuray",
                nombreEspanol = "Fiesta de la Cosecha",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/aymuray.jpg",
                categoria = CategoriasCultural.FESTIVIDADES,
                descripcion = "Celebración de agradecimiento por la cosecha"
            ),
            ElementoCultural(
                nombreKichwa = "Situa",
                nombreEspanol = "Fiesta de Purificación",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/situa.jpeg",
                categoria = CategoriasCultural.FESTIVIDADES,
                descripcion = "Ceremonia de purificación y renovación"
            ),
            ElementoCultural(
                nombreKichwa = "Hatun Puncha",
                nombreEspanol = "Día Grande",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/hatun.jpg",
                categoria = CategoriasCultural.FESTIVIDADES,
                descripcion = "Celebración de eventos importantes comunitarios"
            ),
            ElementoCultural(
                nombreKichwa = "Mushuk Nina",
                nombreEspanol = "Fuego Nuevo",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/mushuc-nina.jpg",
                categoria = CategoriasCultural.FESTIVIDADES,
                descripcion = "Ceremonia de renovación del fuego sagrado"
            ),
            ElementoCultural(
                nombreKichwa = "Pawkar Raymi",
                nombreEspanol = "Taita Carnaval",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/Pawcar.jpeg",
                categoria = CategoriasCultural.FESTIVIDADES,
                descripcion = "Celebración del carnaval andino"
            ),
            ElementoCultural(
                nombreKichwa = "Kuya Raymi",
                nombreEspanol = "Fiesta de Purificación",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/kuya.jpeg",
                categoria = CategoriasCultural.FESTIVIDADES,
                descripcion = "Ceremonia de limpieza espiritual"
            ),
            ElementoCultural(
                nombreKichwa = "Mushuk Nina",
                nombreEspanol = "Fuego Nuevo",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/mushuc-nina.jpg",
                categoria = CategoriasCultural.FESTIVIDADES,
                descripcion = "Renovación del fuego sagrado"
            ),
            ElementoCultural(
                nombreKichwa = "Kapak Inti Raymi",
                nombreEspanol = "Gran Fiesta del Sol",
                imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/kapak.jpeg",
                categoria = CategoriasCultural.FESTIVIDADES,
                descripcion = "Celebración principal del sol"
            )
        )

        elementoCulturalRepository.saveAll(elementos)
        println("✅ ${elementos.size} elementos culturales cargados en la base de datos")
    }
}