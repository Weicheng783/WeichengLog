package app.log.weicheng.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit

fun writeToFile(context: Context, action: String, input: String, time: Long) {
    val filename = "recording_log.txt"
    val storageDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        context.getExternalFilesDir(null) // Scoped storage for Android 10+
    } else {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    }
    val file = File(storageDir, filename)
    val writer = FileWriter(file, true)

    val timeString = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(time))
    writer.append("$action - $input - $timeString\n")
    writer.close()
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun RecordingScreen() {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var userInput by remember { mutableStateOf("") }
    var lastTime: Long? = null
    val mutex = Mutex()
    val coroutineScope = rememberCoroutineScope()
    var elapsedString by remember { mutableStateOf("00:00:00") }

    fun startRecording() {
        if (!isRecording) {
            isRecording = true
            lastTime = System.currentTimeMillis()
            coroutineScope.launch {
                while (isActive && isRecording) {
                    delay(1000) // Update every second
                    mutex.withLock {
                        val currentTime = System.currentTimeMillis()
                        val elapsedTime = currentTime - lastTime!!
                        elapsedString = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                            TimeUnit.MILLISECONDS.toHours(elapsedTime),
                            TimeUnit.MILLISECONDS.toMinutes(elapsedTime) % 60,
                            TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60)
                        writeToFile(context, "update", userInput, currentTime)
                        lastTime = currentTime
                    }
                }
                if (!isActive) {
                    // Recording stopped, write final entry
                    val endTime = System.currentTimeMillis()
                    writeToFile(context, "finish", userInput, endTime)
                }
            }
        }
    }

    fun stopRecording() {
        isRecording = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = if (isRecording) "Recording..." else "Not Recording")
        Text(text = if (isRecording) elapsedString else "00:00:00")
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = userInput,
            onValueChange = { userInput = it },
            label = { Text("What are you doing right now?") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }) {
            Text(text = if (isRecording) "Stop Recording" else "Start Recording")
        }
    }
}