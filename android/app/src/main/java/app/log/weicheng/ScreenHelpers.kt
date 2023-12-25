package app.log.weicheng

import android.content.Context
import android.os.Build
import android.os.ext.SdkExtensions
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.log.weicheng.ui.HomeScreen
import app.log.weicheng.ui.SettingsScreen

/**
 * enum values that represent the screens in the app
 */
enum class ScreenHelpers(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    Settings(title = R.string.settings),
    Edit(title = R.string.edit),
    People(title = R.string.people),
}

@Composable
fun AlertDialogExample(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Example Icon")
        },
        title = {
            Text(text = dialogTitle, textAlign = TextAlign.Center)
        },
        text = {
            Text(text = dialogText, textAlign = TextAlign.Center)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(stringResource(R.string.dismiss))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogAppBar(
    currentScreen: ScreenHelpers,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier.verticalScroll(rememberScrollState())
) {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog){
        AlertDialogExample(
            onDismissRequest = { showDialog = false },
            onConfirmation = { showDialog = false },
            dialogTitle = stringResource(R.string.developer_title),
            dialogText = stringResource(id = R.string.team_info),
            icon = Icons.Default.People
        )
    }

    TopAppBar(
        title = {
            Row {
                Text(stringResource(currentScreen.title))
                Spacer(modifier = Modifier.width(16.dp))
                AssistChip(
                    onClick = { Log.d("Assist chip", "hello world") },
                    label = { Text(stringResource(R.string.version)) },
                    leadingIcon = {
//                    Icon(
//                        Icons.Filled.Settings,
//                        contentDescription = "Localized description",
//                        Modifier.size(AssistChipDefaults.IconSize)
//                    )
                    }
                )
                if(stringResource(id = R.string.build_type) == "beta") {
                    FilterChip(
                        onClick = { },
                        label = {
                            Text(stringResource(R.string.in_beta))
                        },
                        selected = true,
                        leadingIcon = run {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Android,
                                    contentDescription = "in Beta",
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        },
                    )
                }
            }
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = imageVector,
                        contentDescription = contentDescription
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = {
                showDialog = true
            }) {
                Icon(Icons.Outlined.Info, contentDescription = "Information")
            }
        }
    )
}

@Composable
fun ElevatedAssistChipExample() {
    ElevatedAssistChip(
        onClick = { /* Do something! */ },
        label = { Text("Assist Chip") },
        leadingIcon = {
            Icon(
                Icons.Filled.Settings,
                contentDescription = "Localized description",
                Modifier.size(AssistChipDefaults.IconSize)
            )
        }
    )
}

@Composable
fun BottomAppBarExample(
    navController: NavHostController,
) {
    var selectedTab by remember { mutableStateOf(ScreenHelpers.Start) }

    BottomAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
        contentPadding = BottomAppBarDefaults.ContentPadding,
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInHorizontally(),
            exit = fadeOut() + slideOutHorizontally(),
            modifier = Modifier.weight(1f)
        ) {
            IconButton(
                onClick = {
                    if(selectedTab != ScreenHelpers.Settings) {
                        selectedTab = ScreenHelpers.Settings
                        navController.navigate(ScreenHelpers.Settings.name) {
                            popUpTo(ScreenHelpers.Settings.name) {
                                inclusive = true
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                if(selectedTab == ScreenHelpers.Settings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
                }else{
                    Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                }
            }
        }

        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInHorizontally(),
            exit = fadeOut() + slideOutHorizontally(),
            modifier = Modifier.weight(1f)
        ) {
            IconButton(
                onClick = {
                    if(selectedTab != ScreenHelpers.Start) {
                        selectedTab = ScreenHelpers.Start
                        navController.navigate(ScreenHelpers.Start.name) {
                            popUpTo(ScreenHelpers.Start.name) {
                                inclusive = true
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                if(selectedTab == ScreenHelpers.Start) {
                    Icon(Icons.Filled.Home, contentDescription = "Start")
                }else{
                    Icon(Icons.Outlined.Home, contentDescription = "Start")
                }
            }
        }

//        AnimatedVisibility(
//            visible = true,
//            enter = fadeIn() + slideInHorizontally(),
//            exit = fadeOut() + slideOutHorizontally(),
//            modifier = Modifier.weight(1f)
//        ) {
//            IconButton(
//                onClick = {
//                    if(selectedTab != ScreenHelpers.Edit) {
//                        selectedTab = ScreenHelpers.Edit
//                    }
//                },
//                modifier = Modifier
//                    .wrapContentWidth()
//                    .padding(BottomAppBarDefaults.ContentPadding),
////                containerColor = BottomAppBarDefaults.containerColor,
////                elevation = FloatingActionButtonDefaults.elevation()
//            ) {
//                if(selectedTab == ScreenHelpers.Edit) {
//                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
//                }else{
//                    Icon(Icons.Outlined.Edit, contentDescription = "Edit")
//                }
//            }
//        }
//
//        AnimatedVisibility(
//            visible = true,
//            enter = fadeIn() + slideInHorizontally(),
//            exit = fadeOut() + slideOutHorizontally(),
//            modifier = Modifier.weight(1f)
//        ) {
//            IconButton(
//                onClick = {
//                    if(selectedTab != ScreenHelpers.People) {
//                        selectedTab = ScreenHelpers.People
//                    }
//                },
//                modifier = Modifier.weight(1f)
//            ) {
//                if(selectedTab == ScreenHelpers.People) {
//                    Icon(Icons.Filled.People, contentDescription = "People")
//                }else{
//                    Icon(Icons.Outlined.People, contentDescription = "People")
//                }
//            }
//        }
    }
}

@RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
@Composable
fun LogApp(
    navController: NavHostController = rememberNavController(),
    context: Context
) {
    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreen = ScreenHelpers.valueOf(
        backStackEntry?.destination?.route ?: ScreenHelpers.Start.name
    )

    Scaffold(
        topBar = {
            when (currentScreen.name) {
                ScreenHelpers.Start.name -> {
//                    CenterAlignedTopAppBarExample(
//                        currentScreen = currentScreen,
//                        canNavigateBack = navController.previousBackStackEntry != null,
//                        imageVector = Icons.Filled.ArrowBack,
//                        contentDescription = stringResource(R.string.back_button),
//                        navigateUp = { navController.navigateUp() }
//                    )
//                    HomeAppBar(
//                        currentScreen = currentScreen,
//                        canNavigateBack = navController.previousBackStackEntry != null,
//                        imageVector = Icons.Filled.ArrowBack,
//                        contentDescription = stringResource(R.string.back_button),
//                        navigateUp = { navController.navigateUp() }
//                    )
                }
                ScreenHelpers.Settings.name -> {
                    LogAppBar(
                        currentScreen = currentScreen,
                        canNavigateBack = false,
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button),
                        navigateUp = { navController.navigateUp() }
                    )
                }
            }
        },
        bottomBar = {
            BottomAppBarExample(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ScreenHelpers.Start.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = ScreenHelpers.Start.name) {
                if (SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2) {
                    HomeScreen()
                }
            }
            composable(route = ScreenHelpers.Settings.name) {
                SettingsScreen(
                    navController
                )
            }
        }
    }
}