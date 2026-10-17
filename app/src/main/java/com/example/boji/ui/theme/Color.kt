package com.example.boji.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * 设计系统 · 配色规范
 *
 * 基调：深色（#0E1218 系）+ 一个高饱和强调色 Orange（#FF7A33）
 * 三个模块用不同辅助色区分：
 *   大水理论 = 蓝 Water
 *   大睡理论 = 紫 Sleep
 *   薄肌理论 = 橙红 Muscle
 * 徽章金 Gold 只用于点缀（军衔星、达标标记）
 */

// ---------- 中性色（深色） ----------
val BgDark = Color(0xFF0E1218)
val SurfaceDark = Color(0xFF161C26)
val SurfaceDarkAlt = Color(0xFF1E2632)
val OutlineDark = Color(0xFF2B3644)
val TextPrimaryDark = Color(0xFFE9EDF3)
val TextSecondaryDark = Color(0xFF9AA5B5)
val TextTertiaryDark = Color(0xFF6B7686)

// ---------- 中性色（浅色） ----------
val BgLight = Color(0xFFF4F6F9)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceLightAlt = Color(0xFFEDEFF3)
val OutlineLight = Color(0xFFD9DEE6)
val TextPrimaryLight = Color(0xFF151A21)
val TextSecondaryLight = Color(0xFF5C6675)
val TextTertiaryLight = Color(0xFF8B94A3)

// ---------- 强调色 ----------
val Accent = Color(0xFFFF7A33)
val AccentSoft = Color(0xFFFF9A5C)
val AccentDim = Color(0xFF7A3312)

// ---------- 模块色 ----------
val Water = Color(0xFF38B6FF)
val WaterDeep = Color(0xFF0B6FA8)
val WaterSoft = Color(0xFF8FD8FF)

val Sleep = Color(0xFFB07CFF)
val SleepDeep = Color(0xFF5C33A8)
val SleepSoft = Color(0xFFD5BCFF)

val Muscle = Color(0xFFFF5A36)
val MuscleDeep = Color(0xFFA32A0E)
val MuscleSoft = Color(0xFFFFAC98)

// ---------- 点缀 ----------
val Gold = Color(0xFFFFD166)
val Success = Color(0xFF3DDC97)
val Danger = Color(0xFFFF5F56)

/**
 * 模块色的「可读写版本」：卡片外的按钮文字 / 链接 / 次要操作要用它。
 * 深色底上用 Soft（浅），浅色底上用 Deep（深），否则浅色模式下会看不清。
 */
@Composable
@ReadOnlyComposable
fun waterInk(): Color = if (isDarkTheme) WaterSoft else WaterDeep

@Composable
@ReadOnlyComposable
fun sleepInk(): Color = if (isDarkTheme) SleepSoft else SleepDeep

@Composable
@ReadOnlyComposable
fun muscleInk(): Color = if (isDarkTheme) MuscleSoft else MuscleDeep

/** 成功色同样需要深浅两版（浅色底上 #3DDC97 太淡） */
@Composable
@ReadOnlyComposable
fun successInk(): Color = if (isDarkTheme) Success else Color(0xFF0E7A55)

/** 便捷取色：根据当前深浅模式返回对应中性色 */
object BojiColors {
    fun bg(dark: Boolean) = if (dark) BgDark else BgLight
    fun surface(dark: Boolean) = if (dark) SurfaceDark else SurfaceLight
    fun surfaceAlt(dark: Boolean) = if (dark) SurfaceDarkAlt else SurfaceLightAlt
    fun outline(dark: Boolean) = if (dark) OutlineDark else OutlineLight
    fun textPrimary(dark: Boolean) = if (dark) TextPrimaryDark else TextPrimaryLight
    fun textSecondary(dark: Boolean) = if (dark) TextSecondaryDark else TextSecondaryLight
    fun textTertiary(dark: Boolean) = if (dark) TextTertiaryDark else TextTertiaryLight
}
