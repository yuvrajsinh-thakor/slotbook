package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.AuthScreen
import com.example.ui.ConfigScreen
import com.example.ui.ConfigViewModel
import com.example.ui.WebViewScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          AppNavigation(modifier = Modifier.padding(innerPadding))
        }
      }
    }
  }
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = viewModel()
    val configViewModel: ConfigViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "auth",
        modifier = modifier
    ) {
        composable("auth") {
            AuthScreen(
                viewModel = mainViewModel,
                onAuthSuccess = {
                    navController.navigate("config") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }
        
        composable("config") {
            ConfigScreen(
                viewModel = configViewModel,
                onStartAutomation = {
                    navController.navigate("webview")
                }
            )
        }
        
        composable("webview") {
            WebViewScreen(configViewModel = configViewModel)
        }
    }
}
