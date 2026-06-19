package org.wspcgir.strong_giraffe.views

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import kotlin.math.sin

@Composable
fun TextField (
    label: String,
    start: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
) {
    ValueField(
        label = label,
        start = start,
        fromString = { it },
        modifier = modifier,
        keyboardType = KeyboardType.Text,
        enabled = enabled,
        singleLine = singleLine,
        onChange = { onChange(it ?: "") },
    )
}

@Composable
fun IntField(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    start: Int = 0,
    onChange: (Int?) -> Unit,
) {
    NumberField(
        label = label,
        enabled = enabled,
        modifier = modifier,
        start = start,
        fromString = String::toIntOrNull,
        onChange = onChange
    )
}

@Composable
fun FloatField(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    start: Float = 0f,
    onChange: (Float?) -> Unit
) {
    NumberField(
        label = label,
        enabled = enabled,
        modifier = modifier,
        start = start,
        fromString = String::toFloatOrNull,
        onChange = onChange
    )
}

@Composable
fun <T> NumberField(
    label: String,
    start: T,
    fromString: (String) -> T?,
    modifier: Modifier,
    enabled: Boolean = true,
    onChange: (T?) -> Unit
) {
   ValueField(
       label = label,
       start = start,
       fromString = fromString,
       onChange = onChange,
       modifier = modifier,
       enabled = enabled,
       keyboardType = KeyboardType.Number,
   )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T> ValueField(
    label: String,
    start: T,
    fromString: (String) -> T?,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    onChange: (T?) -> Unit = { },
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var text by remember(key1 = start) { mutableStateOf(start.toString()) }
    var valid by remember { mutableStateOf(true) }

    val validStateColors = TextFieldDefaults.colors()
    val errorStateColors = TextFieldDefaults.colors().copy(
        focusedContainerColor = Color.Red.copy(alpha = 0.1f),
        errorTrailingIconColor = Color.Red,
        focusedTextColor = Color.Red
    )

    TextField(
        value = text,
        modifier = modifier,
        enabled = enabled,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
        colors = if (valid) { validStateColors } else { errorStateColors },
        trailingIcon = { if (!valid) { Icons.Default.Warning } },
        isError = !valid,
        singleLine = singleLine,
        onValueChange = { it ->
            text = it
            Log.d("TEXT_FIELD", "The value of it is '$it'")
            val value = fromString(it)
            Log.d("TEXT_FIELD", "The result of fromString is $value")
            valid = value != null
            onChange(value)
        }
    )
}