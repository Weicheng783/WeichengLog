package app.log.weicheng.ui

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import androidx.navigation.NavController
import app.log.weicheng.MainActivity
import app.log.weicheng.R
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


lateinit var dataStore: DataStore<Preferences>
suspend fun saveSettings(key: String, value: String){
    val dataStoreKey = preferencesKey<String>(key)
    dataStore.edit{settings ->
        settings[dataStoreKey] = value
    }
}

suspend fun readSettings(key: String): String? {
    val dataStoreKey = preferencesKey<String>(key)
    val preferences = dataStore.data.first()
    return preferences[dataStoreKey]
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var selectedLanguage by remember { mutableStateOf(supportedLanguages.first()) }
    dataStore = LocalContext.current.createDataStore(name = "isBeta")
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val temp = readSettings("language")
            if(temp != null){
                if(temp == "en"){
                    selectedLanguage = Language("en", "English/英文")
                }else{
                    selectedLanguage = Language("zh", "Chinese (Simplified)/简体中文")
                }
            }
        }
    }

    Scaffold(
        topBar = {
//            TopAppBar(
//                title = { Text(text = "Settings") },
//                backgroundColor = MaterialTheme.colorScheme.primarySurface
//            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                LanguagePicker(selectedLanguage) { language ->
                    selectedLanguage = language
                }
                Spacer(modifier = Modifier.height(16.dp))
//                Section1()
//                Spacer(modifier = Modifier.height(16.dp))
//                Section3()
//                Spacer(modifier = Modifier.height(16.dp))
                Section4()
                Spacer(modifier = Modifier.height(16.dp))
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(R.drawable.cms)
                    .build(),
                    contentDescription = "cms",
                    modifier = Modifier.fillMaxSize()
                )
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(R.drawable.main)
                        .build(),
                    contentDescription = "main",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    )
}

@Composable
fun Section1() {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Theme", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            // Add theme picker item here
        }
    }
}

@Composable
fun Section3() {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "General", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            // Add general settings items here
            Button(onClick = {  }) {
                Text(text = "Clickable")
            }
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun Section4() {
    var betaChecked by remember { mutableStateOf(true) }
    var context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    coroutineScope.launch {
        betaChecked = readSettings("isBeta") != "false"
        if(readSettings("isBeta") == null){
            saveSettings("isBeta", "true")
        }
    }

//    LaunchedEffect(Unit) {
//        withContext(Dispatchers.IO) {
//            betaChecked = readSettings("isBeta") =="true"
//        }
//    }

    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = stringResource(R.string.software_updates_source_code), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.current_version) + stringResource(id = R.string.version) + "." + stringResource(id = R.string.build_type))
//            Button(onClick = { /*TODO*/ }) {
//                Text("Check Updates")
//            }
            Spacer(modifier = Modifier.height(8.dp))
            UpdateCheckScreen()
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Text(text = stringResource(R.string.join_weicheng_log_beta_program), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = betaChecked,
                    onCheckedChange = {
                        betaChecked = it
                        coroutineScope.launch {
                            if (betaChecked) {
                                saveSettings("isBeta", "true")
                            } else {
                                saveSettings("isBeta", "false")
                            }
                        }
                    },
                    thumbContent = if (betaChecked) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    } else {
                        null
                    }
                )
            }
            Text(stringResource(R.string.beta_notice))
        }
    }
}

data class Language(val code: String, val name: String)

val supportedLanguages = listOf(
    Language("en", "English/英文"),
    Language("zh", "Chinese (Simplified)/简体中文"),
    // Add more languages as needed
)

@Composable
fun LanguagePicker(
    selectedLanguage: Language,
    onLanguageSelected: (Language) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Box {
        Button(onClick = { expanded = true }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .clickable { expanded = true }
                    .padding(16.dp)

            ) {
                Text(text = stringResource(R.string.language))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = selectedLanguage.name, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        if (expanded) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                supportedLanguages.forEach { language ->
                    DropdownMenuItem(
                        text = { Text(text = language.name) },
                        onClick = {
                            onLanguageSelected(language)
                            expanded = false
                            // Update the app locale when a language is selected
                            coroutineScope.launch {
                                saveSettings("language", language.code)
                            }
//                            configuration.setLocale(Locale(language.code))
//                            context.createConfigurationContext(Configuration(configuration))
                            context.startActivity(Intent(context, MainActivity::class.java))
                            (context as MainActivity).finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LanguagePickerScreen() {
    var selectedLanguage by remember { mutableStateOf(supportedLanguages.first()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LanguagePicker(
            selectedLanguage = selectedLanguage,
            onLanguageSelected = { language ->
                selectedLanguage = language
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Selected Language Code: ${selectedLanguage.code}",
            fontWeight = FontWeight.Bold
        )
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun UpdateCheckScreen() {
    var result by remember { mutableStateOf<List<String>?>(null) }
    var isFetchSuccessful by remember { mutableStateOf(false) }
    var context = LocalContext.current

    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isNetworkAvailable = isNetworkAvailable(connectivityManager)
    val isNotMeteredNetwork = isMeteredNetwork(connectivityManager)

    // Use LaunchedEffect to fetch updates when the composable is first composed
    if (isNotMeteredNetwork) {
        LaunchedEffect(Unit) {
            try {
                withContext(Dispatchers.IO) {
                    result = fetchNewVersion(
                        "https://weicheng.app/cms/weicheng_log/version.txt",
                        context
                    )
                }
                isFetchSuccessful = result != null
            } catch (e: Exception) {
                // Handle exceptions if needed
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
//            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isNotMeteredNetwork) {
            if (isFetchSuccessful) {
                result?.get(0)?.let { Text(text = stringResource(R.string.latest_version) + it) }
                Spacer(modifier = Modifier.height(8.dp))
                result?.get(2)
                    ?.let { Text(text = stringResource(R.string.version_update_notes) + it) }
                Spacer(modifier = Modifier.height(8.dp))
                ResultedUpdatesResults(
                    result,
                    true,
                    result?.get(3).toBoolean(),
                    LocalContext.current
                )
            } else {
                Text(
                    stringResource(R.string.server_disconnected_simple),
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            if(isNetworkAvailable) {
                AssistChipConstructor(
                    text = stringResource(R.string.cellular_or_metered_network_detected),
                    icon = Icons.Filled.WifiOff
                )
            } else {
                // No Internet Case
                AssistChipConstructor(
                    text = stringResource(R.string.server_disconnected),
                    icon = Icons.Filled.CloudOff
                )
            }
        }
        var context = LocalContext.current
        Row {
            Button(onClick = {
                val webIntent: Intent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Weicheng783/WeichengLog"))
                try {
                    ContextCompat.startActivity(context, webIntent, null)
                } catch (e: ActivityNotFoundException) {
                    // Define what your app should do if no activity can handle the intent.
                }
            }) {
                Text(stringResource(R.string.source_code))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                val webIntent: Intent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://weicheng.app/cms/weicheng_log/releases.php"))
                try {
                    ContextCompat.startActivity(context, webIntent, null)
                } catch (e: ActivityNotFoundException) {
                    // Define what your app should do if no activity can handle the intent.
                }
            }) {
                Text(stringResource(R.string.source_update_notes))
            }
        }
    }
}