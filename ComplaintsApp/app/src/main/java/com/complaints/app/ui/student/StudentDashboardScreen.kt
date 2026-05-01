package com.complaints.app.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.complaints.app.ui.components.SeverityBadge
import com.complaints.app.ui.components.StatusBadge
import com.complaints.app.ui.components.TopBarWithLogout
import com.complaints.app.ui.theme.*
import com.complaints.app.util.SessionManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
    onSubmitNew: () -> Unit,
    onLogout: () -> Unit,
    vm: StudentDashboardViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    val user  by SessionManager.currentUser.collectAsState()

    Scaffold(
        topBar = {
            TopBarWithLogout(
                title    = "Complaint Portal",
                userName = user?.name ?: "",
                userRole = user?.role ?: "student",
                onLogout = onLogout
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick            = onSubmitNew,
                containerColor     = Primary,
                contentColor       = MaterialTheme.colorScheme.onPrimary,
                icon               = { Icon(Icons.Default.Add, "New Complaint") },
                text               = { Text("New Complaint", fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh    = vm::fetchComplaints,
            modifier     = Modifier.fillMaxSize().padding(padding)
        ) {
            LazyColumn(
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier            = Modifier.fillMaxSize()
            ) {
                // ── Welcome Header ────────────────────────
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "👋 Hello, ${user?.name}",
                                style      = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Track your submitted complaints below",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = vm::fetchComplaints) {
                            Icon(Icons.Default.Refresh, "Refresh", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // ── Stats Cards ───────────────────────────
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            Triple("📋 Total",    state.totalCount.toString(),   MaterialTheme.colorScheme.onSurface),
                            Triple("⏳ Pending",  state.pendingCount.toString(),  StatusPending),
                            Triple("✅ Resolved", state.resolvedCount.toString(), StatusResolved),
                            Triple("🔴 High",     state.highSevCount.toString(),  SeverityHigh)
                        ).forEach { (label, value, color) ->
                            Card(
                                modifier  = Modifier.weight(1f),
                                shape     = RoundedCornerShape(14.dp),
                                elevation = CardDefaults.cardElevation(2.dp),
                                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier             = Modifier.padding(10.dp),
                                    horizontalAlignment  = Alignment.CenterHorizontally
                                ) {
                                    Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                                    Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
                                }
                            }
                        }
                    }
                }

                // ── Filter Chips ──────────────────────────
                item {
                    Text("My Complaints", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("all", "pending", "in-progress", "resolved", "rejected")) { f ->
                            FilterChip(
                                selected = state.statusFilter == f,
                                onClick  = { vm.setFilter(f) },
                                label    = { Text(f.replaceFirstChar { it.uppercase() }, fontSize = 12.sp) },
                                colors   = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    selectedLabelColor     = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                // ── Error ─────────────────────────────────
                if (state.error.isNotEmpty()) {
                    item {
                        Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(10.dp)) {
                            Text(state.error, modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }

                // ── Complaints List ───────────────────────
                if (!state.isLoading && state.filtered.isEmpty()) {
                    item {
                        Column(
                            modifier            = Modifier.fillMaxWidth().padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📭", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                if (state.statusFilter == "all") "No complaints yet"
                                else "No ${state.statusFilter} complaints",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                if (state.statusFilter == "all") "Tap + to submit your first complaint."
                                else "Try a different filter.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                } else {
                    items(state.filtered, key = { it.id }) { complaint ->
                        ComplaintCard(complaint = complaint)
                    }
                }

                item { Spacer(Modifier.height(80.dp)) } // FAB clearance
            }
        }
    }
}

@Composable
private fun ComplaintCard(complaint: com.complaints.app.data.model.Complaint) {
    val dateStr = try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(complaint.createdAt)
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date!!)
    } catch (e: Exception) { complaint.createdAt.take(10) }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text       = complaint.title,
                    modifier   = Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(8.dp))
                StatusBadge(complaint.status)
            }

            Spacer(Modifier.height(6.dp))

            // Description preview
            Text(
                text     = complaint.description.take(80) + if (complaint.description.length > 80) "…" else "",
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(10.dp))

            // Bottom row: category + severity + date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category chip
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        complaint.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                SeverityBadge(complaint.severity)

                Spacer(Modifier.weight(1f))

                Text(dateStr, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Admin notes
            if (!complaint.adminNotes.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Surface(color = LowBg, shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.padding(8.dp)) {
                        Text("💬 ", fontSize = 12.sp)
                        Text(
                            complaint.adminNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusResolved,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
