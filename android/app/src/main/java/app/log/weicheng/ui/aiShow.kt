package app.log.weicheng.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

@Composable
fun LiveHttpInput() {
    var inputText by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var processedResultText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val client = remember {
        OkHttpClient.Builder()
            .connectTimeout(1200, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(1200, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(1200, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Enter Input") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            isLoading = true
                            val rawResult = sendHttpPostRequest(client, inputText)
                            resultText = rawResult
                            Log.d("LiveHttpInput", "Raw Result: $rawResult")
                            processedResultText = processResult(rawResult)
                            Log.d("LiveHttpInput", "Processed Result: $processedResultText")
                        } catch (e: Exception) {
                            resultText = "Error: ${e.message}"
                            Log.e("LiveHttpInput", "Error during request: ${e.message}", e)
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Sending..." else "Send")
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = resultText,
                onValueChange = {},
                label = { Text("Raw Result") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                maxLines = 10
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = processedResultText,
                onValueChange = {},
                label = { Text("Processed Result") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                maxLines = 10
            )
        }
    }
}

suspend fun sendHttpPostRequest(
    client: OkHttpClient,
    inputText: String,
): String = withContext(Dispatchers.IO) {
    Log.d("sendHttpPostRequest", "Starting HTTP request")
    val json = JSONObject().apply {
        put("model", "gemma3:4b")
        put("prompt", inputText)
    }.toString()
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val body = json.toRequestBody(mediaType)
    val request = Request.Builder()
        .url("http://127.0.0.1:11434/api/generate")
        .post(body)
        .build()

    try {
        Log.d("sendHttpPostRequest", "Executing request")
        client.newCall(request).execute().use { response ->
            Log.d("sendHttpPostRequest", "Response received: ${response.code}")
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
            val responseBody = response.body?.string() ?: "No response body"
            Log.d("sendHttpPostRequest", "Request completed successfully")
            return@withContext responseBody
        }
    } catch (e: Exception) {
        Log.e("sendHttpPostRequest", "Request failed: ${e.message}", e)
        return@withContext "Error: ${e.message}"
    }
}

fun processResult(rawResult: String): String {
    return try {
        val responses = mutableListOf<String>()
        var startIndex = 0
        while (startIndex < rawResult.length) {
            val openBraceIndex = rawResult.indexOf('{', startIndex)
            if (openBraceIndex == -1) break // No more JSON objects

            val closeBraceIndex = rawResult.indexOf('}', openBraceIndex)
            if (closeBraceIndex == -1) break // Incomplete JSON

            val jsonString = rawResult.substring(openBraceIndex, closeBraceIndex + 1)
            try {
                val jsonObject = JSONObject(jsonString)
                val response = jsonObject.optString("response", "")
                responses.add(response)
            } catch (e: Exception) {
                Log.e("LiveHttpInput", "Error parsing JSON: $jsonString, ${e.message}")
            }
            startIndex = closeBraceIndex + 1
        }
        responses.joinToString("")
    } catch (e: Exception) {
        Log.e("LiveHttpInput", "Error processing result: ${e.message}", e)
        "Error processing result"
    }
}