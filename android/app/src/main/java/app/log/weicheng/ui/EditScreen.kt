package app.log.weicheng.ui

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

@RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
@Composable
fun EditScreen(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    GenericTextField(name = "a", icon = Icons.Outlined.Person, text = "Enter your name", onChange = {}, onDoneAction = {}, keyboardOptions = KeyboardOptions.Default.copy(
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Next
    ),)
    GenericTextField(name = "b", icon = Icons.Outlined.Code, text = "Connect to device", onChange = {}, onDoneAction = {}, keyboardOptions = KeyboardOptions.Default.copy(
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Send
    ),)
}