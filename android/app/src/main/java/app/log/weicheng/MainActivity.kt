package app.log.weicheng

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import app.log.weicheng.ui.readSettings
import app.log.weicheng.ui.theme.WeichengLogTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : ComponentActivity() {
    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
//        val config = resources.configuration
//        val lang = "en"
//        val locale = Locale(lang)
//        Locale.setDefault(locale)
//        config.setLocale(locale)
//
//        createConfigurationContext(config)
//        resources.updateConfiguration(config, resources.displayMetrics)

        setContent {
            WeichengLogTheme {
//                var locale by remember { mutableStateOf(Locale.getDefault().language) }
                    LaunchedEffect(Unit) {
                        withContext(Dispatchers.IO) {
                            val temp = readSettings("language")
                            if(temp != null){
                                val config = resources.configuration
                                val newLocale = Locale(temp)
                                Locale.setDefault(newLocale)
                                config.setLocale(newLocale)
                                createConfigurationContext(config)
                                resources.updateConfiguration(config, resources.displayMetrics)
                            }
                        }
                    }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    LogApp(context = context)
                }
            }
        }
    }

}