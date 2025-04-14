package app.log.weicheng.ui

import CardSliderColorP
import ColorItem
import ElevatedCardColorP
import PhotoJournalScreen
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
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.preferencesDataStore
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
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import kotlin.random.Random

// Note: This is at the top level of the file, outside of any classes.
val Context.dataStore by preferencesDataStore("isBeta")

@kotlin.OptIn(ExperimentalFoundationApi::class)
@SuppressLint("CoroutineCreationDuringComposition", "NewApi")
@RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    context: Context
) {
    var result by remember { mutableStateOf<List<String>?>(null) }
    val scrollState = rememberScrollState()
    var isFetchSuccessful by remember { mutableStateOf(false) }
    dataStore = context.dataStore

    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isNetworkAvailable = isNetworkAvailable(connectivityManager)
    val isNotMeteredNetwork = isMeteredNetwork(connectivityManager)

    var selectedTime by remember { mutableStateOf(LocalDateTime.now()) }

    var showColorSettings = remember {  mutableStateOf(false) }
    var showTimeBlock = remember {  mutableStateOf(true) }
    var showTimeBlockSettings = remember {  mutableStateOf(false) }

    var saveFieldText = remember { mutableStateOf("") }
    var restoreFieldText = remember { mutableStateOf("") }

    var colorItems = remember {
        mutableStateOf(loadColorItemsFromSharedPreferences(context).map {
            ColorItem(it.uuid, it.hexCode, it.color, it.task)
        }.toMutableList())
    }

    var selectedTimes = remember { mutableStateListOf<String>() }
    val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    val savedTimes = sharedPref.getStringSet("selectedTimes", null)?.toMutableList() ?: mutableListOf()
    savedTimes.forEach { selectedTimes.add(it.toString()) }

    val cameraViewModel = remember { CameraPreviewViewModel() }

//    if (isNotMeteredNetwork) {
//        LaunchedEffect(Unit) {
//            withContext(Dispatchers.IO) {
//                result =
//                    fetchNewVersion("http://132.145.27.119:8026/cms/weicheng_log/version.txt", context)
//                isFetchSuccessful = result != null
//            }
//        }
//    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = scrollState)
    ) {
        Column(
            modifier = modifier,
//            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
            ) {
                val greeting by remember { mutableStateOf(getGreeting()) }
                Surface(
                    modifier = Modifier.padding(10.dp),
                ) {
                    Text(
                        text = stringResource(id = greeting),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
                Text(
                    text = stringResource(R.string.title_main),
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = fontFamilyTitle
//                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))
                )
                Spacer(modifier = Modifier.height(8.dp))
                val randomText = when (Random.nextInt(0, 10)) { // Assuming you have 10 sentences
                    0 -> stringResource(R.string.affirm_1)
                    1 -> stringResource(R.string.affirm_2)
                    2 -> stringResource(R.string.affirm_3)
                    3 -> stringResource(R.string.affirm_4)
                    4 -> stringResource(R.string.affirm_5)
                    5 -> stringResource(R.string.affirm_6)
                    6 -> stringResource(R.string.affirm_7)
                    7 -> stringResource(R.string.affirm_8)
                    8 -> stringResource(R.string.affirm_9)
                    9 -> stringResource(R.string.affirm_10)
                    else -> "" // Handle any additional cases
                }

                CurrentTimeText();

//                Text(
//                    text = randomText,
//                    fontSize = 20.sp,
//                    textAlign = TextAlign.Center,
//                    fontFamily = fontFamily,
//                )

//                if (isNotMeteredNetwork) {
//                    if (isFetchSuccessful) {
//                        // File fetched successfully
//                        ResultedUpdatesResults(
//                            result,
//                            false,
//                            result?.get(3).toBoolean(),
//                            context = LocalContext.current
//                        )
//                        var responses = justFetch()
//                        if (responses != null) {
//                            PhotoCard(responses)
//                        }
//                    } else {
//                        // File fetch unsuccessful
//                        AssistChipConstructor(
//                            text = stringResource(R.string.server_disconnected),
//                            icon = Icons.Filled.CloudOff
//                        )
//                        InternetNotConnectedPage(
//                            stringResource(R.string.no_internet_connection),
//                            stringResource(R.string.check_internet_text),
//                            Icons.Default.WifiOff
//                        )
//                    }
//                } else {
//                    if(isNetworkAvailable) {
//                        AssistChipConstructor(
//                            text = stringResource(R.string.cellular_or_metered_network_detected),
//                            icon = Icons.Filled.WifiOff
//                        )
//                        InternetNotConnectedPage(
//                            stringResource(R.string.no_cellular_allowed),
//                            stringResource(R.string.no_cellular_allowed_description),
//                            Icons.Default.SignalCellularNodata
//                        )
//                    } else {
                        // File fetch unsuccessful
//                        AssistChipConstructor(
//                            text = stringResource(R.string.server_disconnected),
//                            icon = Icons.Filled.CloudOff
//                        )
//                        InternetNotConnectedPage(
//                            stringResource(R.string.no_internet_connection),
//                            stringResource(R.string.check_internet_text),
//                            Icons.Default.WifiOff
//                        )
//                    }
//                }

                Spacer(modifier = Modifier.height(10.dp))

//                Text(
//                    text = stringResource(R.string.new_year_notice),
//                    fontSize = 20.sp,
//                    textAlign = TextAlign.Center, // Center the text horizontally
//                    fontFamily = fontFamily // Example of specifying a font family
//                )

//                Spacer(modifier = Modifier.height(16.dp))

                // AI
//                LiveHttpInput();

                RunningAndGymExercise(context, true);

                Spacer(modifier = Modifier.height(10.dp));

                PhotoJournalScreen(context)

                Spacer(modifier = Modifier.height(10.dp));

                Button(onClick = { showTimeBlock.value = !showTimeBlock.value },
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                ) {
                    Row (
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Time Block", // 用于无障碍访问
                            modifier = Modifier.size(24.dp), // 设置图标大小
//                        tint = Color.Unspecified // 使用默认颜色或指定颜色
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text= stringResource(R.string.time_block_registering))
                        Spacer(modifier = Modifier.width(10.dp))
                        AssistChip(
                            onClick = { Log.d("Assist chip", "Now in Stable") },
                            label = { Text("Stable v1.2", color = MaterialTheme.colorScheme.onPrimary) },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = "Stable",
                                    Modifier.size(AssistChipDefaults.IconSize),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        )
                    }
                }

                if(showTimeBlock.value) {
                    ElevatedCard(
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 3.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .height(500.dp)
                        ) {
                            InfiniteTimeGrid(LocalContext.current)
                        }
                    }

                    CardSliderColorP(LocalContext.current)
                    Button(
                        onClick = { showTimeBlockSettings.value = !showTimeBlockSettings.value },
                        modifier = Modifier.fillMaxWidth(0.9f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                    ) {
                        Row {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = "Time Block Backup & Restore",
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = stringResource(R.string.time_block_backup_restore))
                        }
                    }

                    if (showTimeBlockSettings.value) {
                        TextField(value = saveFieldText.value, onValueChange = {

                        }, modifier = Modifier)

                        TextField(value = restoreFieldText.value, onValueChange = {
                            restoreFieldText.value = it
                        }, modifier = Modifier)


                        Button(onClick = {
                            val gson = Gson()
                            val jsonArray = JsonArray()
                            colorItems.value.forEach { item ->
                                jsonArray.add(gson.toJsonTree(item))
                            }
                            val jsonString = gson.toJson(jsonArray)
                            saveFieldText.value = jsonString
                        }) {
                            Text(stringResource(R.string.pop_up_color_configurations))
                        }

                        Button(onClick = {
                            saveFieldText.value = ""
                            val gson = Gson()
                            val jsonArray = JsonArray()
                            var arrr = mutableStateListOf<String>()
                            selectedTimes.forEach { item ->
                                if (!arrr.contains(item)) {
                                    jsonArray.add(gson.toJsonTree(item))
                                } else {
                                    arrr.add(item)
                                }
                            }
                            val jsonString = gson.toJson(jsonArray)
                            saveFieldText.value = jsonString
                        }) {
                            Text(stringResource(R.string.pop_up_time_block_data))
                        }

                        Button(onClick = {
                            val sharedPref: SharedPreferences =
                                PreferenceManager.getDefaultSharedPreferences(context)
                            with(sharedPref.edit()) {
                                putString("colorItems", restoreFieldText.value)
                                apply()
                            }
                        }) {
                            Text(stringResource(R.string.restore_color_configurations))
                        }

                        Button(onClick = {
                            val sharedPref: SharedPreferences =
                                PreferenceManager.getDefaultSharedPreferences(context)
                            val gson = Gson()
                            val type = object : TypeToken<List<String>>() {}.type
                            val stringList: List<String> =
                                gson.fromJson(restoreFieldText.value, type)

                            // 将 List<String> 转换为 Set<String>
                            val stringSet: Set<String> = stringList.toSet()
                            with(sharedPref.edit()) {
                                putStringSet("selectedTimes", stringSet)
                                apply()
                            }
                        }) {
                            Text(stringResource(R.string.restore_time_block_data))
                        }
                    }

                    Button(
                        onClick = { showColorSettings.value = !showColorSettings.value },
                        modifier = Modifier.fillMaxWidth(0.9f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                    ) {
                        Row {
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = "Color Settings", // 用于无障碍访问
                                modifier = Modifier.size(24.dp), // 设置图标大小
//                        tint = Color.Unspecified // 使用默认颜色或指定颜色
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = stringResource(R.string.color_label_settings))
                        }
                    }

                    if (showColorSettings.value) {
                        ElevatedCardColorP(LocalContext.current)
                    }
                }

//                CameraPreviewScreen(viewModel = cameraViewModel, modifier = Modifier)

//                RecordingScreen()

//                Text(
//                    text = stringResource(R.string.personal_motto),
//                    fontSize = 20.sp,
//                    textAlign = TextAlign.Center, // Center the text horizontally
//                    fontFamily = fontFamily // Example of specifying a font family
//                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.contributions),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center, // Center the text horizontally
                    fontFamily = fontFamily // Example of specifying a font family
                )

                Spacer(modifier = Modifier.height(16.dp))

//                Text(
//                    text = stringResource(R.string.acknowledgments),
//                    fontSize = 20.sp,
//                    textAlign = TextAlign.Center, // Center the text horizontally
//                    fontFamily = fontFamily // Example of specifying a font family
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
                val pickPictureLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.GetContent()
//                    ActivityResultContracts.OpenDocument()
                ) { imageUri ->
                    if (imageUri != null) {
                        // Update the state with the Uri
                    }
                }

                val activityResultLauncher =
                    rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestMultiplePermissions())
                    { permissions ->
                        // Handle Permission granted/rejected
                        var permissionGranted = true
                        permissions.entries.forEach {
                            if (it.value == false)
//                                it.key in REQUIRED_PERMISSIONS &&
                                permissionGranted = false
                        }
                        if (!permissionGranted) {
                            Log.d("Permission Camera", "Permission Denied.")
//                            Toast.makeText(baseContext,
//                                "Permission request denied",
//                                Toast.LENGTH_SHORT).show()
                        } else {
                            Log.d("Permission Camera", "Permission Granted.")
                        }
                    }

