package com.example.ui

object AppStrings {
    private val es = mapOf(
        "app_title" to "🪺 Madrid Recicla",
        "scanner_tab" to "Escáner",
        "search_tab" to "Buscador",
        "points_tab" to "Puntos Limpios",
        "scanner_subtitle" to "Apunta al residuo con la cámara para clasificarlo al instante",
        "emulator_warning" to "💡 ¿En emulador? Haz clic en un objeto de prueba abajo",
        "emulator_drawer_title" to "⚡ Simulador de Escaneo Rápido (Prueba sin cámara)",
        "analyzing_ai" to "Analizando con IA...",
        "consulting_rules" to "Consultando normativas y directrices medioambientales de Madrid...",
        "camera_access_required" to "Acceso a la Cámara Requerido",
        "camera_access_desc" to "Madrid Recicla necesita acceso a tu cámara para escanear de manera inteligente los objetos que quieres desechar y decirte en qué contenedor de Madrid depositarlos.",
        "enable_camera" to "Habilitar Cámara",
        "search_placeholder" to "Escribe un objeto (ej. bombilla, pañal...)",
        "search_cta" to "Clasificar con IA de Madrid 🇪🇸",
        "container_guide_title" to "Guía de Contenedores de Madrid",
        "container_examples_label" to "Ejemplos habituales (toca para probar):",
        "ref_location_title" to "Ubicación de Referencia",
        "loc_simulated" to "📍 Simulado: Madrid",
        "loc_gps" to "🛰️ Ubicación real detectada por el GPS de tu móvil.",
        "loc_change_hint" to "Cambiar barrio de simulación para calcular distancias:",
        "locations_main_title" to "Puntos Limpios Fijos en Madrid",
        "how_to_get_there" to "¿Cómo llegar desde mi posición?",
        "got_it_btn" to "Entendido, gracias",
        "classification_instructions_label" to "🔍 Instrucciones de Clasificación:",
        "madrid_policy_label" to "🇪🇸 Directiva Comunidad de Madrid:",
        "error_title" to "Ha ocurrido un problema",
        "error_generic" to "No se ha podido procesar la clasificación. Inténtalo de nuevo.",
        "error_conn" to "Error en la conexión con el servicio: ",
        "error_image" to "No se ha podido identificar el objeto de la imagen.",
        "error_image_proc" to "Error al procesar la imagen: ",
        "error_ok" to "Vale"
    )

    private val en = mapOf(
        "app_title" to "🪺 Madrid Recycles",
        "scanner_tab" to "Scanner",
        "search_tab" to "Search",
        "points_tab" to "Recycling Points",
        "scanner_subtitle" to "Point the camera at waste to classify it instantly",
        "emulator_warning" to "💡 In workspace preview? Click a test object below",
        "emulator_drawer_title" to "⚡ Fast Scan Simulator (Test without camera)",
        "analyzing_ai" to "Analyzing with AI...",
        "consulting_rules" to "Consulting Madrid environmental regulations and directives...",
        "camera_access_required" to "Camera Access Required",
        "camera_access_desc" to "Madrid Recicla requires camera access to intelligently scan materials you want to discard and guide you to correct bins in Madrid.",
        "enable_camera" to "Enable Camera",
        "search_placeholder" to "Type an item (e.g., light bulb, diaper...)",
        "search_cta" to "Classify with Madrid AI 🇪🇸",
        "container_guide_title" to "Madrid Container Guide",
        "container_examples_label" to "Common examples (tap to try):",
        "ref_location_title" to "Reference Location",
        "loc_simulated" to "📍 Simulated: Madrid",
        "loc_gps" to "🛰️ Real location detected by your device GPS.",
        "loc_change_hint" to "Change neighborhood simulation to compute distances:",
        "locations_main_title" to "Fixed Recycling Points in Madrid",
        "how_to_get_there" to "How to get there from my location?",
        "got_it_btn" to "Got it, thank you",
        "classification_instructions_label" to "🔍 Sorting Instructions:",
        "madrid_policy_label" to "🇪🇸 Comunidad de Madrid Directive:",
        "error_title" to "A problem occurred",
        "error_generic" to "Could not process sorting. Please try again.",
        "error_conn" to "Service connection error: ",
        "error_image" to "Could not identify the object in the photo.",
        "error_image_proc" to "Error processing image: ",
        "error_ok" to "Dismiss"
    )

    fun get(key: String, lang: String): String {
        return if (lang == "en") {
            en[key] ?: es[key] ?: key
        } else {
            es[key] ?: key
        }
    }
}
