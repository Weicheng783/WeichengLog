package app.log.weicheng

import ColorItem
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.preference.PreferenceManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleLeft
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import loadColorItemsFromSharedPreferences
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@OptIn(UnstableApi::class)
@Composable
fun InfiniteTimeGrid(context: Context) {
    val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    val selectedTimes = remember { mutableStateListOf<String>() }
    var selectedColorIndex by remember {
        mutableStateOf(
            with(context) {
                val defaultIndex = 0
                val selectedIndexKey = "selectedIndex"
                val selectedColorIndex = sharedPref.getInt(selectedIndexKey, defaultIndex)
                selectedColorIndex
            }
        )
    }

    var colorItems = remember {
        mutableStateOf(loadColorItemsFromSharedPreferences(context).map {
            ColorItem(it.uuid, it.hexCode, it.color, it.task)
        }.toMutableList())
    }

    val savedTimes =
        sharedPref.getStringSet("selectedTimes", null)?.toMutableList() ?: mutableListOf()
    savedTimes.forEach { selectedTimes.add(it.toString()) }

    val currentTime = LocalDateTime.now()
    var startTime = currentTime.minus(0, ChronoUnit.MONTHS).truncatedTo(ChronoUnit.DAYS)
    var timeList = generateSequence(startTime) { it.plusMinutes(15) }.take(96).toList()
    var addingAmount = remember { mutableStateOf(0L) }
    var dateTextSelected: String by remember {
        mutableStateOf(
            currentTime.toLocalDate().toString()
        )
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        if (uri != null) {
            exportData(context, uri)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            importData(context, uri)
        }
    }

    LazyRow(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            Spacer(modifier = Modifier.width(30.dp))

            Button(
                onClick = {
                    addingAmount.value--
                    dateTextSelected =
                        startTime.plus(addingAmount.value, ChronoUnit.DAYS).truncatedTo(
                            ChronoUnit.DAYS
                        ).toLocalDate().toString()
                },
                modifier = Modifier,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
            ) {
                Row {
                    Icon(
                        imageVector = Icons.Default.ArrowCircleLeft,
                        contentDescription = "Backward",
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(text = dateTextSelected)

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = {
                    addingAmount.value++
                    dateTextSelected =
                        startTime.plus(addingAmount.value, ChronoUnit.DAYS).truncatedTo(
                            ChronoUnit.DAYS
                        ).toLocalDate().toString()
                },
                modifier = Modifier,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
            ) {
                Row {
                    Icon(
                        imageVector = Icons.Default.ArrowCircleRight,
                        contentDescription = "Forward",
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(onClick = { exportLauncher.launch("mood_stress_data.txt") }) {
                Icon(imageVector = Icons.Default.Save, contentDescription = "Export")
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(onClick = { importLauncher.launch(arrayOf("text/plain")) }) {
                Icon(imageVector = Icons.Default.UploadFile, contentDescription = "Import")
            }
        }
    }

    DisposableEffect(selectedTimes) {
        onDispose {
            val timesSet = selectedTimes.map { it.toString() }.toSet()
            with(sharedPref.edit()) {
                putStringSet("selectedTimes", timesSet)
                apply()
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(timeList.size) { index ->
            val gridTime = timeList[index]
            var isSelected = false
            var colorItem: ColorItem? = null
            var moodText: String? = null
            var moodIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
            var stressLevel: Int? = null

            colorItems.value.find { colorItem ->
                val fullKey = dateTextSelected + "T" + gridTime.toLocalTime()
                    .toString() + "ID" + colorItem.uuid
                if (selectedTimes.contains(fullKey)) {
                    isSelected = true
                    val moodData = sharedPref.getString(fullKey, null)
                    if (moodData != null) {
                        val parts = moodData.split("|||")
                        if (parts.size >= 3) {
                            moodText = parts[0]
                            moodIcon = when (parts[1]) {
                                "SentimentVeryDissatisfied" -> Icons.Default.SentimentVeryDissatisfied
                                "SentimentDissatisfied" -> Icons.Default.SentimentDissatisfied
                                "SentimentNeutral" -> Icons.Default.SentimentNeutral
                                "SentimentSatisfied" -> Icons.Default.SentimentSatisfied
                                "SentimentVerySatisfied" -> Icons.Default.SentimentVerySatisfied
                                else -> Icons.Default.SentimentNeutral
                            }
                            stressLevel = parts[2].toIntOrNull()
                        }
                    }
                    true
                } else {
                    false
                }
            }?.let { selectedColorItem ->
                colorItem = selectedColorItem
            }

            var lastClickTime by remember { mutableStateOf(0L) }
            var showMoodDialog by remember { mutableStateOf(false) }
            var currentMoodText by remember { mutableStateOf(moodText ?: "") }
            var currentMoodIcon by remember { mutableStateOf(moodIcon ?: Icons.Default.SentimentNeutral) }
            var currentStressLevel by remember { mutableStateOf(stressLevel ?: 0) }

            Box(
                modifier = Modifier.fillMaxWidth(fraction = 0.25f)
            ) {
                Button(
                    onClick = {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastClickTime < 300) {
                            showMoodDialog = true
                            lastClickTime = 0
                        } else {
                            lastClickTime = currentTime
                            android.os.Handler().postDelayed({
                                if (System.currentTimeMillis() - lastClickTime >= 300 && lastClickTime != 0L) {
                                    selectedColorIndex = sharedPref.getInt("selectedIndex", 0)
                                    if (isSelected) {
                                        selectedTimes.remove(
                                            dateTextSelected + "T" + gridTime.toLocalTime()
                                                .toString() + "ID" + colorItem?.uuid
                                        )
                                    } else {
                                        if (!selectedTimes.contains(
                                                dateTextSelected + "T" + gridTime.toLocalTime()
                                                    .toString() + "ID" + colorItems.value[selectedColorIndex].uuid
                                            )
                                        ) {
                                            selectedTimes.add(
                                                dateTextSelected + "T" + gridTime.toLocalTime()
                                                    .toString() + "ID" + colorItems.value[selectedColorIndex].uuid
                                            )
                                        }
                                    }
                                    Log.d("selectedTimes", selectedTimes.toString())
                                    val timesSet = selectedTimes.map { it.toString() }.toSet()
                                    with(sharedPref.edit()) {
                                        putStringSet("selectedTimes", timesSet)
                                        apply()
                                    }
                                    lastClickTime = 0
                                }

                            }, 300)

                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) colorItem?.color ?: MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primaryContainer, // Use Material3 color scheme
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    shape = RectangleShape,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${
                                gridTime.hour.toString().padStart(2, '0')
                            }:${gridTime.minute.toString().padStart(2, '0')}",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                        )
                    if (moodIcon != null || stressLevel != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (moodIcon != null) {
                                Icon(
                                    modifier = Modifier.size(16.dp),
                                    imageVector = moodIcon ?: Icons.Default.SentimentNeutral,
                                    contentDescription = "Mood Icon",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            if(stressLevel != null){
                                Text(text = "(${stressLevel})", color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                    if(isSelected){
                        Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(colorItem?.color ?: MaterialTheme.colorScheme.secondaryContainer))
                    }
                }
            }

            if (showMoodDialog) {
                AlertDialog(
                    onDismissRequest = { showMoodDialog = false },
                    title = { Text("Enter Your Mood") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = currentMoodText,
                                onValueChange = { currentMoodText = it },
                                label = { Text("Why are you feeling this way?") }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SentimentVeryDissatisfied,
                                    contentDescription = "Very Dissatisfied",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(4.dp)
                                        .clickable {
                                            currentMoodIcon =
                                                Icons.Default.SentimentVeryDissatisfied
                                        },
                                    tint = if (currentMoodIcon == Icons.Default.SentimentVeryDissatisfied) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                                Icon(
                                    imageVector = Icons.Default.SentimentDissatisfied,
                                    contentDescription = "Dissatisfied",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(4.dp)
                                        .clickable {
                                            currentMoodIcon = Icons.Default.SentimentDissatisfied
                                        },
                                    tint = if (currentMoodIcon == Icons.Default.SentimentDissatisfied) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                                Icon(
                                    imageVector = Icons.Default.SentimentNeutral,
                                    contentDescription = "Neutral",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(4.dp)
                                        .clickable {
                                            currentMoodIcon = Icons.Default.SentimentNeutral
                                        },
                                    tint = if (currentMoodIcon == Icons.Default.SentimentNeutral) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                                Icon(
                                    imageVector = Icons.Default.SentimentSatisfied,
                                    contentDescription = "Satisfied",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(4.dp)
                                        .clickable {
                                            currentMoodIcon = Icons.Default.SentimentSatisfied
                                        },
                                    tint = if (currentMoodIcon == Icons.Default.SentimentSatisfied) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                                Icon(
                                    imageVector = Icons.Default.SentimentVerySatisfied,
                                    contentDescription = "Very Satisfied",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(4.dp)
                                        .clickable {
                                            currentMoodIcon =
                                                Icons.Default.SentimentVerySatisfied
                                        },
                                    tint = if (currentMoodIcon == Icons.Default.SentimentVerySatisfied) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()){
                                Text("Stress Level: ${currentStressLevel}")
                            }
                            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()){
                                Button(onClick = { if(currentStressLevel >0) currentStressLevel-- }) {
                                    Text("-")
                                }
                                Button(onClick = { if(currentStressLevel < 10) currentStressLevel++ }) {
                                    Text("+")
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            val fullKey = dateTextSelected + "T" + gridTime.toLocalTime()
                                .toString() + "ID" + colorItem?.uuid
                            val moodData = "$currentMoodText|||${currentMoodIcon.name.removePrefix("Icons.Default.")}|||$currentStressLevel"
                            with(sharedPref.edit()) {
                                putString(fullKey, moodData)
                                apply()
                            }
                            showMoodDialog = false
                            selectedTimes.add(fullKey)
                        }) {
                            Text("Finish")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showMoodDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
}

fun exportData(context: Context, uri: Uri) {
    val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    val selectedTimes = sharedPref.getStringSet("selectedTimes", emptySet()) ?: emptySet<String>()

    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            selectedTimes.forEach { key ->
                val moodData = sharedPref.getString(key, null)
                if (moodData != null) {
                    outputStream.write("$key|||$moodData\n".toByteArray())
                }
            }
        }
    } catch (e: Exception) {
        Log.e("ExportData", "Error exporting data: ${e.message}")
    }
}

fun importData(context: Context, uri: Uri) {
    val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = sharedPref.edit()

    try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val parts = line?.split("|||", limit = 2)
                    if (parts != null && parts.size == 2) {
                        editor.putString(parts[0], parts[1])
                    }
                }
            }
        }
        editor.apply()
    } catch (e: Exception) {
        Log.e("ImportData", "Error importing data: ${e.message}")
    }
}