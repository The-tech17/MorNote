package com.sanjana.mornote.data

import com.google.firebase.functions.FirebaseFunctions
import com.sanjana.mornote.ui.detail.AiAction
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BackendAiManager {
    private val functions = FirebaseFunctions.getInstance()

    suspend fun processAiAction(
        action: AiAction, 
        text: String, 
        provider: AiProvider
    ): String? = withContext(Dispatchers.IO) {
        val data = hashMapOf(
            "text" to text,
            "action" to action.title,
            "provider" to provider.name
        )

        try {
            val result = functions
                .getHttpsCallable("processAiAction")
                .call(data)
                .await()
            
            val response = result.data as? Map<*, *>
            response?.get("result") as? String
        } catch (_: Exception) {
            null
        }
    }
}
