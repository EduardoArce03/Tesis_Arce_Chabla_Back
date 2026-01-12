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
                imagenUrl = "https://www.surdoc.cl/sites/default/files/record_images/02619.00.jpg",
                categoria = CategoriasCultural.VESTIMENTA,
                descripcion = "Faja tejida tradicionalmente usada en la vestimenta andina"
            ),
            ElementoCultural(
                nombreKichwa = "Kushma",
                nombreEspanol = "Túnica",
                imagenUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQkCMBUSenosSvbmY_Nvlm9cKlyFe_C8Ai57A&s",
                categoria = CategoriasCultural.VESTIMENTA,
                descripcion = "Túnica tradicional de los pueblos andinos"
            ),
            ElementoCultural(
                nombreKichwa = "Lliclla",
                nombreEspanol = "Manta",
                imagenUrl = "https://img.freepik.com/fotos-premium/coloridas-telas-peruanas-manta-tradicional-lliclla_136753-1303.jpg",
                categoria = CategoriasCultural.VESTIMENTA,
                descripcion = "Manta rectangular usada por mujeres andinas"
            ),
            ElementoCultural(
                nombreKichwa = "Chuspa",
                nombreEspanol = "Bolsa",
                imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/b/b2/Bag_for_Carrying_Coca_Leaves%2C_Chuspas%2C_20th_Century%2C_30.1165.22.jpg",
                categoria = CategoriasCultural.VESTIMENTA,
                descripcion = "Pequeña bolsa tejida para guardar hojas de coca"
            ),
            ElementoCultural(
                nombreKichwa = "Anaku",
                nombreEspanol = "Vestido",
                imagenUrl = "https://kinku.com.ec/1571-large_default/anaco-campana-negro.jpg",
                categoria = CategoriasCultural.VESTIMENTA,
                descripcion = "Vestido tradicional de las mujeres andinas"
            ),
            ElementoCultural(
                nombreKichwa = "Unkuna",
                nombreEspanol = "Pañuelo",
                imagenUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTNQV3bLDRi77J9Ls-JeC74mwaDd_SRWEBfEw&s",
                categoria = CategoriasCultural.VESTIMENTA,
                descripcion = "Pañuelo multiuso usado en ceremonias"
            ),
            ElementoCultural(
                nombreKichwa = "Chullo",
                nombreEspanol = "Gorro",
                imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/db/SKOUFI.JPG/330px-SKOUFI.JPG",
                categoria = CategoriasCultural.VESTIMENTA,
                descripcion = "Gorro tejido con orejeras típico de los Andes"
            ),
            ElementoCultural(
                nombreKichwa = "Usuta",
                nombreEspanol = "Sandalias",
                imagenUrl = "https://plazavea.vteximg.com.br/arquivos/ids/22575729-1000-1000/imageUrl_2.jpg?v=638053540538270000",
                categoria = CategoriasCultural.VESTIMENTA,
                descripcion = "Sandalias tradicionales andinas"
            ),
            ElementoCultural(
                nombreKichwa = "Punchukuna",
                nombreEspanol = "Poncho",
                imagenUrl = "https://alpacaoficial.com/cdn/shop/files/Poncho-Cayambe_1080x.png?v=1694479988",
                categoria = CategoriasCultural.VESTIMENTA,
                descripcion = "Prenda exterior de abrigo"
            ),
            ElementoCultural(
                nombreKichwa = "Sombrero",
                nombreEspanol = "Sombrero",
                imagenUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRHVjz_A9wElUvHA7B3xCBA01WTb6PfEpqF5w&s",
                categoria = CategoriasCultural.VESTIMENTA,
                descripcion = "Sombrero tradicional andino"
            ),
            ElementoCultural(
                nombreKichwa = "Pacha",
                nombreEspanol = "Bayeta",
                imagenUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQrj4noWKh9Y8a-nw5V6vvD3MScGAL3bY04vg&s",
                categoria = CategoriasCultural.VESTIMENTA,
                descripcion = "Tela tradicional andina"
            ),
            ElementoCultural(
                nombreKichwa = "Makiwatana",
                nombreEspanol = "Pulsera",
                imagenUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTA14w6fmojiUJXavyOYUaUopEIbBWTNGyAcA&s",
                categoria = CategoriasCultural.VESTIMENTA,
                descripcion = "Adorno de mano tradicional"
            ),

            // MÚSICA - 12 elementos
            ElementoCultural(
                nombreKichwa = "Pinkuyllu",
                nombreEspanol = "Pingullo",
                imagenUrl = "https://images.unsplash.com/photo-1511379938547-c1f69419868d?w=400",
                categoria = CategoriasCultural.MUSICA,
                descripcion = "Instrumento de viento tradicional andino"
            ),
            ElementoCultural(
                nombreKichwa = "Wankara",
                nombreEspanol = "Tambor",
                imagenUrl = "https://images.unsplash.com/photo-1519892300165-cb5542fb47c7?w=400",
                categoria = CategoriasCultural.MUSICA,
                descripcion = "Tambor ceremonial usado en festividades"
            ),
            ElementoCultural(
                nombreKichwa = "Quipa",
                nombreEspanol = "Quena",
                imagenUrl = "https://images.unsplash.com/photo-1510915228340-29c85a43dcfe?w=400",
                categoria = CategoriasCultural.MUSICA,
                descripcion = "Flauta andina hecha de caña o madera"
            ),
            ElementoCultural(
                nombreKichwa = "Antara",
                nombreEspanol = "Zampoña",
                imagenUrl = "https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?w=400",
                categoria = CategoriasCultural.MUSICA,
                descripcion = "Instrumento de viento tipo flauta de pan"
            ),
            ElementoCultural(
                nombreKichwa = "Charana",
                nombreEspanol = "Charango",
                imagenUrl = "https://images.unsplash.com/photo-1511379938547-c1f69419868d?w=400",
                categoria = CategoriasCultural.MUSICA,
                descripcion = "Instrumento de cuerda similar a una guitarra pequeña"
            ),
            ElementoCultural(
                nombreKichwa = "Pututo",
                nombreEspanol = "Caracol",
                imagenUrl = "https://images.unsplash.com/photo-1487180144351-b8472da7d491?w=400",
                categoria = CategoriasCultural.MUSICA,
                descripcion = "Instrumento de viento hecho de caracol marino"
            ),
            ElementoCultural(
                nombreKichwa = "Tinya",
                nombreEspanol = "Tamborcillo",
                imagenUrl = "https://images.unsplash.com/photo-1528443503949-36bb18c1f4c6?w=400",
                categoria = CategoriasCultural.MUSICA,
                descripcion = "Pequeño tambor ceremonial"
            ),
            ElementoCultural(
                nombreKichwa = "Runa Taki",
                nombreEspanol = "Canto del Pueblo",
                imagenUrl = "https://images.unsplash.com/photo-1516280440614-37939bbacd81?w=400",
                categoria = CategoriasCultural.MUSICA,
                descripcion = "Música tradicional vocal andina"
            ),
            ElementoCultural(
                nombreKichwa = "Wankar",
                nombreEspanol = "Tambor Grande",
                imagenUrl = "https://images.unsplash.com/photo-1519892300165-cb5542fb47c7?w=400",
                categoria = CategoriasCultural.MUSICA,
                descripcion = "Tambor de gran tamaño"
            ),
            ElementoCultural(
                nombreKichwa = "Runa Tinya",
                nombreEspanol = "Caja",
                imagenUrl = "https://images.unsplash.com/photo-1460667262436-cf19894f4774?w=400",
                categoria = CategoriasCultural.MUSICA,
                descripcion = "Instrumento de percusión"
            ),
            ElementoCultural(
                nombreKichwa = "Kena",
                nombreEspanol = "Quena Grande",
                imagenUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400",
                categoria = CategoriasCultural.MUSICA,
                descripcion = "Flauta de mayor tamaño"
            ),
            ElementoCultural(
                nombreKichwa = "Charango",
                nombreEspanol = "Charango Andino",
                imagenUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400",
                categoria = CategoriasCultural.MUSICA,
                descripcion = "Instrumento de cuerdas típico"
            ),

            // LUGARES - 12 elementos
            ElementoCultural(
                nombreKichwa = "Ingapirka",
                nombreEspanol = "Muro del Inca",
                imagenUrl = "https://images.unsplash.com/photo-1587974928442-77dc3e0dba72?w=400",
                categoria = CategoriasCultural.LUGARES,
                descripcion = "Importante sitio arqueológico Cañari-Inca"
            ),
            ElementoCultural(
                nombreKichwa = "Hatun Rumi",
                nombreEspanol = "Piedra Grande",
                imagenUrl = "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400",
                categoria = CategoriasCultural.LUGARES,
                descripcion = "Lugar sagrado de grandes piedras ceremoniales"
            ),
            ElementoCultural(
                nombreKichwa = "Yacu Mama",
                nombreEspanol = "Madre Agua",
                imagenUrl = "https://images.unsplash.com/photo-1439066615861-d1af74d74000?w=400",
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
                imagenUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=400",
                categoria = CategoriasCultural.LUGARES,
                descripcion = "Posada o refugio del camino inca"
            ),
            ElementoCultural(
                nombreKichwa = "Kulunchu",
                nombreEspanol = "Adoratorio",
                imagenUrl = "https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=400",
                categoria = CategoriasCultural.LUGARES,
                descripcion = "Lugar de adoración y ceremonias"
            ),
            ElementoCultural(
                nombreKichwa = "Chakana Ñan",
                nombreEspanol = "Camino de la Cruz del Sur",
                imagenUrl = "https://images.unsplash.com/photo-1472214103451-9374bd1c798e?w=400",
                categoria = CategoriasCultural.LUGARES,
                descripcion = "Caminos ceremoniales alineados astronómicamente"
            ),
            ElementoCultural(
                nombreKichwa = "Ushnu",
                nombreEspanol = "Plataforma Ceremonial",
                imagenUrl = "https://images.unsplash.com/photo-1523712999610-f77fbcfc3843?w=400",
                categoria = CategoriasCultural.LUGARES,
                descripcion = "Plataforma ceremonial inca"
            ),
            ElementoCultural(
                nombreKichwa = "Urku",
                nombreEspanol = "Montaña",
                imagenUrl = "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400",
                categoria = CategoriasCultural.LUGARES,
                descripcion = "Montaña sagrada"
            ),
            ElementoCultural(
                nombreKichwa = "Yaku",
                nombreEspanol = "Laguna",
                imagenUrl = "https://images.unsplash.com/photo-1464207687429-7505649dae38?w=400",
                categoria = CategoriasCultural.LUGARES,
                descripcion = "Cuerpo de agua sagrado"
            ),
            ElementoCultural(
                nombreKichwa = "Pukara",
                nombreEspanol = "Fortaleza",
                imagenUrl = "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=400",
                categoria = CategoriasCultural.LUGARES,
                descripcion = "Construcción defensiva"
            ),
            ElementoCultural(
                nombreKichwa = "Chakra",
                nombreEspanol = "Terraza Agrícola",
                imagenUrl = "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?w=400",
                categoria = CategoriasCultural.LUGARES,
                descripcion = "Sistema de agricultura en terrazas"
            ),

            // FESTIVIDADES - 12 elementos
            ElementoCultural(
                nombreKichwa = "Inti Raymi",
                nombreEspanol = "Fiesta del Sol",
                imagenUrl = "https://images.unsplash.com/photo-1533174072545-7a4b6ad7a6c3?w=400",
                categoria = CategoriasCultural.FESTIVIDADES,
                descripcion = "Celebración del solsticio de invierno"
            ),
            ElementoCultural(
                nombreKichwa = "Pawkar Raymi",
                nombreEspanol = "Fiesta del Florecimiento",
                imagenUrl = "https://images.unsplash.com/photo-1490750967868-88aa4486c946?w=400",
                categoria = CategoriasCultural.FESTIVIDADES,
                descripcion = "Celebración del equinoccio de primavera"
            ),
            ElementoCultural(
                nombreKichwa = "Killa Raymi",
                nombreEspanol = "Fiesta de la Luna",
                imagenUrl = "https://images.unsplash.com/photo-1509023464722-18d996393ca8?w=400",
                categoria = CategoriasCultural.FESTIVIDADES,
                descripcion = "Celebración dedicada a la luna y la feminidad"
            ),
            ElementoCultural(
                nombreKichwa = "Kapak Raymi",
                nombreEspanol = "Fiesta del Señor",
                imagenUrl = "https://images.unsplash.com/photo-1527891751199-7225231a68dd?w=400",
                categoria = CategoriasCultural.FESTIVIDADES,
                descripcion = "Celebración del solsticio de verano"
            ),
            ElementoCultural(
                nombreKichwa = "Aymuray",
                nombreEspanol = "Fiesta de la Cosecha",
                imagenUrl = "https://images.unsplash.com/photo-1574943320219-553eb213f72d?w=400",
                categoria = CategoriasCultural.FESTIVIDADES,
                descripcion = "Celebración de agradecimiento por la cosecha"
            ),
            ElementoCultural(
                nombreKichwa = "Situa",
                nombreEspanol = "Fiesta de Purificación",
                imagenUrl = "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400",
                categoria = CategoriasCultural.FESTIVIDADES,
                descripcion = "Ceremonia de purificación y renovación"
            ),
            ElementoCultural(
                nombreKichwa = "Hatun Puncha",
                nombreEspanol = "Día Grande",
                imagenUrl = "https://images.unsplash.com/photo-1533174072545-7a4b6ad7a6c3?w=400",
                categoria = CategoriasCultural.FESTIVIDADES,
                descripcion = "Celebración de eventos importantes comunitarios"
            ),
            ElementoCultural(
                nombreKichwa = "Mushuk Nina",
                nombreEspanol = "Fuego Nuevo",
                imagenUrl = "https://images.unsplash.com/photo-1516426122078-c23e76319801?w=400",
                categoria = CategoriasCultural.FESTIVIDADES,
                descripcion = "Ceremonia de renovación del fuego sagrado"
            ),
            ElementoCultural(
                nombreKichwa = "Pawkar Raymi",
                nombreEspanol = "Taita Carnaval",
                imagenUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=400",
                categoria = CategoriasCultural.FESTIVIDADES,
                descripcion = "Celebración del carnaval andino"
            ),
            ElementoCultural(
                nombreKichwa = "Kuya Raymi",
                nombreEspanol = "Fiesta de Purificación",
                imagenUrl = "https://images.unsplash.com/photo-1478145046317-39f10e56b5e9?w=400",
                categoria = CategoriasCultural.FESTIVIDADES,
                descripcion = "Ceremonia de limpieza espiritual"
            ),
            ElementoCultural(
                nombreKichwa = "Mushuk Nina",
                nombreEspanol = "Fuego Nuevo",
                imagenUrl = "https://images.unsplash.com/photo-1464047736614-af63643285bf?w=400",
                categoria = CategoriasCultural.FESTIVIDADES,
                descripcion = "Renovación del fuego sagrado"
            ),
            ElementoCultural(
                nombreKichwa = "Kapak Inti Raymi",
                nombreEspanol = "Gran Fiesta del Sol",
                imagenUrl = "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=400",
                categoria = CategoriasCultural.FESTIVIDADES,
                descripcion = "Celebración principal del sol"
            )
        )

        elementoCulturalRepository.saveAll(elementos)
        println("✅ ${elementos.size} elementos culturales cargados en la base de datos")
    }
}