package com.example.ricefieldsurvey.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val FieldGreen = Color(0xFF176B4B)
val FieldGreenLight = Color(0xFFE4F3EA)
val FieldInk = Color(0xFF182D25)
val FieldBackground = Color(0xFFF6F9F6)

private val fieldColors = lightColorScheme(
    primary = FieldGreen,
    onPrimary = Color.White,
    primaryContainer = FieldGreenLight,
    onPrimaryContainer = FieldInk,
    secondary = Color(0xFF526A5D),
    background = FieldBackground,
    onBackground = FieldInk,
    surface = Color.White,
    onSurface = FieldInk,
    outline = Color(0xFF829489),
    error = Color(0xFFB3261E),
)

@Composable
fun SurveyTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = fieldColors, content = content)
}
