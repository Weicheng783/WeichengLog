package app.log.weicheng.ui

import CardSliderColorP
import ColorItem
import ElevatedCardColorP
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.params.DynamicRangeProfiles
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.DocumentsContract
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleLeft
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.materialPath
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import app.log.weicheng.InfiniteTimeGrid
import app.log.weicheng.R
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import loadColorItemsFromSharedPreferences
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import kotlin.random.Random
import java.lang.reflect.Type
import java.io.BufferedWriter
import java.io.OutputStreamWriter

data class ExerciseData(
    val runningDistance: Double?,
    val gymExercises: List<String>?,
    val trainedBodyParts: List<String>?,
    val weight: Double?,
    val difficulty: Int? = null,
    val notes: String? = null // Add notes
)

@Composable
fun RunningAndGymExercise(context: Context, showStat: Boolean) {
    var showStatistics by remember { mutableStateOf(showStat) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var exerciseData by remember { mutableStateOf(loadExerciseData(context, selectedDate) ?: ExerciseData(null, null, null, null)) }
    var editRunningDistance by remember { mutableStateOf(false) }
    var editGymExercises by remember { mutableStateOf(false) }
    var editTrainedBodyParts by remember { mutableStateOf(false) }
    var editWeight by remember { mutableStateOf(false) }
    var editDifficulty by remember { mutableStateOf(false) }
    var editNotes by remember { mutableStateOf(false) } // Add editNotes
    var newRunningDistance by remember { mutableStateOf(exerciseData.runningDistance?.toString() ?: "") }
    var newGymExercises by remember { mutableStateOf(exerciseData.gymExercises?.joinToString(", ") ?: "") }
    var newTrainedBodyParts by remember { mutableStateOf(exerciseData.trainedBodyParts?.joinToString(", ") ?: "") }
    var newWeight by remember { mutableStateOf(exerciseData.weight?.toString() ?: "") }
    var newDifficulty by remember { mutableStateOf(exerciseData.difficulty?.toString() ?: "") }
    var newNotes by remember { mutableStateOf(exerciseData.notes ?: "") } // Add newNotes

    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        if (uri != null) {
            backupData(context, uri)
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            restoreData(context, uri)
            exerciseData = loadExerciseData(context, selectedDate) ?: ExerciseData(null, null, null, null)
            newRunningDistance = exerciseData.runningDistance?.toString() ?: ""
            newGymExercises = exerciseData.gymExercises?.joinToString(", ") ?: ""
            newTrainedBodyParts = exerciseData.trainedBodyParts?.joinToString(", ") ?: ""
            newWeight = exerciseData.weight?.toString() ?: ""
            newDifficulty = exerciseData.difficulty?.toString() ?: ""
            newNotes = exerciseData.notes ?: ""
        }
    }

    LaunchedEffect(selectedDate) {
        exerciseData = loadExerciseData(context, selectedDate) ?: ExerciseData(null, null, null, null)
        newRunningDistance = exerciseData.runningDistance?.toString() ?: ""
        newGymExercises = exerciseData.gymExercises?.joinToString(", ") ?: ""
        newTrainedBodyParts = exerciseData.trainedBodyParts?.joinToString(", ") ?: ""
        newWeight = exerciseData.weight?.toString() ?: ""
        newDifficulty = exerciseData.difficulty?.toString() ?: ""
        newNotes = exerciseData.notes ?: ""
    }

    Column {
        Button(
            onClick = { showStatistics = !showStatistics },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(alignment = Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
        ) {
            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(id = R.drawable.sprint_48px),
                    contentDescription = "Sprint Icon",
                    modifier = Modifier.size(24.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
                )
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = "Gym exercises", // 用于无障碍访问
                    modifier = Modifier.size(24.dp), // 设置图标大小
//                        tint = Color.Unspecified // 使用默认颜色或指定颜色
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = stringResource(R.string.running_gym_exercises))
                Spacer(modifier = Modifier.width(10.dp))
                AssistChip(
                    onClick = { Log.d("Assist chip", "Alpha 0411") },
                    label = { Text("in α", color = MaterialTheme.colorScheme.onPrimary) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = "in Alpha",
                            Modifier.size(AssistChipDefaults.IconSize),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                )
            }
        }

        if (showStatistics) {
            RegistrationDaysCard(context = context, dummyData = false)
            ExerciseStatistics(
                selectedDate = selectedDate,
                onDateChange = {
                    selectedDate = it
                    exerciseData = loadExerciseData(context, selectedDate) ?: ExerciseData(null, null, null, null)
                    newRunningDistance = exerciseData.runningDistance?.toString() ?: ""
                    newGymExercises = exerciseData.gymExercises?.joinToString(", ") ?: ""
                    newTrainedBodyParts = exerciseData.trainedBodyParts?.joinToString(", ") ?: ""
                    newWeight = exerciseData.weight?.toString() ?: ""
                    newDifficulty = exerciseData.difficulty?.toString() ?: ""
                    newNotes = exerciseData.notes ?: ""
                },
                exerciseData = exerciseData,
                editRunningDistance = editRunningDistance,
                editGymExercises = editGymExercises,
                editTrainedBodyParts = editTrainedBodyParts,
                editWeight = editWeight,
                editDifficulty = editDifficulty,
                editNotes = editNotes, // Add editNotes
                onEditRunningDistanceChange = { editRunningDistance = it },
                onEditGymExercisesChange = { editGymExercises = it },
                onEditTrainedBodyPartsChange = { editTrainedBodyParts = it },
                onEditWeightChange = { editWeight = it },
                onEditDifficultyChange = { editDifficulty = it },
                onEditNotesChange = { editNotes = it }, // Add onEditNotesChange
                newRunningDistance = newRunningDistance,
                newGymExercises = newGymExercises,
                newTrainedBodyParts = newTrainedBodyParts,
                newWeight = newWeight,
                newDifficulty = newDifficulty,
                newNotes = newNotes, // Add newNotes
                onNewRunningDistanceChange = { newRunningDistance = it },
                onNewGymExercisesChange = { newGymExercises = it },
                onNewTrainedBodyPartsChange = { newTrainedBodyParts = it },
                onNewWeightChange = { newWeight = it },
                onNewDifficultyChange = { newDifficulty = it },
                onNewNotesChange = { newNotes = it }, // Add onNewNotesChange
                onSaveRunningDistance = {
                    saveExerciseData(context, selectedDate, exerciseData.copy(runningDistance = newRunningDistance.toDoubleOrNull(), difficulty = newDifficulty.toIntOrNull(), notes = newNotes)); exerciseData = exerciseData.copy(runningDistance = newRunningDistance.toDoubleOrNull(), difficulty = newDifficulty.toIntOrNull(), notes = newNotes); editRunningDistance = false
                },
                onSaveGymExercises = {
                    saveExerciseData(context, selectedDate, exerciseData.copy(gymExercises = newGymExercises.split(",").map { it.trim() }, difficulty = newDifficulty.toIntOrNull(), notes = newNotes)); exerciseData = exerciseData.copy(gymExercises = newGymExercises.split(",").map { it.trim() }, difficulty = newDifficulty.toIntOrNull(), notes = newNotes); editGymExercises = false
                },
                onSaveTrainedBodyParts = {
                    saveExerciseData(context, selectedDate, exerciseData.copy(trainedBodyParts = newTrainedBodyParts.split(",").map { it.trim() }, difficulty = newDifficulty.toIntOrNull(), notes = newNotes)); exerciseData = exerciseData.copy(trainedBodyParts = newTrainedBodyParts.split(",").map { it.trim() }, difficulty = newDifficulty.toIntOrNull(), notes = newNotes); editTrainedBodyParts = false
                },
                onSaveWeight = {
                    saveExerciseData(context, selectedDate, exerciseData.copy(weight = newWeight.toDoubleOrNull(), difficulty = newDifficulty.toIntOrNull(), notes = newNotes)); exerciseData = exerciseData.copy(weight = newWeight.toDoubleOrNull(), difficulty = newDifficulty.toIntOrNull(), notes = newNotes); editWeight = false
                },
                onSaveDifficulty = {
                    saveExerciseData(context, selectedDate, exerciseData.copy(difficulty = newDifficulty.toIntOrNull(), notes = newNotes)); exerciseData = exerciseData.copy(difficulty = newDifficulty.toIntOrNull(), notes = newNotes); editDifficulty = false
                },
                onSaveNotes = {
                    saveExerciseData(context, selectedDate, exerciseData.copy(notes = newNotes, difficulty = newDifficulty.toIntOrNull())); exerciseData = exerciseData.copy(notes = newNotes, difficulty = newDifficulty.toIntOrNull()); editNotes = false
                }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { backupLauncher.launch("exercise_data.txt") }) {
                    Text(stringResource(R.string.backup))
                }
                Button(onClick = { restoreLauncher.launch(arrayOf("text/plain")) }) {
                    Text(stringResource(R.string.restore))
                }
            }
        }
    }
}

