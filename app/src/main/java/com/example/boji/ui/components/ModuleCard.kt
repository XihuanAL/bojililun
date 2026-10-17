package com.example.boji.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.boji.ui.theme.Success
import kotlinx.coroutines.launch

/** 卡片右上角模块剪影的尺寸 */
private val MOTIF_SIZE = 130.dp

/** 进度槽的最小高度：总览三张卡靠它对齐 */
private val PROGRESS_SLOT_MIN = 76.dp

/**
 * 模块主卡——总览页和三个子模块页用的是**同一个组件**，
 * 所以「总览的小卡片」和「子模块顶部的大卡片」天然长一样。
 *
 * ```
 * ┌──────────────────────────────┐
 * │ 模块名            角标        │
 * │                              │
 * │        进 度（主体）          │  ← 大水=圆环 / 大睡=七个小方块 / 薄肌=无
 * │                              │
 * │ 激励语（点击可换一句）          │
 * │ 一行状态                      │
 * └──────────────────────────────┘
 * ```
 *
 * 卡片本身是模块色渐变 + 白色文字（深浅主题下都这么显示，属品牌卡）；
 * 卡片以外的内容（快捷操作、提醒设置）跟随系统主题，因此支持浅色 / 深色。
 */
@Composable
fun ModuleCard(
    title: String,
    badge: String,
    quote: String,
    brush: Brush,
    softColor: Color,
    glow: Color,
    modifier: Modifier = Modifier,
    decoration: DrawScope.(Color) -> Unit = {},
    quoteStyle: TextStyle = MaterialTheme.typography.headlineSmall,
    minHeight: Dp = 0.dp,
    showQuoteHint: Boolean = false,
    onCardClick: (() -> Unit)? = null,
    progressSlot: (@Composable () -> Unit)? = null,
    info: String? = null
) {
    // 换文案时的淡入 + 轻微缩放
    val alphaAnim = remember { Animatable(1f) }
    val scaleAnim = remember { Animatable(1f) }
    LaunchedEffect(quote) {
        alphaAnim.snapTo(0.15f)
        scaleAnim.snapTo(0.92f)
        launch { alphaAnim.animateTo(1f, tween(300, easing = FastOutSlowInEasing)) }
        launch { scaleAnim.animateTo(1f, tween(300, easing = FastOutSlowInEasing)) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .clip(RoundedCornerShape(20.dp))
            .background(brush)
            .then(
                if (onCardClick != null) Modifier.clickable { onCardClick.invoke() }
                else Modifier
            )
    ) {
        // 右侧模块剪影水印 + 光晕
        Canvas(
            modifier = Modifier
                .size(MOTIF_SIZE)
                .align(Alignment.CenterEnd)
                .alpha(0.6f)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glow.copy(alpha = 0.32f), Color.Transparent),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.width * 0.75f
                ),
                radius = size.width * 0.75f,
                center = Offset(size.width / 2f, size.height / 2f)
            )
            decoration(glow)
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = softColor,
                    letterSpacing = 4.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            if (progressSlot != null) {
                Spacer(Modifier.height(18.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = PROGRESS_SLOT_MIN),
                    contentAlignment = Alignment.Center,
                    content = { progressSlot() }
                )
            }

            Spacer(Modifier.height(18.dp))
            Column(
                modifier = Modifier
                    .alpha(alphaAnim.value)
                    .scale(scaleAnim.value)
            ) {
                Text(
                    text = quote,
                    style = quoteStyle,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            if (showQuoteHint) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "点一下，换一句",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.45f)
                )
            }

            if (info != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = info,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.78f)
                )
            }
        }
    }
}

/** 大水理论用的进度环（纯 Canvas 绘制） */
@Composable
fun ModuleRingProgress(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    stroke: Dp = 9.dp,
    content: @Composable () -> Unit
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 700),
        label = "ring"
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = stroke.toPx()
            drawArc(
                color = Color.White.copy(alpha = 0.16f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = width, cap = StrokeCap.Round)
            )
            if (animated > 0f) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    style = Stroke(width = width, cap = StrokeCap.Round)
                )
            }
        }
        content()
    }
}

/**
 * 大睡理论用的「近 7 天小方块」。
 * 绿 = 按时睡，半透明白 = 没忍住，空心 = 还没记录。
 */
@Composable
fun WeekSquares(
    results: List<Boolean?>,
    modifier: Modifier = Modifier,
    height: Dp = 44.dp,
    gap: Dp = 6.dp
) {
    val shape = RoundedCornerShape(height / 3f)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(gap)
    ) {
        results.forEach { r ->
            val fill = when (r) {
                true -> Success
                false -> Color.White.copy(alpha = 0.20f)
                null -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(height)
                    .background(fill, shape)
                    .border(
                        width = 1.dp,
                        color = if (r == null) Color.White.copy(alpha = 0.28f) else fill,
                        shape = shape
                    )
            )
        }
    }
}

