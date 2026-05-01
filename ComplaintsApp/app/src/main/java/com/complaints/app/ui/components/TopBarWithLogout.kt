package com.complaints.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.complaints.app.ui.theme.*

/**
 * TopBarWithLogout — equivalent to the web's <Navbar /> component.
 * Shows the app title, current user name + role badge, and logout button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarWithLogout(
    title:       String,
    userName:    String,
    userRole:    String,
    onLogout:    () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text       = title,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 17.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text     = "👤 $userName",
                        fontSize = 12.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        color  = if (userRole == "admin") PendingBg else LowBg,
                        shape  = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text     = userRole,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                            fontSize = 10.sp,
                            color    = if (userRole == "admin") StatusPending else StatusResolved,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        actions = {
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Logout",
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}