@Composable
fun ExerciseStatistics(
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    exerciseData: ExerciseData,
    editRunningDistance: Boolean,
    editGymExercises: Boolean,
    editTrainedBodyParts: Boolean,
    editWeight: Boolean,
    editDifficulty: Boolean,
    editNotes: Boolean, // Add editNotes
    onEditRunningDistanceChange: (Boolean) -> Unit,
    onEditGymExercisesChange: (Boolean) -> Unit,
    onEditTrainedBodyPartsChange: (Boolean) -> Unit,
    onEditWeightChange: (Boolean) -> Unit,
    onEditDifficultyChange: (Boolean) -> Unit,
    onEditNotesChange: (Boolean) -> Unit, // Add onEditNotesChange
    newRunningDistance: String,
    newGymExercises: String,
    newTrainedBodyParts: String,
    newWeight: String,
    newDifficulty: String,
    newNotes: String, // Add newNotes
    onNewRunningDistanceChange: (String) -> Unit,
    onNewGymExercisesChange: (String) -> Unit,
    onNewTrainedBodyPartsChange: (String) -> Unit,
    onNewWeightChange: (String) -> Unit,
    onNewDifficultyChange: (String) -> Unit,
    onNewNotesChange: (String) -> Unit, // Add onNewNotesChange
    onSaveRunningDistance: () -> Unit,
    onSaveGymExercises: () -> Unit,
    onSaveTrainedBodyParts: () -> Unit,
    onSaveWeight: () -> Unit,
    onSaveDifficulty: () -> Unit,
    onSaveNotes: () -> Unit // Add onSaveNotes
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val formattedDate = selectedDate.format(formatter)
    val context = LocalContext.current
    var showDifficultyDialog by remember {mutableStateOf(false)}

    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { onDateChange(selectedDate.minusDays(1)) }) {
                Row {
                    Icon(
                        imageVector = Icons.Default.ArrowCircleLeft,
                        contentDescription = "Backward",
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Text(text = formattedDate, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.primary)
            Button(onClick = { onDateChange(selectedDate.plusDays(1)) }) {
                Row {
                    Icon(
                        imageVector = Icons.Default.ArrowCircleRight,
                        contentDescription = "Forward",
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }

        ExerciseCard(
            label = stringResource(R.string.running_distance_km),
            value = exerciseData.runningDistance?.toString() ?: stringResource(R.string.no_data),
            editMode = editRunningDistance,
            onEditChange = onEditRunningDistanceChange,
            onValueChange = onNewRunningDistanceChange,
            onSave = onSaveRunningDistance,
            newValue = newRunningDistance,
            icon = Icons.Default.DirectionsRun
        )

        ExerciseCard(
            label = stringResource(R.string.gym_exercises),
            value = exerciseData.gymExercises?.joinToString(", ") ?: stringResource(R.string.no_data),
            editMode = editGymExercises,
            onEditChange = onEditGymExercisesChange,
            onValueChange = onNewGymExercisesChange,
            onSave = onSaveGymExercises,
            newValue = newGymExercises,
            icon = Icons.Default.FitnessCenter
        )

        ExerciseCard(
            label = stringResource(R.string.trained_body_parts),
            value = exerciseData.trainedBodyParts?.joinToString(", ") ?: stringResource(R.string.no_data),
            editMode = editTrainedBodyParts,
            onEditChange = onEditTrainedBodyPartsChange,
            onValueChange = onNewTrainedBodyPartsChange,
            onSave = onSaveTrainedBodyParts,
            newValue = newTrainedBodyParts,
            icon = Icons.Default.FitnessCenter
        )

        ExerciseCard(
            label = stringResource(R.string.weight_kg),
            value = exerciseData.weight?.toString() ?: stringResource(R.string.no_data),
            editMode = editWeight,
            onEditChange = onEditWeightChange,
            onValueChange = onNewWeightChange,
            onSave = onSaveWeight,
            newValue = newWeight,
            icon = Icons.Default.Scale
        )
        ExerciseCard(
            label = stringResource(R.string.difficulty_1_10),
            value = exerciseData.difficulty?.toString() ?: stringResource(R.string.no_data),
            editMode = editDifficulty,
            onEditChange = onEditDifficultyChange,
            onValueChange = onNewDifficultyChange,
            onSave = onSaveDifficulty,
            newValue = newDifficulty,
            icon = Icons.Default.Info,
            showInfoDialog = {showDifficultyDialog = true}
        )
        ExerciseCard(
            label = stringResource(R.string.notes),
            value = exerciseData.notes ?: stringResource(R.string.no_data),
            editMode = editNotes,
            onEditChange = onEditNotesChange,
            onValueChange = onNewNotesChange,
            onSave = onSaveNotes,
            newValue = newNotes,
            icon = Icons.Default.Notes
        )
        if(showDifficultyDialog){
            AlertDialog(onDismissRequest = { showDifficultyDialog = false }, title = { Text(
                stringResource(R.string.difficulty_instruction)
            ) }, text = {Text(stringResource(R.string._1_means_very_easy_10_means_extremely_hard))}, confirmButton = { Button(onClick = { showDifficultyDialog = false }) {
                Text(stringResource(R.string.ok))
            } })
        }
    }
}

@Composable
fun ExerciseCard(
    label: String,
    value: String,
    editMode: Boolean,
    onEditChange: (Boolean) -> Unit,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    newValue: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    showInfoDialog: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "$label: $value", modifier = Modifier.weight(1f))
                if(icon == Icons.Default.Info){
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Info", modifier = Modifier.clickable { showInfoDialog() })
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Button(onClick = { onEditChange(!editMode) }) {
                    Text(if (editMode) stringResource(R.string.cancel_2) else stringResource(R.string.edit_2))
                }
            }
            if (editMode) {
                TextField(value = newValue, onValueChange = onValueChange)
                Button(onClick = onSave) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}

@Composable
fun RegistrationDaysCard(context: Context, dummyData: Boolean) {
    var registrationDays = 0
    if(dummyData){
        registrationDays = 0
    } else {
        registrationDays = calculateRegistrationDays(context).toInt()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.DateRange, contentDescription = "Days", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.days, registrationDays), modifier = Modifier.weight(1f))
            }
        }
    }
}

