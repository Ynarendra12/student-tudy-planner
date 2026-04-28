package com.example.studentstudyplanner.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GenerativeAiHelper {
    /**
     * Gemini API Key provided by user.
     */
    private const val API_KEY = "AIzaSyDtOTB8QTYm2yTizH6WLLc5Ym9Z6je3Yzk"

    private val model by lazy {
        GenerativeModel(
            // Using the base model name. If this fails, the key might be for a restricted project.
            modelName = "gemini-1.5-flash",
            apiKey = API_KEY.trim(),
            systemInstruction = content { 
                text("You are a high-speed, direct academic tutor. Provide only the essential solution or summary. Use bold text for key terms. No conversational filler.") 
            }
        )
    }

    // Use a chat session for a real "Chat" experience that remembers context
    private var chatSession = model.startChat()

    suspend fun getExplanation(prompt: String): String = withContext(Dispatchers.IO) {
        if (API_KEY.isBlank() || API_KEY.contains("YOUR_GEMINI")) {
            return@withContext "Please enter a valid API key in GenerativeAiHelper.kt"
        }
        
        try {
            // Sending message through chatSession to make it feel like "Real Gemini"
            val response = chatSession.sendMessage(prompt)
            response.text ?: "I could not generate an answer. Please rephrase."
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: ""
            if (errorMsg.contains("404") || errorMsg.contains("not found", true)) {
                // If gemini-1.5-flash fails with 404, we try the 1.0 pro model as a fallback
                try {
                    val fallbackModel = GenerativeModel(modelName = "gemini-pro", apiKey = API_KEY.trim())
                    val response = fallbackModel.generateContent(prompt)
                    response.text ?: "Solution not found."
                } catch (ex: Exception) {
                    "AI Error: The model 'gemini-1.5-flash' is not active for this key yet.\n\n" +
                    "FIX: Go to https://aistudio.google.com/ and create a NEW key. Use 'Create API key in new project'."
                }
            } else if (errorMsg.contains("safety", true)) {
                "AI Error: This request was blocked by safety filters. Please ask academic questions only."
            } else {
                "Gemini Error: $errorMsg"
            }
        }
    }
}
