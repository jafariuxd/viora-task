package com.example.network

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.BuildConfig
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    val mimeType: String,
    val data: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }

    private val systemInstructionText = """
        You are a helpful assistant that converts user voice notes into a structured task JSON.
        Return ONLY a valid JSON object. Do not include markdown formatting or backticks.
        If the user input contains English phrases amid Persian, correctly parse the entire intent.
        ALWAYS translate the user's intent and output the `title` and `description` in English, regardless of the language the user speaks.
        Pay special attention to proper nouns, brand names, technical terminology, and English loanwords spoken in Persian. Do not aggressively autocorrect these into similar-sounding, unrelated Persian words. Use the surrounding context to accurately transcribe industry terms and brands.
        Extract the task title, description, appropriate list/folder (e.g. Design, Development, Marketing, Unplanned Tasks - default to Unplanned Tasks if not clear), deadline or days left, and specific due time if mentioned.
        For deadline, output an integer for `daysLeft`. For example, if it's today `daysLeft` is 0, tomorrow is 1, next week is 7. Default is 7 if no date specified.
        If a specific time or hour is mentioned (e.g., "at 5pm", "ساعت ۶ عصر", "ساعت ۱۰ صبح", "ساعت ۸ شب"), convert it to 24-hour HH:mm format (e.g. "17:00", "18:00", "10:00", "20:00") in `dueTime`.
        The JSON object must match this schema:
        {
          "title": "Task title in English",
          "description": "Any additional details or context in English (optional)",
          "listId": "The folder/list it belongs to",
          "teamId": "The name of the team this list belongs to (optional, but include if available)",
          "daysLeft": 1,
          "dueTime": "18:30",
          "hasExplicitDeadline": true
        }
    """.trimIndent()

    suspend fun generateTaskFromJson(prompt: String, contextInfo: String = ""): String? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty()) return@withContext null

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt + "\n\n" + contextInfo)))),
            systemInstruction = Content(parts = listOf(Part(text = systemInstructionText))),
            generationConfig = GenerationConfig(responseMimeType = "application/json")
        )

        try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun generateTaskFromAudio(base64Audio: String, mimeType: String, contextInfo: String = ""): String? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty()) return@withContext null

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(
                Part(text = "Please transcribe this audio and create the JSON task.\n\n" + contextInfo),
                Part(inlineData = InlineData(mimeType = mimeType, data = base64Audio))
            ))),
            systemInstruction = Content(parts = listOf(Part(text = systemInstructionText))),
            generationConfig = GenerationConfig(responseMimeType = "application/json")
        )

        try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    
    }

    suspend fun generateDailyBrief(tasksContext: String, eventsContext: String, timeOfDay: String): String? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty()) return@withContext null
        
        val instruction = """
            You are a creative, motivating AI assistant inside a productivity app called Viora.
            Generate a unique daily brief for the user based on their current tasks and events.
            Return ONLY a valid JSON object. Do not include markdown formatting or backticks.
            
            The JSON object must match this schema:
            {
              "greeting": "A creative, varying greeting matching the time of day ($timeOfDay) (e.g. 'Rise and shine!', 'Good afternoon').",
              "summary": "A dynamically generated, highly varied, conversational paragraph (2-3 sentences) summarizing their day. Mention their tasks and events smoothly. Make it sound insightful and human. Vary the tone (e.g., focused, relaxed, encouraging).",
              "insight": "A unique, inspiring quote, productivity tip, or mindset advice. Be creative."
            }
        """.trimIndent()
        
        val prompt = "Tasks today:\n$tasksContext\n\nEvents today:\n$eventsContext"
        
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = instruction))),
            generationConfig = GenerationConfig(responseMimeType = "application/json")
        )
        try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