//                Button(onClick = {
//                    // In your button's click
//                    pickPictureLauncher.launch("text/plain")
//                }, modifier = Modifier.padding(top = 16.dp)) {
//                    Text("test file picker")
//                }

//                Button(onClick = {
//                    // In your button's click
//
//                }, modifier = Modifier.padding(top = 16.dp)) {
//                    Text("test camera")
//                }

                Text(
                    text = stringResource(R.string.last_sentence),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center, // Center the text horizontally
                    fontFamily = fontFamily // Example of specifying a font family
                )

                Spacer(modifier = Modifier.height(16.dp))

//                Text(
//                    text = stringResource(R.string.opensourceinfo),
//                    fontSize = 20.sp,
//                    textAlign = TextAlign.Center, // Center the text horizontally
//                    fontFamily = fontFamily // Example of specifying a font family
//                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.weicheng_internal_tool_2025),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center, // Center the text horizontally
                    fontFamily = fontFamily // Example of specifying a font family
                )

                Spacer(modifier = Modifier.height(10.dp))

                LiveHttpInput();

                Spacer(modifier = Modifier.height(8.dp))

//                Text(
//                    text = "HLG Supported: " + isHLGSupported("0").toString(),
//                    fontSize = 20.sp,
//                    textAlign = TextAlign.Center, // Center the text horizontally
//                    fontFamily = fontFamily // Example of specifying a font family
//                )
//
//                Spacer(modifier = Modifier.height(8.dp))

            }
        }
    }
}

