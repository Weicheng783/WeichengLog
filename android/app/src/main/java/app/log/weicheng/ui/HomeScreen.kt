package app.log.weicheng.ui

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SignalCellularNodata
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.createDataStore
import androidx.media3.common.util.UnstableApi
import app.log.weicheng.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalTime
import kotlin.random.Random

@SuppressLint("CoroutineCreationDuringComposition")
@RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    var result by remember { mutableStateOf<List<String>?>(null) }
    val scrollState = rememberScrollState()
    var isFetchSuccessful by remember { mutableStateOf(false) }
    var context = LocalContext.current
    dataStore = LocalContext.current.createDataStore(name = "isBeta")

    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isNetworkAvailable = isNetworkAvailable(connectivityManager)
    val isNotMeteredNetwork = isMeteredNetwork(connectivityManager)


    if (isNotMeteredNetwork) {
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                result =
                    fetchNewVersion("https://weicheng.app/cms/weicheng_log/version.txt", context)
                isFetchSuccessful = result != null
            }
        }
    }

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

                Text(
                    text = randomText,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = fontFamily,
                )

                if (isNotMeteredNetwork) {
                    if (isFetchSuccessful) {
                        // File fetched successfully
                        ResultedUpdatesResults(
                            result,
                            false,
                            result?.get(3).toBoolean(),
                            context = LocalContext.current
                        )
                        var responses = justFetch()
                        if (responses != null) {
                            PhotoCard(responses)
                        }
                    } else {
                        // File fetch unsuccessful
                        AssistChipConstructor(
                            text = stringResource(R.string.server_disconnected),
                            icon = Icons.Filled.CloudOff
                        )
                        InternetNotConnectedPage(
                            stringResource(R.string.no_internet_connection),
                            stringResource(R.string.check_internet_text),
                            Icons.Default.WifiOff
                        )
                    }
                } else {
                    if(isNetworkAvailable) {
                        AssistChipConstructor(
                            text = stringResource(R.string.cellular_or_metered_network_detected),
                            icon = Icons.Filled.WifiOff
                        )
                        InternetNotConnectedPage(
                            stringResource(R.string.no_cellular_allowed),
                            stringResource(R.string.no_cellular_allowed_description),
                            Icons.Default.SignalCellularNodata
                        )
                    } else {
                        // File fetch unsuccessful
                        AssistChipConstructor(
                            text = stringResource(R.string.server_disconnected),
                            icon = Icons.Filled.CloudOff
                        )
                        InternetNotConnectedPage(
                            stringResource(R.string.no_internet_connection),
                            stringResource(R.string.check_internet_text),
                            Icons.Default.WifiOff
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.contributions),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center, // Center the text horizontally
                    fontFamily = fontFamily // Example of specifying a font family
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.acknowledgments),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center, // Center the text horizontally
                    fontFamily = fontFamily // Example of specifying a font family
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.last_sentence),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center, // Center the text horizontally
                    fontFamily = fontFamily // Example of specifying a font family
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.opensourceinfo),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center, // Center the text horizontally
                    fontFamily = fontFamily // Example of specifying a font family
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
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

        dataStore = context.createDataStore(name = "isBeta")
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

@androidx.annotation.OptIn(UnstableApi::class)
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