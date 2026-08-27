package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.Task
import com.example.model.TaskStatus
import com.example.network.GeminiClient
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraNeonLime
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.io.File
import java.util.UUID

class VoiceAddActivity : ComponentActivity() {

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    private var isListening by mutableStateOf(false)
    private var recognizedText by mutableStateOf("")
    private var isProcessing by mutableStateOf(false)
    private var isSuccess by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        Log.d("VoiceAddActivity", "Permission result: isGranted=$isGranted")
        if (isGranted) {
            startListening()
        } else {
            Log.e("VoiceAddActivity", "Microphone permission denied.")
            Toast.makeText(this, "Microphone permission is required", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (isSuccess) {
                                Text("Task Created!", color = VioraNeonLime, fontSize = 24.sp, fontFamily = SFProDisplayFontFamily, fontWeight = FontWeight.Bold)
                            } else if (isProcessing) {
                                CircularProgressIndicator(color = VioraNeonLime)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(recognizedText, color = Color.Black)
                            } else {
                                Text(if (isListening) "Listening..." else "Tap to listen", color = Color.Black, fontSize = 20.sp, fontFamily = SFProDisplayFontFamily, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(text = recognizedText.ifEmpty { "Waiting for your voice..." }, color = Color.Gray, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { 
                                        if (isListening) {
                                            stopListeningAndProcess()
                                        } else {
                                            if (ContextCompat.checkSelfPermission(this@VoiceAddActivity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                                startListening()
                                            } else {
                                                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isListening) Color.Red else VioraNeonLime)
                                ) {
                                    Text(if (isListening) "Stop & Process" else "Listen", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        Log.d("VoiceAddActivity", "Checking RECORD_AUDIO permission...")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            Log.d("VoiceAddActivity", "RECORD_AUDIO permission already granted.")
            startListening()
        } else {
            Log.d("VoiceAddActivity", "Requesting RECORD_AUDIO permission...")
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startListening() {
        audioFile = File(cacheDir, "audio_record.mp4")
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        
        try {
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()
            }
            isListening = true
            recognizedText = "درحال ضبط صدا..."
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to start recording", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopListeningAndProcess() {
        if (!isListening) return
        isListening = false
        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaRecorder?.release()
        mediaRecorder = null
        
        isProcessing = true
        recognizedText = "درحال پردازش با هوش مصنوعی..."
        
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            if (audioFile != null && audioFile!!.exists()) {
                val bytes = audioFile!!.readBytes()
                val base64Audio = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                
                // Fetch context
                var contextInfo = ""
                try {
                    val prefs = getSharedPreferences("viora_task_prefs", android.content.Context.MODE_PRIVATE)
                    val mockListsJson = prefs.getString("mock_lists", null)
                    if (mockListsJson != null) {
                        val moshi = com.squareup.moshi.Moshi.Builder().addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
                        val mockListsAdapter = moshi.adapter<Map<String, com.example.model.TaskList>>(com.squareup.moshi.Types.newParameterizedType(Map::class.java, String::class.java, com.example.model.TaskList::class.java))
                        val loaded = mockListsAdapter.fromJson(mockListsJson)
                        if (loaded != null) {
                            val availableInfo = loaded.values.map { "- List: ${it.name}, Team: ${it.teamId}" }.joinToString("\n")
                            contextInfo = "Available lists and their teams:\n$availableInfo"
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }

                val jsonResult = com.example.network.GeminiClient.generateTaskFromAudio(base64Audio, "audio/mp4", contextInfo)
                
                if (jsonResult != null) {
                    try {
                        val jsonObj = org.json.JSONObject(jsonResult)
                        val title = jsonObj.optString("title", "تسک صوتی")
                        val desc = jsonObj.optString("description", "")
                        val listId = jsonObj.optString("listId", "Unplanned Tasks")
                        val teamId = jsonObj.optString("teamId", "")
                        val daysLeft = jsonObj.optInt("daysLeft", 7)
                        val dueTime = jsonObj.optString("dueTime", "").trim()
                        val hasExplicit = jsonObj.optBoolean("hasExplicitDeadline", false)

                        var deadlineMillis: Long? = null
                        var dueDateTextStr = ""

                        if (hasExplicit || dueTime.isNotEmpty()) {
                            val cal = java.util.Calendar.getInstance()
                            cal.add(java.util.Calendar.DAY_OF_YEAR, daysLeft)

                            var formattedTime = ""
                            if (dueTime.matches(Regex("\\d{1,2}:\\d{2}"))) {
                                val parts = dueTime.split(":")
                                val h = parts[0].toIntOrNull() ?: 23
                                val m = parts[1].toIntOrNull() ?: 59
                                cal.set(java.util.Calendar.HOUR_OF_DAY, h)
                                cal.set(java.util.Calendar.MINUTE, m)
                                cal.set(java.util.Calendar.SECOND, 0)
                                cal.set(java.util.Calendar.MILLISECOND, 0)
                                formattedTime = String.format(java.util.Locale.getDefault(), "%02d:%02d", h, m)
                            } else {
                                cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
                                cal.set(java.util.Calendar.MINUTE, 59)
                                cal.set(java.util.Calendar.SECOND, 0)
                                cal.set(java.util.Calendar.MILLISECOND, 0)
                            }

                            deadlineMillis = cal.timeInMillis

                            val dayPrefix = when (daysLeft) {
                                0 -> "Today"
                                1 -> "Tomorrow"
                                else -> java.text.SimpleDateFormat("yyyy MMM dd", java.util.Locale.ENGLISH).format(cal.time)
                            }

                            dueDateTextStr = if (formattedTime.isNotEmpty()) {
                                "$dayPrefix at $formattedTime"
                            } else {
                                dayPrefix
                            }
                        }

                        saveTask(title, desc, listId, teamId, daysLeft, deadlineMillis, dueDateTextStr) {
                            isProcessing = false
                            isSuccess = true
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                kotlinx.coroutines.delay(1500)
                                finish()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        saveTask("تسک صوتی جدید", "یادداشت صوتی (پردازش محلی)", "Unplanned Tasks", "", 7, null, "") {
                            isProcessing = false
                            isSuccess = true
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                kotlinx.coroutines.delay(1500)
                                finish()
                            }
                        }
                    }
                } else {
                    // Fallback to offline creation when Gemini API key is missing or server is unreachable
                    saveTask("تسک صوتی جدید", "یادداشت صوتی سریع (آفلاین)", "Unplanned Tasks", "", 7, null, "") {
                        isProcessing = false
                        isSuccess = true
                        android.widget.Toast.makeText(this@VoiceAddActivity, "تسک صوتی ذخیره شد", android.widget.Toast.LENGTH_SHORT).show()
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            kotlinx.coroutines.delay(1200)
                            finish()
                        }
                    }
                }
            } else {
                android.widget.Toast.makeText(this@VoiceAddActivity, "صدایی ضبط نشد", android.widget.Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun saveTask(
        title: String,
        desc: String,
        folder: String,
        teamId: String,
        daysLeft: Int,
        selectedDeadlineMillis: Long? = null,
        dueDateText: String = "",
        onSaved: () -> Unit
    ) {
        val newTask = Task(
            id = UUID.randomUUID().toString(),
            title = title,
            client = "Viora design",
            userId = "user1",
            teamId = teamId.ifEmpty { null },
            listId = folder.lowercase().replace(" ", "_"),
            folder = folder.ifEmpty { "Unplanned Tasks" },
            daysLeft = daysLeft,
            status = TaskStatus.TODO,
            description = desc,
            selectedDeadlineMillis = selectedDeadlineMillis,
            dueDateText = dueDateText
        )
        
        Thread {
            try {
                val prefs = getSharedPreferences("viora_task_prefs", Context.MODE_PRIVATE)
                val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                val tasksAdapter = moshi.adapter<List<Task>>(Types.newParameterizedType(List::class.java, Task::class.java))
                
                val tasksJson = prefs.getString("tasks", null)
                val currentTasks = if (tasksJson != null) {
                    tasksAdapter.fromJson(tasksJson)?.toMutableList() ?: mutableListOf()
                } else {
                    mutableListOf()
                }
                
                currentTasks.add(0, newTask) // Add to top
                
                prefs.edit().putString("tasks", tasksAdapter.toJson(currentTasks)).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            runOnUiThread {
                onSaved()
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        mediaRecorder = null
    }
}