val localDateTimeSaver: Saver<LocalDateTime, String> = Saver(
    save = { it.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) },
    restore = { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }
)

@kotlin.OptIn(ExperimentalFoundationApi::class)
@Preview(showBackground = true)
@Composable
fun InfiniteTimeGridPreview() {
    MaterialTheme {
        InfiniteTimeGrid(LocalContext.current)
    }
}

fun parseCustomDateTime(input: String): LocalDateTime? {
    // 假设日期时间部分和自定义后缀之间总是由 'T' 和随后的非数字字符分隔
    val dateTimePattern = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}".toRegex()
    val matchResult = dateTimePattern.find(input)

    return matchResult?.value?.let { dateTimeString ->
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault())
        // 注意：这里我们假设时区是系统默认时区，您可能需要根据实际情况调整
        LocalDateTime.parse(dateTimeString, formatter)
    }
}

@Composable
fun CurrentTimeText() {
    val currentTime = remember { mutableStateOf(getCurrentTime()) }
    val scope = rememberCoroutineScope()

    // 使用 LaunchedEffect 来启动一个协程，每秒更新时间
    LaunchedEffect(Unit) {
        scope.launch {
            while (true) {
                delay(1000L) // 延迟1秒
                currentTime.value = getCurrentTime()
            }
        }
    }

    Text(
        text = currentTime.value,
        fontSize = 24.sp,
        modifier = Modifier.fillMaxSize(),
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold
    )
}

