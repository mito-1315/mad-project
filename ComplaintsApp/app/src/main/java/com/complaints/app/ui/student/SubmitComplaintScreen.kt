package com.complaints.app.ui.student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.complaints.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitComplaintScreen(
    onSuccess: () -> Unit,
    onCancel:  () -> Unit,
    vm: SubmitComplaintViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    // Navigate when submitted successfully
    LaunchedEffect(state.navigateBack) {
        if (state.navigateBack) {
            vm.clearNavigation()
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title         = { Text("Submit a Complaint", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Subtitle
            Text(
                "Your complaint will be automatically classified by severity using AI.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // API error
            AnimatedVisibility(visible = state.apiError.isNotEmpty()) {
                Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(10.dp)) {
                    Text(state.apiError, modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall)
                }
            }

            // Success
            AnimatedVisibility(visible = state.successMessage.isNotEmpty()) {
                Surface(color = LowBg, shape = RoundedCornerShape(10.dp)) {
                    Text(state.successMessage, modifier = Modifier.padding(12.dp),
                        color = StatusResolved,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium)
                }
            }

            // Title field
            OutlinedTextField(
                value         = state.title,
                onValueChange = vm::onTitleChange,
                label         = { Text("Complaint Title *") },
                placeholder   = { Text("Brief summary of the issue (min 5 chars)") },
                leadingIcon   = { Icon(Icons.Default.Title, null) },
                isError       = state.titleError.isNotEmpty(),
                supportingText = if (state.titleError.isNotEmpty()) {
                    { Text(state.titleError, color = MaterialTheme.colorScheme.error) }
                } else null,
                singleLine   = true,
                modifier     = Modifier.fillMaxWidth(),
                shape        = RoundedCornerShape(12.dp)
            )

            // Category dropdown
            ExposedDropdownMenuBox(
                expanded        = categoryMenuExpanded,
                onExpandedChange = { categoryMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value         = state.category.ifBlank { "-- Select a department --" },
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Category *") },
                    leadingIcon   = { Icon(Icons.Default.Category, null) },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                    isError       = state.categoryError.isNotEmpty(),
                    supportingText = if (state.categoryError.isNotEmpty()) {
                        { Text(state.categoryError, color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier      = Modifier.fillMaxWidth().menuAnchor(),
                    shape         = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded        = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false }
                ) {
                    COMPLAINT_CATEGORIES.forEach { cat ->
                        DropdownMenuItem(
                            text    = { Text(cat) },
                            onClick = { vm.onCategoryChange(cat); categoryMenuExpanded = false }
                        )
                    }
                }
            }

            // Description field
            OutlinedTextField(
                value         = state.description,
                onValueChange = vm::onDescChange,
                label         = {
                    Text("Description * (${state.description.length} chars — min 20)")
                },
                placeholder   = { Text("Describe your issue in detail…") },
                leadingIcon   = { Icon(Icons.Default.Description, null) },
                isError       = state.descError.isNotEmpty(),
                supportingText = if (state.descError.isNotEmpty()) {
                    { Text(state.descError, color = MaterialTheme.colorScheme.error) }
                } else null,
                minLines = 5,
                maxLines = 10,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp)
            )

            // AI notice
            Surface(
                color = PendingBg,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Text("🤖 ", fontSize = 14.sp)
                    Text(
                        "AI Severity Classification: Our ML model will automatically classify your complaint as Low, Medium, or High severity.",
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusPending
                    )
                }
            }

            // Buttons row
            Row(
                modifier             = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick  = onCancel,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape    = RoundedCornerShape(12.dp)
                ) { Text("Cancel") }

                Button(
                    onClick  = vm::submit,
                    enabled  = !state.isLoading,
                    modifier = Modifier.weight(2f).height(52.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    if (state.isLoading)
                        CircularProgressIndicator(modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    else Text("📤 Submit Complaint", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