// ------------------------------------------------------------------
// 各模块的 Canvas 背景剪影（纯代码绘制，无图片依赖）
// ------------------------------------------------------------------

/** 大水理论：透明玻璃杯 */
fun DrawScope.drawWaterGlass(glow: Color) {
    val cx = size.width / 2f
    val cy = size.height * 0.5f

    val cupH = size.height * 0.72f
    val topW = size.width * 0.62f
    val botW = size.width * 0.48f
    val top = cy - cupH / 2f
    val bottom = cy + cupH / 2f

    val body = Path().apply {
        moveTo(cx - topW / 2f, top)
        lineTo(cx + topW / 2f, top)
        lineTo(cx + botW / 2f, bottom)
        lineTo(cx - botW / 2f, bottom)
        close()
    }
    drawPath(body, glow.copy(alpha = 0.13f))
    drawPath(body, glow.copy(alpha = 0.55f), style = Stroke(width = 2.5.dp.toPx()))

    // 杯里的水
    val waterTop = bottom - cupH * 0.52f
    val ratio = (bottom - waterTop) / cupH
    val wAtWater = botW + (topW - botW) * ratio
    val water = Path().apply {
        moveTo(cx - wAtWater / 2f, waterTop)
        lineTo(cx + wAtWater / 2f, waterTop)
        lineTo(cx + botW / 2f, bottom)
        lineTo(cx - botW / 2f, bottom)
        close()
    }
    drawPath(water, glow.copy(alpha = 0.30f))

    // 杯口
    drawOval(
        color = glow.copy(alpha = 0.42f),
        topLeft = Offset(cx - topW / 2f, top - 6.dp.toPx()),
        size = Size(topW, 12.dp.toPx())
    )
    // 高光
    drawLine(
        color = Color.White.copy(alpha = 0.28f),
        start = Offset(cx - topW * 0.33f, top + 14.dp.toPx()),
        end = Offset(cx - botW * 0.28f, bottom - 14.dp.toPx()),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )
    // 水面波纹
    drawLine(
        color = Color.White.copy(alpha = 0.22f),
        start = Offset(cx - wAtWater * 0.32f, waterTop + 7.dp.toPx()),
        end = Offset(cx + wAtWater * 0.32f, waterTop + 7.dp.toPx()),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round
    )
}

/** 大睡理论：枕头 */
fun DrawScope.drawPillow(glow: Color) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val w = size.width * 0.86f
    val h = size.height * 0.46f

    rotate(degrees = -8f, pivot = Offset(cx, cy)) {
        drawRoundRect(
            color = glow.copy(alpha = 0.24f),
            topLeft = Offset(cx - w / 2f, cy - h / 2f),
            size = Size(w, h),
            cornerRadius = CornerRadius(h * 0.44f, h * 0.44f)
        )
        drawRoundRect(
            color = glow.copy(alpha = 0.13f),
            topLeft = Offset(cx - w / 2f + w * 0.11f, cy - h / 2f + h * 0.22f),
            size = Size(w * 0.78f, h * 0.56f),
            cornerRadius = CornerRadius(h * 0.28f, h * 0.28f)
        )
        // 中缝
        drawLine(
            color = glow.copy(alpha = 0.40f),
            start = Offset(cx - w * 0.26f, cy),
            end = Offset(cx + w * 0.26f, cy),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/** 薄肌理论：哑铃剪影 */
fun DrawScope.drawMuscleMark(glow: Color) {
    val w = size.width
    val cx = w / 2f
    val cy = size.height / 2f

    val barW = w * 0.52f
    val barH = 12.dp.toPx()
    drawRoundRect(
        color = glow.copy(alpha = 0.34f),
        topLeft = Offset(cx - barW / 2f, cy - barH / 2f),
        size = Size(barW, barH),
        cornerRadius = CornerRadius(barH / 2f, barH / 2f)
    )
    val plateW = 15.dp.toPx()
    val plateH = size.height * 0.52f
    listOf(-1f, 1f).forEach { dir ->
        val innerX = cx + dir * (barW / 2f - plateW * 0.35f) - if (dir > 0) 0f else plateW
        drawRoundRect(
            color = glow.copy(alpha = 0.30f),
            topLeft = Offset(innerX, cy - plateH / 2f),
            size = Size(plateW, plateH),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
        )
        val outerW = plateW * 0.66f
        val outerX = cx + dir * (barW / 2f + plateW * 0.55f) - if (dir > 0) 0f else outerW
        drawRoundRect(
            color = glow.copy(alpha = 0.18f),
            topLeft = Offset(outerX, cy - plateH * 0.32f),
            size = Size(outerW, plateH * 0.64f),
            cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
        )
    }
}
