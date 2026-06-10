package com.example.data

enum class WasteContainer(
    val id: String,
    val nameEs: String,
    val nameEn: String,
    val colorHex: String,
    val descriptionEs: String,
    val descriptionEn: String,
    val examplesEs: List<String>,
    val examplesEn: List<String>
) {
    AMARILLO(
        "AMARILLO",
        "Contenedor Amarillo",
        "Yellow Bin",
        "#FBC02D", // Bright deep yellow
        "Envases de plástico, latas de metal y briks de cartón/aluminio.",
        "Plastic packaging, metal cans, and juice/milk cartons.",
        listOf("Botellas de plástico", "Latas de refresco", "Briks de leche", "Bolsas de plástico", "Tarrinas de yogur"),
        listOf("Plastic bottles", "Soda cans", "Milk cartons", "Plastic bags", "Yogurt cups")
    ),
    AZUL(
        "AZUL",
        "Contenedor Azul",
        "Blue Bin",
        "#1976D2", // Rich Blue
        "Envases de cartón, cajas, periódicos, revistas, folletos y papel de embalar.",
        "Cardboard packaging, boxes, newspapers, magazines, flyers, and wrapping paper.",
        listOf("Cajas de cartón", "Periódicos", "Papel de regalo", "Hueveras de cartón", "Bolsas de papel"),
        listOf("Cardboard boxes", "Newspapers", "Wrapping paper", "Cardboard egg trays", "Paper bags")
    ),
    VERDE(
        "VERDE",
        "Contenedor Verde",
        "Green Bin",
        "#388E3C", // Dark green
        "Únicamente botellas, tarros y frascos de vidrio sin los tapones.",
        "Glass bottles, jars, and vials only (remove lids and caps).",
        listOf("Botellas de vidrio", "Tarros de conservas", "Frascos de colonia", "Tarros de mermelada"),
        listOf("Glass bottles", "Preserve jars", "Perfume bottles", "Jam jars")
    ),
    MARRON(
        "MARRON",
        "Contenedor Marrón",
        "Brown Bin (Organic)",
        "#795548", // Organic brown
        "Restos de comida, posos de café, infusiones, servilletas y papel de cocina usados, tapones de corcho natural.",
        "Food scraps, coffee grounds, tea bags, used napkins, paper towels, and natural cork stoppers.",
        listOf("Peladuras de frutas", "Restos de carne o pescado", "Cáscaras de huevo", "Posos de café", "Servilletas sucias"),
        listOf("Fruit peels", "Meat or fish remnants", "Eggshells", "Coffee grounds", "Dirty napkins")
    ),
    GRIS(
        "GRIS",
        "Contenedor Gris (Resto)",
        "Grey Bin (Resto)",
        "#616161", // Sleek grey
        "Para todo aquello que no se puede reciclar pero que tampoco es biodegradable (compresas, pañales, colillas, etc.).",
        "For everything that cannot be recycled but is not biodegradable (diapers, pads, cigarette butts, etc.).",
        listOf("Pañales", "Colillas de tabaco", "Polvo de barrer", "Platos de cerámica rotos", "Chicles"),
        listOf("Diapers", "Cigarette butts", "Swept dust", "Broken ceramic plates", "Chewing gum")
    ),
    PUNTO_LIMPIO(
        "PUNTO_LIMPIO",
        "Punto Limpio / Punto SIGRE",
        "Specialized Recycling Point",
        "#00796B", // Dark teal
        "Residuos especiales que requieren tratamiento específico (pilas, aceites, bombillas, electrodomésticos, pinturas, medicamentos).",
        "Specialized or hazardous waste requiring specific treatment (batteries, bulb lamps, domestic oils, electronics, medicines).",
        listOf("Pilas y baterías", "Bombillas y fluorescentes", "Aceite de cocina usado", "Electrodomésticos", "Medicamentos caducados"),
        listOf("Batteries", "Light bulbs & flourescents", "Used cooking oil", "Home appliances", "Expired medicines")
    );

    fun getName(lang: String): String = if (lang == "en") nameEn else nameEs
    fun getDescription(lang: String): String = if (lang == "en") descriptionEn else descriptionEs
    fun getExamples(lang: String): List<String> = if (lang == "en") examplesEn else examplesEs
}

data class MadridPuntoLimpio(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val scheduleEs: String = "Lunes a Sábado: 8:00h - 20:00h. Domingos y Festivos: 9:00h - 14:00h.",
    val scheduleEn: String = "Monday to Saturday: 8:00 AM - 8:00 PM. Sundays and Holidays: 9:00 AM - 2:00 PM.",
    val phone: String = "010 Ayuntamiento de Madrid"
)

object MadridData {
    val puntosLimpios = listOf(
        MadridPuntoLimpio(
            "Punto Limpio Fijo - Retiro",
            "Calle de la Estrella Polar, s/n, 28007 Madrid",
            40.408103,
            -3.665421
        ),
        MadridPuntoLimpio(
            "Punto Limpio Fijo - Chamberí",
            "Calle de Joaquín María López, s/n (esq. Isaac Peral), 28015 Madrid",
            40.439812,
            -3.712123
        ),
        MadridPuntoLimpio(
            "Punto Limpio Fijo - Arganzuela",
            "Calle Cofradía de Aluche (junto al Parque de la Arganzuela), 28005 Madrid",
            40.395122,
            -3.705298
        ),
        MadridPuntoLimpio(
            "Punto Limpio Fijo - Chamartín",
            "Avenida de Alfonso XIII, 128, 28016 Madrid",
            40.457812,
            -3.668112
        ),
        MadridPuntoLimpio(
            "Punto Limpio Fijo - Tetuán",
            "Paseo de la Dirección, 38, 28039 Madrid",
            40.458999,
            -3.705888
        ),
        MadridPuntoLimpio(
            "Punto Limpio Fijo - Moncloa - Aravaca",
            "Avenida de Valladolid (esq. Senda del Rey), 28008 Madrid",
            40.428416,
            -3.729117
        ),
        MadridPuntoLimpio(
            "Punto Limpio Fijo - Fuencarral - El Pardo",
            "Avenida de Montecarmelo, 2, 28049 Madrid",
            40.505122,
            -3.693421
        ),
        MadridPuntoLimpio(
            "Punto Limpio Fijo - Carabanchel",
            "Calle del Aguacate (esq. Vía Lusitana), 28044 Madrid",
            40.370812,
            -3.743112
        ),
        MadridPuntoLimpio(
            "Punto Limpio Fijo - Puente de Vallecas",
            "Calle de Josefa Díaz, 13, 28038 Madrid",
            40.391212,
            -3.642112
        ),
        MadridPuntoLimpio(
            "Punto Limpio Fijo - San Blas - Canillejas",
            "Calle de San Faustino, 31, 28022 Madrid",
            40.435889,
            -3.606111
        )
    )

    // Madrid Center default (Puerta del Sol)
    const val MADRID_CENTER_LAT = 40.416775
    const val MADRID_CENTER_LNG = -3.703790
}