fun calculateRegistrationDays(context: Context): Long {
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    val firstDateKey = "first_exercise_date"
    val gson = Gson()
    var count = 0L

    var firstDate: LocalDate? = null
    val allPrefs = sharedPreferences.all

    // Find the earliest date with exercise data
    for ((key, value) in allPrefs) {
        if (key.startsWith("exercise_data_")) {
            try {
                val json = value as String
                val exerciseData: ExerciseData = gson.fromJson(json, ExerciseData::class.java)

                if ((exerciseData.runningDistance != null && exerciseData.runningDistance!! > 0.0) ||
                    (exerciseData.gymExercises != null && exerciseData.gymExercises!!.isNotEmpty())) {
                    val dateString = key.substringAfter("exercise_data_")
                    val currentDate = LocalDate.parse(dateString)

                    if (firstDate == null || currentDate.isBefore(firstDate)) {
                        firstDate = currentDate
                    }
                    count++
                }
            } catch (e: Exception) {
                Log.e("RegistrationDays", "Error parsing exercise data: ${e.message}")
            }
        }
    }

    if (firstDate == null) {
        return 0 // No exercise data found
    }

    return count

//    val today = LocalDate.now()
//    return ChronoUnit.DAYS.between(firstDate, today) + 1
}

fun backupData(context: Context, uri: Uri) {
    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                var currentDate = LocalDate.now().minusDays(30)
                while (currentDate.isBefore(LocalDate.now().plusDays(30))) {
                    loadExerciseData(context, currentDate)?.let { data ->
                        writer.write("${currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}~|~")
                        writer.write("${data.runningDistance ?: ""}~|~")
                        writer.write("${data.gymExercises?.joinToString(";")
                            ?.replace(Regex("[\r\n]+"), "") ?: ""}~|~")
                        writer.write("${data.trainedBodyParts?.joinToString(";")
                            ?.replace(Regex("[\r\n]+"), "") ?: ""}~|~")
                        writer.write("${data.weight ?: ""}~|~")
                        writer.write("${data.difficulty ?: ""}~|~")
                        writer.write("${data.notes?.replace(Regex("[\r\n]+"), "") ?: ""}\n")
                    }
                    currentDate = currentDate.plusDays(1)
                }
            }
        }
    } catch (e: Exception) {
        Log.e("BackupData", "Error backing up data: ${e.message}")
    }
}

