package app.log.weicheng.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.log.weicheng.R

@Composable
fun LoginScreen(
    onLoginButtonClicked: (String, String) -> Unit,
    modifier: Modifier = Modifier
){
    val scrollState = rememberScrollState()
    var name by remember { mutableStateOf("") }
    var deviceCode by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = scrollState)
    ) {
        ColumnContent(
            name = name,
            deviceCode = deviceCode,
            onNameChange = { newName ->
                name = newName
            },
            onDeviceCodeChange = { newCode ->
                deviceCode = newCode
            },
            onDoneAction = {
                keyboardController?.hide()
            },
            onButtonClick = onLoginButtonClicked
        )
    }
}

@Composable
fun RoundedCornerCard(title: String, message: String, number: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .aspectRatio(1f) // Ensures the card is squared
            .clip(RoundedCornerShape(16.dp))
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(), // Center the content horizontally
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center, // Center the text horizontally
                    modifier = Modifier
                        .weight(1f) // Takes half of the available space
                )
                Text(
                    text = number.toString(),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center, // Center the text horizontally
                    modifier = Modifier
                        .weight(1f) // Takes half of the available space
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
            Text(
                text = message,
                fontSize = 18.sp,
                textAlign = TextAlign.Center, // Center the text horizontally
                fontFamily = FontFamily.Serif // Example of specifying a font family
            )
        }
    }
}

@Composable
fun ColumnContent(
    name: String,
    deviceCode: String,
    onNameChange: (String) -> Unit,
    onDeviceCodeChange: (String) -> Unit,
    onDoneAction: () -> Unit,
    onButtonClick: (String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TodayHeader()
        Text(
            text = "Let's Go",
            fontWeight = FontWeight.Light,
            style = MaterialTheme.typography.headlineMedium
        )

        HelloButton(name, deviceCode, onNameChange, onDeviceCodeChange, onDoneAction, onClick = {onButtonClick(deviceCode, name)})
        Spacer(modifier = Modifier.height(16.dp))
        DisplayPager()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericTextField(name: String, icon: ImageVector, text: String, onChange: (String) -> Unit, onDoneAction: () -> Unit, keyboardOptions: KeyboardOptions) {
    TextField(
        value = name,
        onValueChange = { onChange(it) },
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .clip(MaterialTheme.shapes.medium),
        label = { Text(text) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions(onDone = { onDoneAction() })
    )
}

@Composable
fun HelloButton(name: String,
                deviceCode: String,
                onNameChange: (String) -> Unit,
                onDeviceCodeChange: (String) -> Unit,
                onDoneAction: () -> Unit,
                onClick: () -> Unit)
{
    var isButtonClicked by remember { mutableStateOf(false) }

    if (!isButtonClicked) {
        GenericTextField(name = name, icon = Icons.Outlined.Person, text = "Enter your name", onChange = onNameChange, onDoneAction = onDoneAction, keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        ),)
        GenericTextField(name = deviceCode, icon = Icons.Outlined.Code, text = "Connect to device", onChange = onDeviceCodeChange, onDoneAction = onDoneAction, keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Send
        ),)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                onClick()
                isButtonClicked = true
            },
            modifier = Modifier.fillMaxWidth(0.9F)
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Say Hello")
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(textAlign = TextAlign.Center, text="Loading...\n Wait, Calm down~")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DisplayPager() {
    @OptIn(ExperimentalFoundationApi::class)
    val pagerState = rememberPagerState(pageCount = {1})
    HorizontalPager(state = pagerState) { page ->
        when (page) {
            0 -> RoundedCornerCard(message = stringResource(id = R.string.team_info), title = "Team Architects", number = page+1)
        }
    }
}

@Composable
fun TodayHeader() {
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
}

@Preview
@Composable
fun LoginScreenPreview(){
    LoginScreen(
        onLoginButtonClicked = { _: String, _: String -> },
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_medium))
    )
}