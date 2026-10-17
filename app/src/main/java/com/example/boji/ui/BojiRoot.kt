package com.example.boji.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.boji.MainActivity
import com.example.boji.R
import com.example.boji.data.Records
import com.example.boji.data.Settings
import com.example.boji.ui.screen.GuideScreen
import com.example.boji.ui.screen.MuscleScreen
import com.example.boji.ui.screen.OverviewScreen
import com.example.boji.ui.screen.SettingsScreen
import com.example.boji.ui.screen.SleepScreen
import com.example.boji.ui.screen.WaterScreen
import com.example.boji.ui.theme.Accent
import com.example.boji.ui.theme.Muscle
import com.example.boji.ui.theme.Sleep
import com.example.boji.ui.theme.Water
import com.example.boji.util.Copy
import kotlinx.coroutines.delay

private data class NavItem(
    val label: String,
    val icon: ImageVector,
    val tint: Color
)

private val NAV_ITEMS = listOf(
    NavItem("总览", Icons.Filled.Home, Accent),
    NavItem("大水理论", Icons.Filled.WaterDrop, Water),
    NavItem("大睡理论", Icons.Filled.Bedtime, Sleep),
    NavItem("薄肌理论", Icons.Filled.FitnessCenter, Muscle)
)

@Composable
fun BojiRoot(
    controller: AppController,
    settings: Settings,
    records: Records,
    targetTab: MainActivity.TabRequest
) {
    var tab by remember { mutableIntStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }
    var showGuide by remember { mutableStateOf(false) }
    var splashDone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(900)
        splashDone = true
    }

    LaunchedEffect(targetTab.seq) {
        when (targetTab.tab) {
            MainActivity.TAB_WATER -> { showSettings = false; showGuide = false; tab = 1 }
            MainActivity.TAB_SLEEP -> { showSettings = false; showGuide = false; tab = 2 }
            MainActivity.TAB_MUSCLE -> { showSettings = false; showGuide = false; tab = 3 }
        }
    }

    Crossfade(targetState = splashDone, label = "splash") { done ->
        if (!done) {
            SplashScreen()
        } else {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        NAV_ITEMS.forEachIndexed { index, item ->
                            val selected = tab == index && !showSettings && !showGuide
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    showSettings = false
                                    showGuide = false
                                    tab = index
                                },
                                icon = {
                                    Icon(
                                        item.icon,
                                        contentDescription = item.label,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        item.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = item.tint,
                                    selectedTextColor = item.tint,
                                    indicatorColor = item.tint.copy(alpha = 0.16f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    when {
                        showGuide -> GuideScreen(onBack = { showGuide = false })
                        showSettings -> SettingsScreen(
                            controller = controller,
                            settings = settings,
                            onBack = { showSettings = false },
                            onOpenGuide = { showGuide = true }
                        )
                        tab == 0 -> OverviewScreen(
                            controller = controller,
                            settings = settings,
                            records = records,
                            onOpenSettings = { showSettings = true },
                            onNavigate = { index ->
                                showSettings = false
                                showGuide = false
                                tab = index
                            }
                        )
                        tab == 1 -> WaterScreen(controller, settings, records)
                        tab == 2 -> SleepScreen(controller, settings, records)
                        else -> MuscleScreen(controller, settings)
                    }
                }
            }
        }
    }
}

@Composable
private fun SplashScreen() {
    val context = LocalContext.current
    val quote = remember { Copy.muscleQuote(context) }
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(500),
        label = "splashScale"
    )
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(400),
        label = "splashAlpha"
    )

    // 闪屏跟随主题底色，浅色模式下不会突然黑屏
    val bg = MaterialTheme.colorScheme.background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(bg, MaterialTheme.colorScheme.surfaceVariant, bg)
                )
            )
            .scale(scale)
            .alpha(alpha),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = context.getString(R.string.splash_title),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Text(
                text = context.getString(R.string.splash_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Accent
            )
            Text(
                text = quote,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 24.dp, start = 40.dp, end = 40.dp)
            )
        }
    }
}
