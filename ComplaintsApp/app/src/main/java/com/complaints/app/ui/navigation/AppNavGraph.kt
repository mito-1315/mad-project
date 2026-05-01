package com.complaints.app.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.complaints.app.ui.admin.AdminPanelScreen
import com.complaints.app.ui.auth.LoginScreen
import com.complaints.app.ui.auth.SignupScreen
import com.complaints.app.ui.student.StudentDashboardScreen
import com.complaints.app.ui.student.SubmitComplaintScreen
import com.complaints.app.util.SessionManager

/**
 * AppNavGraph — the central navigation host.
 *
 * Mirrors App.js routing logic:
 *  - If not logged in → Login
 *  - If admin → AdminPanel
 *  - If student → StudentDashboard
 *
 * Protected routes check SessionManager before rendering.
 */
@Composable
fun AppNavGraph(navController: NavHostController) {

    val user by SessionManager.currentUser.collectAsState()

    // Determine start destination based on session
    val startDestination = when {
        user == null         -> Screen.Login.route
        user!!.role == "admin" -> Screen.AdminPanel.route
        else                 -> Screen.StudentDashboard.route
    }

    NavHost(
        navController  = navController,
        startDestination = startDestination
    ) {
        // ── Public routes ─────────────────────────────────
        composable(Screen.Login.route) {
            // If already logged in, redirect
            LaunchedEffect(user) {
                if (user != null) {
                    val dest = if (user!!.role == "admin") Screen.AdminPanel.route
                               else Screen.StudentDashboard.route
                    navController.navigate(dest) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }
            LoginScreen(
                onLoginSuccess = { role ->
                    val dest = if (role == "admin") Screen.AdminPanel.route
                               else Screen.StudentDashboard.route
                    navController.navigate(dest) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onGoToSignup = {
                    navController.navigate(Screen.Signup.route)
                }
            )
        }

        composable(Screen.Signup.route) {
            LaunchedEffect(user) {
                if (user != null) {
                    val dest = if (user!!.role == "admin") Screen.AdminPanel.route
                               else Screen.StudentDashboard.route
                    navController.navigate(dest) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                }
            }
            SignupScreen(
                onSignupSuccess = { role ->
                    val dest = if (role == "admin") Screen.AdminPanel.route
                               else Screen.StudentDashboard.route
                    navController.navigate(dest) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                },
                onGoToLogin = { navController.popBackStack() }
            )
        }

        // ── Student routes (protected) ────────────────────
        composable(Screen.StudentDashboard.route) {
            // Guard: not logged in → login
            LaunchedEffect(user) {
                if (user == null) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                } else if (user!!.role == "admin") {
                    navController.navigate(Screen.AdminPanel.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            StudentDashboardScreen(
                onSubmitNew = { navController.navigate(Screen.SubmitComplaint.route) },
                onLogout    = {
                    SessionManager.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SubmitComplaint.route) {
            LaunchedEffect(user) {
                if (user == null) navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            SubmitComplaintScreen(
                onSuccess = {
                    navController.navigate(Screen.StudentDashboard.route) {
                        popUpTo(Screen.StudentDashboard.route) { inclusive = true }
                    }
                },
                onCancel  = { navController.popBackStack() }
            )
        }

        // ── Admin routes (protected + admin-only) ─────────
        composable(Screen.AdminPanel.route) {
            LaunchedEffect(user) {
                when {
                    user == null           -> navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                    user!!.role != "admin" -> navController.navigate(Screen.StudentDashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            AdminPanelScreen(
                onLogout = {
                    SessionManager.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
