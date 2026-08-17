package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ShadcnBorder
import com.example.ui.theme.ShadcnForeground
import com.example.ui.theme.ShadcnMutedBackground
import com.example.ui.theme.ShadcnPrimary
import com.example.ui.theme.ShadcnWarning
import com.example.ui.theme.ShadcnWarningBg

/**
 * Minimal Flat Rupee Coin / Badge
 */
@Composable
fun GoldRupeeCoin(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(ShadcnWarningBg)
            .border(1.dp, Color(0xFFFEF08A), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val radius = this.size.minDimension / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val strokeW = size.toPx() * 0.08f
            val rColor = ShadcnWarning

            drawRupeeSign(center = center, size = radius * 1.1f, strokeWidth = strokeW, color = rColor)
        }
    }
}

private fun DrawScope.drawRupeeSign(
    center: Offset,
    size: Float,
    strokeWidth: Float,
    color: Color
) {
    val h = size * 0.75f
    val w = size * 0.55f
    val startX = center.x - w * 0.5f
    val startY = center.y - h * 0.55f

    // Top horizontal bar 1
    drawLine(
        color = color,
        start = Offset(startX, startY),
        end = Offset(startX + w, startY),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    // Top horizontal bar 2
    val bar2Y = startY + h * 0.22f
    drawLine(
        color = color,
        start = Offset(startX, bar2Y),
        end = Offset(startX + w * 0.9f, bar2Y),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    // Vertical top stem
    drawLine(
        color = color,
        start = Offset(startX + w * 0.25f, startY),
        end = Offset(startX + w * 0.25f, bar2Y + h * 0.28f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    // Curve
    val arcPath = Path().apply {
        moveTo(startX + w * 0.25f, startY + h * 0.05f)
        cubicTo(
            startX + w * 0.95f, startY + h * 0.05f,
            startX + w * 0.95f, bar2Y + h * 0.28f,
            startX + w * 0.25f, bar2Y + h * 0.28f
        )
    }
    drawPath(path = arcPath, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

    // Diagonal downward leg
    drawLine(
        color = color,
        start = Offset(startX + w * 0.35f, bar2Y + h * 0.26f),
        end = Offset(startX + w * 0.95f, startY + h),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}

/**
 * Minimal Telegram Card Icon
 */
@Composable
fun Telegram3DCube(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(ShadcnMutedBackground)
            .border(1.dp, ShadcnBorder, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Send,
            contentDescription = "Telegram",
            tint = ShadcnPrimary,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

/**
 * Video Player Play Button
 */
@Composable
fun VideoPlayButton(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(ShadcnPrimary)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play Tutorial",
            tint = Color.White,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}
