package com.complaints.app.ui.navigation

/**
 * Screen route definitions — equivalent to React Router path strings.
 */
sealed class Screen(val route: String) {
    object Login            : Screen("login")
    object Signup           : Screen("signup")
    object StudentDashboard : Screen("student_dashboard")
    object SubmitComplaint  : Screen("submit_complaint")
    object AdminPanel       : Screen("admin_panel")
}
