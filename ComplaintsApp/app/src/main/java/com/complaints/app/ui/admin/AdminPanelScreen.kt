package com.complaints.app.ui.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.complaints.app.data.model.Complaint
import com.complaints.app.ui.components.SeverityBadge
import com.complaints.app.ui.components.StatusBadge
import com.complaints.app.ui.components.TopBarWithLogout
import com.complaints.app.ui.theme.*
import com.complaints.app.util.SessionManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onLogout: () -> Unit,
    vm: AdminPanelViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    val user  by SessionManager.currentUser.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar on messages
    LaunchedEffect(state.snackMessage) {
        if (state.snackMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(state.snackMessage)
            vm.clearSnack()
        }
    }

    Scaffold(
        topBar = {
            TopBarWithLogout(
                title    = "Admin Panel",
                userName = user?.name ?: "",
                userRole = user?.role ?: "admin",
                onLogout = onLogout
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh    = vm::fetchAll,
            modifier     = Modifier.fillMaxSize().padding(padding)
        ) {
            LazyColumn(
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier            = Modifier.fillMaxSize()
            ) {
                // ── Header ─────────────────────────────────
                item {
                    Column {
                        Text("🛠️ Admin Panel", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Manage and triage all submitted complaints",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // ── Stats Cards ────────────────────────────
                state.stats?.let { stats ->
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                Triple("📋 Total",    stats.total.toString(),                       MaterialTheme.colorScheme.onSurface),
                                Triple("⏳ Pending",  (stats.byStatus["pending"] ?: 0).toString(), StatusPending),
                                Triple("🔴 High",     (stats.bySeverity["High"] ?: 0).toString(),  SeverityHigh),
                                Triple("✅ Resolved", (stats.byStatus["resolved"] ?: 0).toString(),StatusResolved)
                            ).forEach { (label, value, color) ->
                                Card(
                                    modifier  = Modifier.weight(1f),
                                    shape     = RoundedCornerShape(14.dp),
                                    elevation = CardDefaults.cardElevation(2.dp),
                                    colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(
                                        modifier            = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                                        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
                                    }
                                }
                            }
                        }
                    }

                    // ── Severity Distribution Bar ──────────
                    item {
                        Card(
                            modifier  = Modifier.fillMaxWidth(),
                            shape     = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(2.dp),
                            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Severity Distribution", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Spacer(Modifier.height(12.dp))
                                listOf(
                                    Triple("High",   stats.bySeverity["High"] ?: 0,   SeverityHigh),
                                    Triple("Medium", stats.bySeverity["Medium"] ?: 0, SeverityMedium),
                                    Triple("Low",    stats.bySeverity["Low"] ?: 0,    SeverityLow)
                                ).forEach { (label, count, color) ->
                                    val pct = if (stats.total > 0) count.toFloat() / stats.total else 0f
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier          = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    ) {
                                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                                            modifier = Modifier.width(56.dp), color = color)
                                        LinearProgressIndicator(
                                            progress = { pct },
                                            modifier = Modifier.weight(1f).height(8.dp).padding(horizontal = 8.dp),
                                            color    = color,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        Text("$count", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                            modifier = Modifier.width(32.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Filters ────────────────────────────────
                item {
                    Card(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Filters", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

                            // Severity filter
                            FilterRow("Severity", SEVERITY_OPTIONS, state.sevFilter) { vm.setSevFilter(it) }
                            // Category filter
                            FilterRow("Category", CATEGORY_OPTIONS, state.catFilter) { vm.setCatFilter(it) }
                            // Status filter
                            FilterRow("Status", listOf("All") + STATUS_OPTIONS, state.staFilter) { vm.setStaFilter(it) }
                        }
                    }
                }

                // ── Error / empty ──────────────────────────
                if (state.error.isNotEmpty()) {
                    item {
                        Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(10.dp)) {
                            Text(state.error, modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("All Complaints (${state.complaints.size})",
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f))
                        IconButton(onClick = vm::fetchAll) {
                            Icon(Icons.Default.Refresh, "Refresh", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (!state.isLoading && state.complaints.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🔍", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text("No complaints match your filters",
                                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    items(state.complaints, key = { it.id }) { complaint ->
                        AdminComplaintCard(
                            complaint     = complaint,
                            editedStatus  = vm.getEditedStatus(complaint.id),
                            editedNotes   = vm.getEditedNotes(complaint.id),
                            isSaving      = complaint.id in state.saving,
                            isSaved       = complaint.id in state.saved,
                            hasEdits      = complaint.id in state.edits,
                            onStatusChange = { vm.onEditStatus(complaint.id, it) },
                            onNotesChange  = { vm.onEditNotes(complaint.id, it) },
                            onSave         = { vm.saveEdit(complaint.id) },
                            onDelete       = { vm.deleteComplaint(complaint.id) }
                        )
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun FilterRow(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
            items(options) { opt ->
                FilterChip(
                    selected = selected == opt,
                    onClick  = { onSelect(opt) },
                    label    = { Text(opt, fontSize = 11.sp) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor     = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminComplaintCard(
    complaint:      Complaint,
    editedStatus:   String,
    editedNotes:    String,
    isSaving:       Boolean,
    isSaved:        Boolean,
    hasEdits:       Boolean,
    onStatusChange: (String) -> Unit,
    onNotesChange:  (String) -> Unit,
    onSave:         () -> Unit,
    onDelete:       () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var statusMenuExpanded by remember { mutableStateOf(false) }

    val dateStr = try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(complaint.createdAt)
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date!!)
    } catch (e: Exception) { complaint.createdAt.take(10) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title            = { Text("Delete Complaint") },
            text             = { Text("Delete \"${complaint.title}\" permanently?") },
            confirmButton    = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton    = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // Title + date
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(complaint.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(complaint.description.take(70) + if (complaint.description.length > 70) "…" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2,
                        overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(dateStr, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Submitted by + category + severity
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(color = PendingBg, shape = RoundedCornerShape(6.dp)) {
                    Text("👤 ${complaint.submittedBy?.name ?: "?"}",
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        fontSize = 11.sp, color = StatusPending, fontWeight = FontWeight.Medium)
                }
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(6.dp)) {
                    Text(complaint.category,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                SeverityBadge(complaint.severity)
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

            // Status dropdown
            ExposedDropdownMenuBox(
                expanded        = statusMenuExpanded,
                onExpandedChange = { statusMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value         = editedStatus.replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Status", fontSize = 12.sp) },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusMenuExpanded) },
                    modifier      = Modifier.fillMaxWidth().menuAnchor(),
                    shape         = RoundedCornerShape(10.dp),
                    textStyle     = LocalTextStyle.current.copy(fontSize = 14.sp)
                )
                ExposedDropdownMenu(expanded = statusMenuExpanded, onDismissRequest = { statusMenuExpanded = false }) {
                    STATUS_OPTIONS.forEach { s ->
                        DropdownMenuItem(
                            text    = { Text(s.replaceFirstChar { it.uppercase() }) },
                            onClick = { onStatusChange(s); statusMenuExpanded = false }
                        )
                    }
                }
            }

            // Admin notes input
            OutlinedTextField(
                value         = editedNotes,
                onValueChange = onNotesChange,
                label         = { Text("Admin Notes", fontSize = 12.sp) },
                placeholder   = { Text("Add a note…", fontSize = 13.sp) },
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(10.dp),
                maxLines      = 3,
                textStyle     = LocalTextStyle.current.copy(fontSize = 13.sp)
            )

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick  = onSave,
                    enabled  = hasEdits && !isSaving,
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = if (isSaved) StatusResolved else Primary
                    )
                ) {
                    when {
                        isSaving -> CircularProgressIndicator(Modifier.size(16.dp),
                            color = Color.White, strokeWidth = 2.dp)
                        isSaved  -> Text("✓ Saved", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        else     -> Text("Save", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }

                OutlinedButton(
                    onClick  = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = SeverityHigh)
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Delete", fontSize = 13.sp)
                }
            }
        }
    }
}
