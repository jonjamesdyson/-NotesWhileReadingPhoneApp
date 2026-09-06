package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val SophisticatedDarkColorScheme =
  darkColorScheme(
    primary = SophisticatedGold,
    onPrimary = SophisticatedDarkOnGold,
    primaryContainer = SophisticatedGoldContainer,
    onPrimaryContainer = SophisticatedOnGoldContainer,
    secondary = SophisticatedSage,
    onSecondary = Color(0xFF131F10),
    secondaryContainer = SophisticatedSageContainer,
    onSecondaryContainer = SophisticatedOnSageContainer,
    background = SophisticatedBackground,
    onBackground = SophisticatedTextPrimary,
    surface = SophisticatedSurface,
    onSurface = SophisticatedTextPrimary,
    surfaceVariant = SophisticatedSurfaceVariant,
    onSurfaceVariant = SophisticatedTextMuted,
    outline = SophisticatedBorder,
    outlineVariant = SophisticatedCardBorder,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = SophisticatedDarkColorScheme,
    typography = Typography,
    content = content
  )
}


