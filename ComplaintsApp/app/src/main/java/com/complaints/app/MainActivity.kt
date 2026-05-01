package com.complaints.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.complaints.app.ui.navigation.AppNavGraph
import com.complaints.app.ui.theme.ComplaintsAppTheme

/**
 * MainActivity — Single Activity that hosts the entire Compose navigation graph.
 * Equivalent to the React <App /> component with <BrowserRouter>.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComplaintsAppTheme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }
}
