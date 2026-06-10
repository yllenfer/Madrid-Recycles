package com.example.data

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

// --- Gemini Request/Response Models ---

@JsonClass(generateAdapter = true)
data class InlineData(
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inlineData") val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = null,
    @Json(name = "temperature") val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content? = null
)

// --- Structured Waste Sorting Output Model ---

@JsonClass(generateAdapter = true)
data class GarbageSortingResult(
    @Json(name = "container") val container: String, // AMARILLO, AZUL, VERDE, MARRON, GRIS, PUNTO_LIMPIO
    @Json(name = "itemName") val itemName: String,
    @Json(name = "explanation") val explanation: String,
    @Json(name = "comunidadMadridPolicy") val comunidadMadridPolicy: String,
    @Json(name = "specialInstructions") val specialInstructions: String
)

// --- Retrofit Interface ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

// --- Gemini Repository / Service Provider ---

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun getSystemInstructions(language: String): String {
        val langName = if (language == "en") "English" else "Spanish (español)"
        val jsonExample = if (language == "en") {
            """
            {
              "container": "AMARILLO" | "AZUL" | "VERDE" | "MARRON" | "GRIS" | "PUNTO_LIMPIO",
              "itemName": "Exact description of the item in English",
              "explanation": "Detailed explanation in English of why it belongs to this container under Madrid regulations.",
              "comunidadMadridPolicy": "Mention general Comunidad de Madrid policies relevant to this type of item.",
              "specialInstructions": "Preparatory step or useful warning for the modern citizen of Madrid (e.g., 'Remove the bottle cap before putting it in Verde', 'Rinse the food container first', 'You can take it to your district's mobile clean point')."
            }
            """
        } else {
            """
            {
              "container": "AMARILLO" | "AZUL" | "VERDE" | "MARRON" | "GRIS" | "PUNTO_LIMPIO",
              "itemName": "Nombre exacto y descriptivo del residuo en español",
              "explanation": "Explicación detallada en español de por qué va en ese contenedor específico basándote en las normativas de Madrid.",
              "comunidadMadridPolicy": "Mención a las políticas medioambientales generales de la Comunidad de Madrid aplicables a este objeto.",
              "specialInstructions": "Paso preparatorio o advertencia útil para el ciudadano madrileño (ej: 'Quita el tapón antes de meter en el verde', 'Límpialo de comida antes', 'Puedes llevarlo al punto limpio móvil del distrito')."
            }
            """
        }

        return """
            Eres un experto en clasificación de residuos urbanos con conocimiento exacto de la normativa y directivas medioambientales de la Comunidad de Madrid y el Ayuntamiento de Madrid, España.
            
            Dado un objeto (o imagen de él) que el usuario quiere desechar, debes clasificarlo estrictamente en uno de estos contenedores específicos de Madrid:
            1. AMARILLO: Envases de plástico, latas, briks (bebidas, salsas). NO juguetes, NO perchas, NO tuppers.
            2. AZUL: Cartón, papel (cajas, periódicos, revistas, bolsas de papel). NO briks, NO papel sucio con comida.
            3. VERDE: Botellas, tarros, frascos de VIDRIO únicamente. Sin tapas ni tapones. NO platos, NO cristales, NO bombillas.
            4. MARRON: Residuos orgánicos biodegradables (restos de comida, posos de café, infusiones, servilletas y papel de cocina sucios de comida, tapones de corcho natural).
            5. GRIS: Resto de residuos (porcelana rota, tazas de cerámica, colillas, pañales, comprensas, juguetes rotos, perchas, polvo).
            6. PUNTO_LIMPIO: Puntos de reciclaje especial para residuos domésticos no comunes (pilas, bombillas, aceites vegetales usados, pintura, electrodomésticos, radiografías, textiles, etc.).
            
            El usuario está utilizando la interfaz en idioma: $langName.
            Por lo tanto, debes responder SIEMPRE con un objeto JSON válido donde los campos de teexto estén escritos en $langName.
            El formato del JSON debe cumplir exactamente con esta estructura de campos (responde con JSON puro):
            $jsonExample
            
            Sé extremadamente preciso. Si un objeto debe ir a un Punto Limpio, clasifícalo como PUNTO_LIMPIO.
        """.trimIndent()
    }

    suspend fun analyzeSearchQuery(query: String, language: String = "es"): GarbageSortingResult? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API Key is missing or default placeholder!")
            return@withContext makeOfflineFallbackResult(query, language)
        }

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = "Clasifica este objeto de desecho: $query")))
            ),
            systemInstruction = Content(parts = listOf(Part(text = getSystemInstructions(language)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2f
            )
        )

        try {
            val response = service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonText != null) {
                // Parse the response
                val adapter = moshi.adapter(GarbageSortingResult::class.java)
                adapter.fromJson(jsonText)
            } else {
                Log.e(TAG, "Empty response from Gemini")
                makeOfflineFallbackResult(query, language)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API: ${e.message}", e)
            makeOfflineFallbackResult(query, language)
        }
    }

    suspend fun analyzeImageFrame(bitmap: Bitmap, language: String = "es"): GarbageSortingResult? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API Key is missing or default placeholder!")
            return@withContext makeOfflineFallbackResult("Imagen de residuo", language)
        }

        val base64Image = bitmap.toBase64()
        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(
                    Part(text = "Clasifica este residuo de la imagen."),
                    Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                ))
            ),
            systemInstruction = Content(parts = listOf(Part(text = getSystemInstructions(language)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2f
            )
        )

        try {
            val response = service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonText != null) {
                val adapter = moshi.adapter(GarbageSortingResult::class.java)
                adapter.fromJson(jsonText)
            } else {
                Log.e(TAG, "Empty response from Gemini")
                makeOfflineFallbackResult("Imagen de residuo", language)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini with image: ${e.message}", e)
            makeOfflineFallbackResult("Imagen de residuo", language)
        }
    }

    private fun makeOfflineFallbackResult(query: String, language: String): GarbageSortingResult {
        val cleanQuery = query.lowercase().trim()
        val isEn = language == "en"
        return when {
            cleanQuery.contains("plástico") || cleanQuery.contains("plastic") || cleanQuery.contains("botella") || cleanQuery.contains("bottle") || cleanQuery.contains("lata") || cleanQuery.contains("can") || cleanQuery.contains("brick") || cleanQuery.contains("brik") || cleanQuery.contains("envase") || cleanQuery.contains("metal") || cleanQuery.contains("aluminio") -> {
                if (isEn) {
                    GarbageSortingResult(
                        "AMARILLO",
                        query,
                        "Detected as lightweight packaging. According to Madrid directives, plastic containers, cans, and cartons are sorted in the Yellow container.",
                        "Comunidad de Madrid regulation for lightweight packaging.",
                        "Do not forget to empty all contents before throwing it away."
                    )
                } else {
                    GarbageSortingResult(
                        "AMARILLO",
                        query,
                        "Detectado como envase ligero. Según la directiva de Madrid, los envases de plástico, briks y latas se clasifican en el contenedor amarillo.",
                        "Normativa Comunidad de Madrid para Envases Ligeros.",
                        "No olvides vaciar todo el contenido antes de tirarlo."
                    )
                }
            }
            cleanQuery.contains("papel") || cleanQuery.contains("paper") || cleanQuery.contains("carton") || cleanQuery.contains("cartón") || cleanQuery.contains("cardboard") || cleanQuery.contains("caja") || cleanQuery.contains("box") || cleanQuery.contains("periodico") || cleanQuery.contains("periódico") || cleanQuery.contains("newspaper") || cleanQuery.contains("revista") -> {
                if (isEn) {
                    GarbageSortingResult(
                        "AZUL",
                        query,
                        "Classified as paper/cardboard. Belongs to the Blue container.",
                        "Cellulose recycling encouragement of the Comunidad de Madrid.",
                        "Fold it properly so it does not occupy excess space in the bin."
                    )
                } else {
                    GarbageSortingResult(
                        "AZUL",
                        query,
                        "Clasificado como papel/cartón. Pertenece al contenedor azul.",
                        "Fomento del reciclaje de celulosa de la Comunidad de Madrid.",
                        "Pliégalo bien para que no ocupe un volumen excesivo en el contenedor."
                    )
                }
            }
            cleanQuery.contains("vidrio") || cleanQuery.contains("glass") || cleanQuery.contains("botella de cerveza") || cleanQuery.contains("bottle") || cleanQuery.contains("tarro") || cleanQuery.contains("jar") || cleanQuery.contains("frasco") || cleanQuery.contains("copa") -> {
                if (isEn) {
                    GarbageSortingResult(
                        "VERDE",
                        query,
                        "It is a glass container. Deposit it in the Green igloo container.",
                        "Glass recovery plan in Madrid.",
                        "Please remove twist caps or corks before putting the bottle inside."
                    )
                } else {
                    GarbageSortingResult(
                        "VERDE",
                        query,
                        "Es un envase de vidrio. Deposítalo en el contenedor verde verde (el iglú).",
                        "Plan de fomento del Vidrio en Madrid.",
                        "Por favor retira tapones de rosca o corcho antes de introducir el frasco en el contenedor."
                    )
                }
            }
            cleanQuery.contains("comida") || cleanQuery.contains("food") || cleanQuery.contains("manzana") || cleanQuery.contains("apple") || cleanQuery.contains("platano") || cleanQuery.contains("banana") || cleanQuery.contains("organico") || cleanQuery.contains("organic") || cleanQuery.contains("restos") || cleanQuery.contains("corcho") || cleanQuery.contains("servilleta sucia") || cleanQuery.contains("napkin") -> {
                if (isEn) {
                    GarbageSortingResult(
                        "MARRON",
                        query,
                        "This belongs to biodegradable organic waste (Brown container).",
                        "Biogas and organic composting encouragement under Madrid Waste Law.",
                        "Deposit in a compostable bag if possible."
                    )
                } else {
                    GarbageSortingResult(
                        "MARRON",
                        query,
                        "Esto pertenece al residuo orgánico biodegradable (Contenedor Marrón).",
                        "Fomento del biogás y compostaje ecológico, Ley de Residuos de Madrid.",
                        "Deposítalo en bolsa compostable si es posible."
                    )
                }
            }
            cleanQuery.contains("pila") || cleanQuery.contains("battery") || cleanQuery.contains("bateria") || cleanQuery.contains("batería") || cleanQuery.contains("aceite") || cleanQuery.contains("oil") || cleanQuery.contains("electrodomestico") || cleanQuery.contains("appliance") || cleanQuery.contains("ordenador") || cleanQuery.contains("computer") || cleanQuery.contains("bombilla") || cleanQuery.contains("bulb") || cleanQuery.contains("fluorescente") || cleanQuery.contains("pintura") || cleanQuery.contains("paint") || cleanQuery.contains("textil") || cleanQuery.contains("ropa") || cleanQuery.contains("clothes") -> {
                if (isEn) {
                    GarbageSortingResult(
                        "PUNTO_LIMPIO",
                        query,
                        "This waste contains chemicals or hazardous components. Must go to a specialized fixed or mobile Recycling Point.",
                        "Specialized Waste Regulation of the Comunidad de Madrid.",
                        "Check the 'Recycling Points' tab to find the nearest facility."
                    )
                } else {
                    GarbageSortingResult(
                        "PUNTO_LIMPIO",
                        query,
                        "Este residuo contiene químicos o componentes peligrosos. Debe acudir a un Punto Limpio fijo o móvil.",
                        "Reglamento de Residuos Especiales de la Comunidad de Madrid.",
                        "Consulta en la sección 'Puntos Limpios' para ubicar el centro más cercano a tu ubicación."
                    )
                }
            }
            else -> {
                if (isEn) {
                    GarbageSortingResult(
                        "GRIS",
                        query,
                        "Not recyclable via direct domestic sorting, or compound waste. Throw in Grey container (Resto).",
                        "General waste fractional regulations of Madrid.",
                        "Use standard trash bags and throw away securely sealed."
                    )
                } else {
                    GarbageSortingResult(
                        "GRIS",
                        query,
                        "No es reciclable por métodos domésticos directos o es desecho doméstico compuesto. Se tira al Contenedor Gris (Resto).",
                        "Regulación general de fracción resto de Madrid.",
                        "Utilice bolsas de basura convencionales y arrójelo bien cerrado."
                    )
                }
            }
        }
    }
}
