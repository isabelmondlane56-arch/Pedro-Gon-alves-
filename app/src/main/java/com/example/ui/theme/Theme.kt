package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val MoneyHubDarkColorScheme = darkColorScheme(
    primary = BurnishedGold,
    onPrimary = ObsidianBlack,
    primaryContainer = DarkGoldContainer,
    onPrimaryContainer = ChampagneGold,
    secondary = SapphireBlue,
    onSecondary = ObsidianBlack,
    secondaryContainer = RoyalNavy,
    onSecondaryContainer = LightSapphire,
    tertiary = EmeraldSuccess,
    onTertiary = ObsidianBlack,
    background = ObsidianBlack,
    onBackground = OffWhiteText,
    surface = DarkSurfaceCard,
    onSurface = OffWhiteText,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = CoolGrayText,
    outline = DarkOutline,
    error = CrimsonAlert,
    onError = OffWhiteText
)

private val MoneyHubLightColorScheme = lightColorScheme(
    primary = RichGold,
    onPrimary = PureWhiteSurface,
    primaryContainer = ChampagneGold,
    onPrimaryContainer = DarkGoldContainer,
    secondary = MidnightNavy,
    onSecondary = PureWhiteSurface,
    secondaryContainer = SoftNavyTint,
    onSecondaryContainer = MidnightNavy,
    tertiary = EmeraldSuccess,
    onTertiary = PureWhiteSurface,
    background = IvoryBackground,
    onBackground = InkDark,
    surface = PureWhiteSurface,
    onSurface = InkDark,
    surfaceVariant = SoftNavyTint,
    onSurfaceVariant = SlateMuted,
    outline = LightOutline,
    error = CrimsonAlert,
    onError = PureWhiteSurface
)

val MoneyHubShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MoneyHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MoneyHubDarkColorScheme else MoneyHubLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = MoneyHubShapes,
        content = content
    )
}