private fun getCurrentTime(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return dateFormat.format(Date())
}

fun isMeteredNetwork(connectivityManager: ConnectivityManager): Boolean {
    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)
    return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
}

fun isNetworkAvailable(connectivityManager: ConnectivityManager): Boolean {
    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)
    return capabilities != null &&
            (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
}


@Composable
fun InternetNotConnectedPage(largeText: String, contentText: String, imageVector: ImageVector) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = stringResource(id = R.string.no_internet_connection),
            modifier = Modifier
                .size(120.dp)
                .padding(16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = largeText,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(8.dp)
        )
        Text(
            text = contentText,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun ResultedUpdatesResults(result: List<String>?, showButton: Boolean, chooseBeta: Boolean, context: Context){
    if((result?.get(0)?.take(6) ?: 231222) == stringResource(id = R.string.version)){
        if(!chooseBeta && stringResource(id = R.string.build_type)=="beta"){
            if((result?.get(0)?.endsWith("beta") == false)){
                AssistChipConstructor(
                    text = stringResource(R.string.server_connected_updates_available),
                    icon = Icons.Filled.Download
                )
                if(showButton){
                    Button(onClick = {
                        val webIntent: Intent =
                            Intent(Intent.ACTION_VIEW, Uri.parse(result.get(1) ?: ""))
                        try {
                            ContextCompat.startActivity(context, webIntent, null)
                        } catch (e: ActivityNotFoundException) {
                            // Define what your app should do if no activity can handle the intent.
                        }
                    }) {
                        Text(stringResource(R.string.download_upgrade_to_the_latest_stable))
                    }
                }
            }else{
                AssistChipConstructor(
                    text = stringResource(R.string.server_connected_downgrades_available),
                    icon = Icons.Filled.Restore
                )
                if(showButton){
                    Button(onClick = {
                        val webIntent: Intent =
                            Intent(Intent.ACTION_VIEW, Uri.parse(result?.get(1) ?: ""))
                        try {
                            ContextCompat.startActivity(context, webIntent, null)
                        } catch (e: ActivityNotFoundException) {
                            // Define what your app should do if no activity can handle the intent.
                        }
                    }) {
                        Text(stringResource(R.string.download_reinstall_the_latest_version))
                    }
                }
            }
        }else if((result?.get(0)?.endsWith("beta") == false) && stringResource(id = R.string.build_type)=="beta"){
            AssistChipConstructor(
                text = stringResource(R.string.server_connected_updates_available),
                icon = Icons.Filled.Download
            )
            if(showButton){
                Button(onClick = {
                    val webIntent: Intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(result.get(1) ?: ""))
                    try {
                        ContextCompat.startActivity(context, webIntent, null)
                    } catch (e: ActivityNotFoundException) {
                        // Define what your app should do if no activity can handle the intent.
                    }
                }) {
                    Text(stringResource(R.string.download_the_latest_version))
                }
            }
        }else{
            AssistChipConstructor(
                text = stringResource(R.string.server_connected_running_the_latest_version),
                icon = Icons.Filled.CheckCircle
            )
        }
    }else{
        if((result?.get(0)?.take(6)?.toInt() ?: 231222) < stringResource(id = R.string.version).toInt() && (stringResource(R.string.build_type) == "beta")){
            AssistChipConstructor(
                text = stringResource(R.string.server_connected_downgrades_available),
                icon = Icons.Filled.Restore
            )
            if(showButton){
                Button(onClick = {
                    val webIntent: Intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(result?.get(1) ?: ""))
                    try {
                        ContextCompat.startActivity(context, webIntent, null)
                    } catch (e: ActivityNotFoundException) {
                        // Define what your app should do if no activity can handle the intent.
                    }
                }) {
                    Text(stringResource(R.string.download_reinstall_the_latest_version))
                }
            }
        }else{
            AssistChipConstructor(
                text = stringResource(R.string.server_connected_updates_available),
                icon = Icons.Filled.Download
            )
            if(showButton){
                Button(onClick = {
                    val webIntent: Intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(result?.get(1) ?: ""))
                    try {
                        ContextCompat.startActivity(context, webIntent, null)
                    } catch (e: ActivityNotFoundException) {
                        // Define what your app should do if no activity can handle the intent.
                    }
                }) {
                    Text(stringResource(R.string.download_the_latest_version))
                }
            }
        }
    }
}

