import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.preference.PreferenceManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.datastore.preferences.core.edit
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.log.weicheng.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import java.lang.reflect.Type
import java.time.LocalDate

val Context.storeData by preferencesDataStore("data_photojournal")

data class ExerciseData(
    val notes: String? = null,
    val difficulty: Int? = null
)

class StoreData {
    suspend fun storeImage(context: Context, date: String, uriString: String) {
        context.storeData.edit { preferences ->
            preferences[stringPreferencesKey("image_$date")] = uriString
        }
    }

    suspend fun getImage(context: Context, date: String): Flow<String?> {
        return context.storeData.data.map { preferences ->
            preferences[stringPreferencesKey("image_$date")]
        }
    }

    suspend fun storeStory(context: Context, date: String, story: String) {
        context.storeData.edit { preferences ->
            preferences[stringPreferencesKey("story_$date")] = story
        }
    }

    suspend fun getStory(context: Context, date: String): Flow<String?> {
        return context.storeData.data.map { preferences ->
            preferences[stringPreferencesKey("story_$date")]
        }
    }

    suspend fun backupData(context: Context, backupFile: File) {
        val prefs = context.storeData.data.first()
        val json = JSONObject()
        val imageMap = mutableMapOf<String, String>()

        prefs.asMap().forEach { (key, value) ->
            if (key.name.startsWith("image_")) {
                val imageUriString = value.toString()
                val imageUri = Uri.parse(imageUriString)
                val imageName = File(imageUri.path ?: "").name
                imageMap[key.name] = imageName
                json.put(key.name, imageName) // Store relative file name in json
            } else {
                json.put(key.name, value.toString())
            }
        }

        withContext(Dispatchers.IO) {
            ZipOutputStream(FileOutputStream(backupFile)).use { zipOutputStream ->
                val entry = ZipEntry("data.json")
                zipOutputStream.putNextEntry(entry)
                zipOutputStream.write(json.toString().toByteArray())
                zipOutputStream.closeEntry()

                imageMap.forEach { (key, imageName) ->
                    val imageUri = Uri.parse(prefs[stringPreferencesKey(key)].toString())
                    try {
                        val inputStream = context.contentResolver.openInputStream(imageUri)
                        if (inputStream != null) {
                            val imageEntry = ZipEntry("images/$imageName")
                            zipOutputStream.putNextEntry(imageEntry)
                            inputStream.copyTo(zipOutputStream)
                            inputStream.close()
                            zipOutputStream.closeEntry()
                        }
                    } catch (e: SecurityException) {
                        Log.e("Backup", "SecurityException during backup: ${e.message}")
                    } catch (e: Exception) {
                        Log.e("Backup", "Error during backup: ${e.message}")
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoJournalScreen(context: Context) {
    val currentDate = remember { mutableStateOf(Calendar.getInstance().time) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var exerciseData by remember { mutableStateOf(loadExerciseData(context, selectedDate) ?: ExerciseData(null)) }

    var imageUri: Uri? by remember { mutableStateOf(null) }
    val dataStore = remember { StoreData() }
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var enlargedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showButtons by remember { mutableStateOf(false) }
    var story by remember { mutableStateOf("") }
    var showStoryDialog by remember { mutableStateOf(false) }
    var editNotes by remember { mutableStateOf(false) }
    var newNotes by remember { mutableStateOf(exerciseData?.notes ?: "") }
    var editDifficulty by remember { mutableStateOf(false) }
    var newDifficulty by remember { mutableStateOf(exerciseData?.difficulty?.toString() ?: "") }

    val imageLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) {
        it?.let { uri ->
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            scope.launch {
                dataStore.storeImage(context, formatDate(currentDate.value), uri.toString())
            }
        }
    }

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let {
            scope.launch(Dispatchers.IO) {
                try {
                    val backupFile = File.createTempFile("backup", ".zip", context.cacheDir)
                    dataStore.backupData(context, backupFile)

                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        backupFile.inputStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    backupFile.delete()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    LaunchedEffect(selectedDate) {
        exerciseData = loadExerciseData(context, selectedDate) ?: ExerciseData(null)
        newNotes = exerciseData?.notes ?: ""
        newDifficulty = exerciseData?.difficulty?.toString() ?: ""
    }

    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            onClick = { showButtons = !showButtons },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(alignment = Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(id = R.drawable.hallway_24px),
                    contentDescription = "",
                    modifier = Modifier.size(24.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = stringResource(R.string.daily_photo))
                Spacer(modifier = Modifier.width(10.dp))
                AssistChip(
                    onClick = { Log.d("Assist chip", "Alpha 0413") },
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

        if (showButtons) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = {
                    currentDate.value = getPreviousDay(currentDate.value)
                    selectedDate = LocalDate.parse(formatDate(currentDate.value))
                }) {
                    Row {
                        Icon(
                            imageVector = Icons.Default.ArrowCircleLeft,
                            contentDescription = "Backward",
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(20.dp))
                Text(formatDate(currentDate.value))
                Spacer(modifier = Modifier.width(20.dp))
                Button(onClick = {
                    currentDate.value = getNextDay(currentDate.value)
                    selectedDate = LocalDate.parse(formatDate(currentDate.value))
                }) {
                    Row {
                        Icon(
                            imageVector = Icons.Default.ArrowCircleRight,
                            contentDescription = "Forward",
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }

            AsyncImage(
                model = imageUri,
                contentDescription = null,
                modifier = Modifier
                    .size(300.dp)
                    .clickable {
                        imageUri?.let {
                            enlargedImageUri = it
                            showDialog = true
                        }
                    },
                contentScale = ContentScale.Crop
            )

            Column(Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = newNotes,
                    onValueChange = { newNotes = it },
                    label = { Text(stringResource(R.string.story)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = editNotes,
                    trailingIcon = {
                        if (editNotes) {
                            IconButton(onClick = {
                                saveExerciseData(context, selectedDate, exerciseData?.copy(notes = newNotes, difficulty = newDifficulty.toIntOrNull()))
                                exerciseData = exerciseData?.copy(notes = newNotes, difficulty = newDifficulty.toIntOrNull())!!
                                editNotes = false
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Save Notes")
                            }
                        } else {
                            IconButton(onClick = { editNotes = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Notes")
                            }
                        }
                    }
                )
//                OutlinedTextField(
//                    value = newDifficulty,
//                    onValueChange = { newDifficulty = it },
//                    label = { Text("Difficulty") },
//                    modifier = Modifier.fillMaxWidth(),
//                    enabled = editDifficulty,
//                    trailingIcon = {
//                        if (editDifficulty) {
//                            IconButton(onClick = {
//                                saveExerciseData(context, selectedDate, exerciseData?.copy(notes = newNotes, difficulty = newDifficulty.toIntOrNull()))
//                                exerciseData = exerciseData?.copy(notes = newNotes, difficulty = newDifficulty.toIntOrNull())!!
//                                editDifficulty = false
//                            }) {
//                                Icon(Icons.Default.Check, contentDescription = "Save Difficulty")
//                            }
//                        } else {
//                            IconButton(onClick = { editDifficulty = true }) {
//                                Icon(Icons.Default.Edit, contentDescription = "Edit Difficulty")
//                            }
//                        }
//                    }
//                )
            }

            LazyRow(horizontalArrangement = Arrangement.Absolute.Center) {
                item {
                    Button(onClick = { imageLauncher.launch(arrayOf("image/*")) }) {
                        Row {
                            Icon(Icons.Default.Image, contentDescription = "Pick Image")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.pick_image))
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val calendar = Calendar.getInstance()
                                calendar.set(year, month, dayOfMonth)
                                currentDate.value = calendar.time
                            },
                            Calendar.getInstance().get(Calendar.YEAR),
                            Calendar.getInstance().get(Calendar.MONTH),
                            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }) {
                        Row {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Choose Date")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.choose_date))
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(onClick = {

                        backupLauncher.launch("photojournal_backup.zip")
                    }) {
                        Row {
                            Icon(Icons.Default.Backup, contentDescription = "Backup")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.backup2))
                        }
                    }

//                    Spacer(modifier = Modifier.width(8.dp))
//
//                    Button(onClick = {
//                        showStoryDialog = true
//                    }) {
//                        Row {
//                            Icon(Icons.Default.Edit, contentDescription = "Add Story")
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text("Add Story")
//                        }
//                    }
                }
            }
        }
    }

    LaunchedEffect(currentDate.value) {
        scope.launch {
            dataStore.getImage(context, formatDate(currentDate.value)).collect {
                imageUri = it?.let { Uri.parse(it) }
            }
            dataStore.getStory(context, formatDate(currentDate.value)).collect {
                story = it ?: ""
            }
        }
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            ZoomableImage(uri = enlargedImageUri)
        }
    }

//    if (showStoryDialog) {
//        Dialog(onDismissRequest = { showStoryDialog = false }) {
//            Column(Modifier.padding(16.dp)) {
//                TextField(
//                    value = story,
//                    onValueChange = { story = it },
//                    label = { Text("Story") },
//                    modifier = Modifier.fillMaxWidth()
//                )
//                Spacer(modifier = Modifier.height(16.dp))
//                Button(onClick = {
//                    scope.launch {
//                        dataStore.storeStory(context, formatDate(currentDate.value), story)
//                        showStoryDialog = false
//                    }
//                }) {
//                    Text("Save Story")
//                }
//            }
//        }
//    }
}

private fun saveExerciseData(context: Context, date: LocalDate, exerciseData: ExerciseData?) {
    if (exerciseData == null) return;
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = sharedPreferences.edit()
    val gson = Gson()
    val json = gson.toJson(exerciseData)
    editor.putString("photo_data_$date", json)
    editor.apply()
}

private fun loadExerciseData(context: Context, date: LocalDate): ExerciseData? {
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    val gson = Gson()
    val json = sharedPreferences.getString("photo_data_$date", null)

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

@Composable
fun ZoomableImage(uri: Uri?) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale *= zoomChange
        offset += offsetChange
    }

    AsyncImage(
        model = uri,
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .transformable(state = state)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            ),
        contentScale = ContentScale.Fit
    )
}

fun formatDate(date: Date): String {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return format.format(date)
}

fun getPreviousDay(date: Date): Date {
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar.add(Calendar.DAY_OF_MONTH, -1)
    return calendar.time
}

fun getNextDay(date: Date): Date {
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar.add(Calendar.DAY_OF_MONTH, 1)
    return calendar.time
}

@Preview(showBackground = true)
@Composable
fun PhotoJournalPreview() {
    MaterialTheme { PhotoJournalScreen(LocalContext.current) }
}