fun restoreData(context: Context, uri: Uri) {
    try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.forEachLine { line ->
                    val parts = line.split("~|~")
                    if (parts.size == 7) {
                        val date = LocalDate.parse(parts[0], DateTimeFormatter.ISO_LOCAL_DATE)
                        val runningDistance = parts[1].toDoubleOrNull()
                        val gymExercises = parts[2].takeIf { it.isNotEmpty() }?.split(";")
                        val trainedBodyParts = parts[3].takeIf { it.isNotEmpty() }?.split(";")
                        val weight = parts[4].toDoubleOrNull()
                        val difficulty = parts[5].toIntOrNull()
                        val notes = parts[6]
                        saveExerciseData(context, date, ExerciseData(runningDistance, gymExercises, trainedBodyParts, weight, difficulty, notes))
                    }
                }
            }
        }
    } catch (e: Exception) {
        Log.e("RestoreData", "Error restoring data: ${e.message}")
    }
}

private fun saveExerciseData(context: Context, date: LocalDate, data: ExerciseData) {
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = sharedPreferences.edit()
    val gson = Gson()
    val json = gson.toJson(data)
    editor.putString("exercise_data_$date", json)
    editor.apply()
}

private fun loadExerciseData(context: Context, date: LocalDate): ExerciseData? {
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    val gson = Gson()
    val json = sharedPreferences.getString("exercise_data_$date", null)

    if (json == null) {
        return null
    }
    try {
        val type: Type = object : TypeToken<ExerciseData>() {}.type
        return gson.fromJson(json, type)
    } catch (e: Exception) {
        Log.e("LoadData", "Error loading data: ${e.message}")
        return null
    }
}

