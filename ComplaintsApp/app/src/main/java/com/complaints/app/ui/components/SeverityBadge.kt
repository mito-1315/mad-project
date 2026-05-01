package com.complaints.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.complaints.app.ui.theme.*

/**
 * SeverityBadge — equivalent to the web's <SeverityBadge /> component.
 * Renders a colored chip: 🔴 High / 🟡 Medium / 🟢 Low / ⚪ Unclassified
 */
@Composable
fun SeverityBadge(severity: String, modifier: Modifier = Modifier) {
    val (text, bg, fg) = when (severity) {
        "High"   -> Triple("🔴 High",          HighBg,      SeverityHigh)
        "Medium" -> Triple("🟡 Medium",         MediumBg,    SeverityMedium)
        "Low"    -> Triple("🟢 Low",            LowBg,       SeverityLow)
        else     -> Triple("⚪ Unclassified",   SurfaceVariant, TextMuted)
    }
    Text(
        text     = text,
        modifier = modifier
            .background(bg, RoundedCornerShape(99.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        color      = fg,
        fontSize   = 11.sp,
        fontWeight = FontWeight.SemiBold
    )
}
