package app.log.weicheng.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.log.weicheng.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

data class LatestResponse(val latest: String, val availableDates: Map<String, String>)

data class ElementResponse(
    val date: String,
    val hasPhotos: Boolean,
    val appreciations: Map<String, String>,
    val photos: Map<String, Photo>,
    val memo: String
)

data class Photo(
    val title: String,
    val storyLines: String,
    val showcaseLink: String,
    val originalQualityLink: String
)

@Composable
fun justFetch(): ElementResponse? {
    var latestResponse by remember { mutableStateOf<LatestResponse?>(null) }
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var elementResponse by remember { mutableStateOf<ElementResponse?>(null) }

    // Fetch latest.txt and available dates
    LaunchedEffect(Unit) {
        latestResponse = fetchLatest()
        // Set the default date to the latest fetched date
        selectedDate = latestResponse?.latest
    }

    // Fetch element data when selectedDate changes
    LaunchedEffect(selectedDate) {
//        Log.d("debugg", selectedDate ?: "Selected date is null")
        if (selectedDate != null) {
            elementResponse = fetchElement(selectedDate!!)
        }
    }

    // Display UI based on the fetched data
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (latestResponse != null) {
            // Display date picker button
            selectedDate?.let {
                DateButton(
                    availableDates = latestResponse!!.availableDates,
                    onDateSelected = { date ->
                        selectedDate = date
                    },
                    currentSelection = it // Pass the current selection here
                )
            }

            // Update the UI when elementResponse is available
            if (elementResponse != null) {
                ElementCard(elementResponse!!)
            } else {
                // Loading or error UI
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
        } else {
            // Loading UI
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }
    }

    // Return the fetched ElementResponse
    return elementResponse
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElementCard(elementResponse: ElementResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        onClick = { /* Handle card click if needed */ }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Display photo information
//            elementResponse.photos.keys.forEachIndexed { index, key ->
//                val photo = elementResponse.photos[key]!!
//                PhotoCard(photo = photo, number = index + 1, total = elementResponse.photos.size)
//            }

            // Display date as the title
            Text(
                text = elementResponse.date,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp),
                fontFamily = fontFamilyTitle
            )

            // Display appreciations
            elementResponse.appreciations.forEach { (key, value) ->
                Text(text = "$key: $value", modifier = Modifier.padding(bottom = 4.dp), fontFamily = fontFamilyNotes, fontSize = 27.sp)
            }

            // Display memo
            Text(text = elementResponse.memo, modifier = Modifier.padding(top = 8.dp), fontFamily = fontFamilyContent, fontSize = 25.sp)
        }
    }
}

@Composable
fun PhotoCard(photo: Photo, number: Int, total: Int) {
    // Use the same PhotoCard composable with modifications to handle photo information
    RoundedCornerPhoto(
        title = photo.title,
        message = photo.storyLines,
        showcaseLink = photo.showcaseLink,
        originalQualityLink = photo.originalQualityLink,
        number = number,
        total = total
    )
}

suspend fun fetchLatest(): LatestResponse {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://weicheng.app/cms/weicheng_log/latest.txt")
        .build()

    return withContext(Dispatchers.IO) {
        try {
            val response: Response = client.newCall(request).execute()
            val body = response.body?.string() ?: throw IllegalStateException("Empty response")
            val json = JSONObject(body)

            val latest = json.getString("latest")
            val availableDatesJson = json.getJSONObject("available_dates")
            val availableDates = mutableMapOf<String, String>()

            availableDatesJson.keys().forEach { key ->
                availableDates[key] = availableDatesJson.getString(key)
            }

            LatestResponse(latest, availableDates)
        } catch (e: Exception) {
            throw IOException("Error fetching latest data", e)
        }
    }
}

suspend fun fetchElement(latest: String): ElementResponse {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://weicheng.app/cms/weicheng_log/$latest.json")
        .build()

    return withContext(Dispatchers.IO) {
        try {
            val response: Response = client.newCall(request).execute()
            val body = response.body?.string() ?: throw IllegalStateException("Empty response")
            val json = JSONObject(body)

            ElementResponse(
                date = json.getString("date"),
                hasPhotos = json.getBoolean("hasPhotos"),
                appreciations = parseAppreciations(json.getJSONObject("appreciations")),
                photos = parsePhotos(json.getJSONObject("photos")),
                memo = json.getString("memo")
            )
        } catch (e: Exception) {
            throw IOException("Error fetching element data", e)
        }
    }
}


fun parseAppreciations(json: JSONObject): Map<String, String> {
    val appreciations = mutableMapOf<String, String>()
    json.keys().forEach { key ->
        appreciations[key] = json.getString(key)
    }
    return appreciations
}

fun parsePhotos(json: JSONObject): Map<String, Photo> {
    val photos = mutableMapOf<String, Photo>()
    json.keys().forEach { key ->
        val photoJson = json.getJSONObject(key)
        photos[key] = Photo(
            title = photoJson.getString("title"),
            storyLines = photoJson.getString("story_lines"),
            showcaseLink = photoJson.getString("showcase_link"),
            originalQualityLink = photoJson.getString("original_quality_link")
        )
    }
    return photos
}

@Composable
fun DateButton(availableDates: Map<String, String>, onDateSelected: (String) -> Unit, currentSelection: String) {
    var showDatePicker by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { showDatePicker = true },
            modifier = Modifier.padding(0.dp)
        ) {
            Text(text = stringResource(R.string.change_date))
        }

        if (showDatePicker) {
            DatePicker(
                availableDates = availableDates,
                onDateSelected = {
                    onDateSelected(it)
                    showDatePicker = false
                },
                currentSelection = currentSelection // Pass the current selection here
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LovedDatePicker(){
    // Decoupled snackbar host state from scaffold state for demo purposes.
    val snackState = remember { SnackbarHostState() }
    val snackScope = rememberCoroutineScope()
    SnackbarHost(hostState = snackState, Modifier)
    val openDialog = remember { mutableStateOf(true) }
    // TODO demo how to read the selected date from the state.
    if (openDialog.value) {
        val datePickerState = rememberDatePickerState()
        val confirmEnabled = remember {
            derivedStateOf { datePickerState.selectedDateMillis != null }
        }
        DatePickerDialog(
            onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
                // button. If you want to disable that functionality, simply use an empty
                // onDismissRequest.
                openDialog.value = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                        snackScope.launch {
                            snackState.showSnackbar(
                                "Selected date timestamp: ${datePickerState.selectedDateMillis}"
                            )
                        }
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun DatePicker(availableDates: Map<String, String>, onDateSelected: (String) -> Unit, currentSelection: String) {
//    var selectedDate by remember { mutableStateOf(availableDates.values.first()) }
    var selectedDate by remember {
        mutableStateOf(currentSelection)
    }

    // Observe changes in availableDates and update selectedDate accordingly
    LaunchedEffect(availableDates) {
        selectedDate = currentSelection
    }

    AlertDialog(
        onDismissRequest = { onDateSelected(currentSelection) },
        title = { Text(text = stringResource(R.string.select_date)) },
        confirmButton = {
            Button(
                onClick = {
                    onDateSelected(selectedDate)
                },
            ) {
                Text(text = stringResource(R.string.confirm), color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    onDateSelected(currentSelection)
                },
            ) {
                Text(text = stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSecondary)
            }
        },
        text = {
            Column {
                availableDates.values.forEach { date ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                selectedDate = date
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = date,
                            color = if (selectedDate == date) MaterialTheme.colorScheme.primary else Color.Black
                        )
                    }
                    HorizontalDivider(color = Color.Gray)
                }
            }
        }
    )
}