//@Preview(showBackground = true)
//@Composable
//fun RunningAndGymPreview() {
//    MaterialTheme {
//        RunningAndGymExercise(LocalContext.current, true)
//    }
//}

@Preview(showBackground = true)
@Composable
fun ExerciseStatisticsPreview() {
    MaterialTheme {
        ExerciseStatistics(
            selectedDate = LocalDate.now(),
            onDateChange = {},
            exerciseData = ExerciseData(10.0, listOf("Push-ups", "Squats"), listOf("Chest", "Legs"), 70.0, 5, "Good workout"),
            editRunningDistance = false,
            editGymExercises = false,
            editTrainedBodyParts = false,
            editWeight = false,
            editDifficulty = false,
            editNotes = false,
            onEditRunningDistanceChange = {},
            onEditGymExercisesChange = {},
            onEditTrainedBodyPartsChange = {},
            onEditWeightChange = {},
            onEditDifficultyChange = {},
            onEditNotesChange = {},
            newRunningDistance = "10.0",
            newGymExercises = "Push-ups, Squats",
            newTrainedBodyParts = "Chest, Legs",
            newWeight = "70.0",
            newDifficulty = "5",
            newNotes = "Good workout",
            onNewRunningDistanceChange = {},
            onNewGymExercisesChange = {},
            onNewTrainedBodyPartsChange = {},
            onNewWeightChange = {},
            onNewDifficultyChange = {},
            onNewNotesChange = {},
            onSaveRunningDistance = {},
            onSaveGymExercises = {},
            onSaveTrainedBodyParts = {},
            onSaveWeight = {},
            onSaveDifficulty = {},
            onSaveNotes = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrationDaysCardPreview() {
    MaterialTheme {
        RegistrationDaysCard(LocalContext.current, dummyData = true)
    }
}
