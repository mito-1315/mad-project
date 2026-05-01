package com.complaints.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.complaints.app.ui.theme.*

/**
 * StatusBadge — equivalent to the web's <StatusBadge /> component.
 */
@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (label, bg, fg) = when (status) {
        "pending"     -> Triple("Pending",     PendingBg,  StatusPending)
        "in-progress" -> Triple("In Progress", ProgressBg, StatusInProgress)
        "resolved"    -> Triple("Resolved",    ResolvedBg, StatusResolved)
        "rejected"    -> Triple("Rejected",    RejectedBg, StatusRejected)
        else          -> Triple(status,        SurfaceVariant, TextMuted)
    }
    Text(
        text     = label,
        modifier = modifier
            .background(bg, RoundedCornerShape(99.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        color      = fg,
        fontSize   = 11.sp,
        fontWeight = FontWeight.SemiBold
    )
}
