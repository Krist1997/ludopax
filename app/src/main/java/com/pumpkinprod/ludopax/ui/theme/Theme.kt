package com.pumpkinprod.ludopax.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    tertiary = TertiaryColor
)


@Composable
fun LudopaxAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography, // Assuming Typography is defined elsewhere
        shapes = Shapes,        // Assuming Shapes is defined elsewhere
        content = content
    )
}
