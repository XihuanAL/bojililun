package com.example.boji.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** 三个沉浸式模块页强制使用深色语义（底色本来就是深色渐变），避免浅色主题下底卡片文字看不清 */
val BojiDarkColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = Color(0xFF1A0D04),
    primaryContainer = AccentDim,
    onPrimaryContainer = AccentSoft,
    secondary = Water,
    onSecondary = Color(0xFF001C2B),
    tertiary = Sleep,
    background = BgDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceDarkAlt,
    onSurfaceVariant = TextSecondaryDark,
    outline = OutlineDark,
    error = Danger
)

private val LightScheme = lightColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE2D1),
    onPrimaryContainer = Color(0xFF5C2408),
    secondary = WaterDeep,
    onSecondary = Color.White,
    tertiary = SleepDeep,
    background = BgLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceLightAlt,
    onSurfaceVariant = TextSecondaryLight,
    outline = OutlineLight,
    error = Danger
)

/** 当前是否为深色模式（供自定义组件读中性色用） */
val LocalIsDark = staticCompositionLocalOf { true }

val isDarkTheme: Boolean
    @Composable
    @ReadOnlyComposable
    get() = LocalIsDark.current

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Composable
fun BojiTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    CompositionLocalProvider(LocalIsDark provides dark) {
        MaterialTheme(
            colorScheme = if (dark) BojiDarkColorScheme else LightScheme,
            typography = BojiTypography,
            content = content
        )
    }
}
