package com.complaints.app.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.complaints.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onSignupSuccess: (role: String) -> Unit,
    onGoToLogin: () -> Unit,
    vm: SignupViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    var showPassword by remember { mutableStateOf(false) }
    var showConfirm  by remember { mutableStateOf(false) }
    var roleMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.successRole) {
        state.successRole?.let {
            vm.clearSuccess()
            onSignupSuccess(it)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Primary.copy(alpha = 0.08f), MaterialTheme.colorScheme.background))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape    = RoundedCornerShape(24.dp),
                color    = Primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) { Text("📝", fontSize = 36.sp) }
            }

            Spacer(Modifier.height(20.dp))
            Text("Create account", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Join the complaint management portal", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            Spacer(Modifier.height(24.dp))

            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AnimatedVisibility(visible = state.apiError.isNotEmpty()) {
                        Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(10.dp)) {
                            Text(state.apiError, modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    // Name
                    OutlinedTextField(
                        value = state.name, onValueChange = vm::onNameChange,
                        label = { Text("Full name") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        isError = state.nameError.isNotEmpty(),
                        supportingText = if (state.nameError.isNotEmpty()) {{ Text(state.nameError, color = MaterialTheme.colorScheme.error) }} else null,
                        singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                    )

                    // Email
                    OutlinedTextField(
                        value = state.email, onValueChange = vm::onEmailChange,
                        label = { Text("Email address") },
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = state.emailError.isNotEmpty(),
                        supportingText = if (state.emailError.isNotEmpty()) {{ Text(state.emailError, color = MaterialTheme.colorScheme.error) }} else null,
                        singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                    )

                    // Password
                    OutlinedTextField(
                        value = state.password, onValueChange = vm::onPasswordChange,
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = state.passwordError.isNotEmpty(),
                        supportingText = if (state.passwordError.isNotEmpty()) {{ Text(state.passwordError, color = MaterialTheme.colorScheme.error) }} else null,
                        singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                    )

                    // Confirm password
                    OutlinedTextField(
                        value = state.confirm, onValueChange = vm::onConfirmChange,
                        label = { Text("Confirm password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { showConfirm = !showConfirm }) {
                                Icon(if (showConfirm) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                            }
                        },
                        visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = state.confirmError.isNotEmpty(),
                        supportingText = if (state.confirmError.isNotEmpty()) {{ Text(state.confirmError, color = MaterialTheme.colorScheme.error) }} else null,
                        singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                    )

                    // Role dropdown
                    ExposedDropdownMenuBox(
                        expanded = roleMenuExpanded,
                        onExpandedChange = { roleMenuExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = state.role.replaceFirstChar { it.uppercase() },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Account type") },
                            leadingIcon = { Icon(Icons.Default.Badge, null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleMenuExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = roleMenuExpanded, onDismissRequest = { roleMenuExpanded = false }) {
                            listOf("student", "admin").forEach { r ->
                                DropdownMenuItem(
                                    text = { Text(r.replaceFirstChar { it.uppercase() }) },
                                    onClick = { vm.onRoleChange(r); roleMenuExpanded = false }
                                )
                            }
                        }
                    }

                    // Submit
                    Button(
                        onClick = vm::signup, enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        if (state.isLoading)
                            CircularProgressIndicator(modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        else Text("Create account", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.Center) {
                Text("Already have an account? ", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                TextButton(onClick = onGoToLogin, contentPadding = PaddingValues(0.dp)) {
                    Text("Sign in", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }
}