suspend fun fetchNewVersion(url: String, context: Context): List<String>? {
    if (!isInternetLocationReachable(url)) {
        return null
    }

    try {
        val websiteUrl = URL(url)
        val connection = websiteUrl.openConnection() as HttpURLConnection

        // Set a timeout for the connection (optional)
        connection.connectTimeout = 5000 // 5 seconds
        connection.readTimeout = 5000 // 5 seconds

        dataStore = context.dataStore
        var fetchedBeta = readSettings("isBeta") == "true"

        val inputStream = connection.inputStream
        val reader = BufferedReader(InputStreamReader(inputStream))

        var line: String?
        val content = StringBuilder()

        while (reader.readLine().also { line = it } != null) {
            content.append(line).append("\n")
        }

        reader.close()
        inputStream.close()

        if (content.isEmpty()) {
            // Handle empty content
            return null
        }

        // Parse JSON content
        val jsonObject = JSONObject(content.toString())

        if (jsonObject.length() == 0) {
            // Handle empty JSON object
            return null
        }

        // Extract information
        val versionsObject = jsonObject.getJSONObject("versions")
        var latestVersionObject = versionsObject.getJSONObject(jsonObject.getString("latest"))

        if(fetchedBeta){
            latestVersionObject = versionsObject.getJSONObject(jsonObject.getString("latest_beta"))
        }

        val latestVersion = latestVersionObject.getString("version")
        val downloadLink = latestVersionObject.getString("download")
        val updateDescription = latestVersionObject.getString("updates")

        // Create a list with the extracted information
        return listOf(latestVersion, downloadLink, updateDescription, fetchedBeta.toString())

    } catch (e: IOException) {
        // Handle network or IO errors
        return null
    } catch (e: JSONException) {
        // Handle JSON parsing errors
        return null
    }
}

@OptIn(UnstableApi::class)
@Composable
fun AssistChipConstructor(text: String, icon: ImageVector) {
    AssistChip(
        onClick = { },
        label = { Text(text) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = text,
                Modifier.size(AssistChipDefaults.IconSize)
            )
        }
    )
}

private suspend fun isInternetLocationReachable(urlString: String): Boolean {
    return try {
        val url = URL(urlString)
        val connection = withContext(Dispatchers.IO) {
            url.openConnection()
        } as HttpURLConnection
        connection.connectTimeout = 3000 // 3 seconds
        connection.requestMethod = "HEAD"

        val responseCode = connection.responseCode

        responseCode in 200..299
    } catch (e: IOException) {
        false
    }
}

fun getGreeting(): Int {
    return when (LocalTime.now()) {
        in LocalTime.MIDNIGHT..LocalTime.of(5, 59) -> R.string.good_night
        in LocalTime.of(6, 0)..LocalTime.of(8, 59) -> R.string.good_morning_early
        in LocalTime.of(9, 0)..LocalTime.of(11, 59) -> R.string.good_morning_late
        in LocalTime.of(12, 0)..LocalTime.of(17, 59) -> R.string.good_afternoon
        in LocalTime.of(18, 0)..LocalTime.of(23, 59) -> R.string.good_evening
        else -> R.string.good_morning_early // Strange Corner Case?
    }
}

@Composable
private fun isTenBitProfileSupported(cameraId: String): Boolean {
    val context = LocalContext.current
//    val cameraManager = context.getSystemService(CAMERA_SERVICE) as CameraManager
//    val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)
//    val availableCapabilities = cameraCharacteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
//    for (capability in availableCapabilities!!) {
//        if (capability == CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_DYNAMIC_RANGE_TEN_BIT) {
//            return true
//        }
//    }
    return false
}

@Composable
@RequiresApi(api = 33)
private fun isHLGSupported(cameraId: String): Boolean {
//    if (isTenBitProfileSupported(cameraId)) {
//        val context = LocalContext.current
//        val cameraManager = context.getSystemService(CAMERA_SERVICE) as CameraManager
//        val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)
//        val availableProfiles = cameraCharacteristics
//            .get(CameraCharacteristics.REQUEST_AVAILABLE_DYNAMIC_RANGE_PROFILES)!!
//            .supportedProfiles
//
//        // Checks for the desired profile, in this case HLG10
//        return availableProfiles.contains(DynamicRangeProfiles.HLG10)
//    }
    return false;
}