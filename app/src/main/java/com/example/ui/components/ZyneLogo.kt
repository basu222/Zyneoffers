package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ShadcnForeground
import com.example.ui.theme.ShadcnPrimary
import com.example.ui.theme.ZyneWhite

/**
 * Minimalist Zyne Monogram Mark ('Z')
 * Clean solid monochrome design fitting shadcn/ui aesthetic
 */
@Composable
fun ZyneLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    shadowElevation: Dp = 0.dp,
    backgroundColor: Color = com.example.ui.theme.ZyneBlue,
    markColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            // Background solid square with rounded corners
            drawRoundRect(
                color = backgroundColor,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.22f, h * 0.22f)
            )

            // Precision Z monogram geometry
            val pad = w * 0.24f
            val pw = w - pad * 2
            val ph = h - pad * 2

            val path = Path().apply {
                val barH = ph * 0.25f
                moveTo(pad, pad)
                lineTo(pad + pw, pad)
                lineTo(pad + pw, pad + barH)
                lineTo(pad + barH * 1.5f, pad + ph - barH)
                lineTo(pad + pw, pad + ph - barH)
                lineTo(pad + pw, pad + ph)
                lineTo(pad, pad + ph)
                lineTo(pad, pad + ph - barH)
                lineTo(pad + pw - barH * 1.5f, pad + barH)
                lineTo(pad, pad + barH)
                close()
            }

            drawPath(
                path = path,
                color = markColor
            )
        }
    }
}

/**
 * Minimalist Zyne Full Logo
 * Monogram mark + crisp sans-serif typography
 */
@Composable
fun ZyneOfficialLogo(
    modifier: Modifier = Modifier,
    markSize: Dp = 32.dp,
    textColor: Color = ShadcnForeground,
    fontSize: Int = 22,
    showSparkle: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        ZyneLogoMark(
            size = markSize,
            shadowElevation = 0.dp
        )

        Spacer(modifier = Modifier.width(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Zyne",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = fontSize.sp,
                    letterSpacing = (-0.5).sp
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Offers",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Normal,
                    color = textColor.copy(alpha = 0.65f),
                    fontSize = fontSize.sp,
                    letterSpacing = (-0.5).sp
                )
            )
        }
    }
}

/**
 * Minimal Sparkle / Star Icon
 */
@Composable
fun SparkleIcon(
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    tint: Color = ShadcnPrimary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f

        val sparklePath = Path().apply {
            moveTo(cx, 0f)
            quadraticTo(cx, cy, w, cy)
            quadraticTo(cx, cy, cx, h)
            quadraticTo(cx, cy, 0f, cy)
            quadraticTo(cx, cy, cx, 0f)
            close()
        }
        drawPath(sparklePath, color = tint)
    }
}
