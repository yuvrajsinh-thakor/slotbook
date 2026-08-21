package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun AuthScreen(
    viewModel: MainViewModel,
    onAuthSuccess: () -> Unit
) {
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val lockoutRemainingMs by viewModel.lockoutRemainingMs.collectAsState()
    val timeManipulation by viewModel.timeManipulationDetected.collectAsState()
    var inputPassword by remember { mutableStateOf("") }
    
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            onAuthSuccess()
        }
    }
    
    LaunchedEffect(lockoutRemainingMs) {
        if (lockoutRemainingMs > 0) {
            while (viewModel.lockoutRemainingMs.value > 0) {
                delay(1000)
                viewModel.updateLockoutTimer()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (timeManipulation) {
            Text("Security Error: Time manipulation detected.", color = MaterialTheme.colorScheme.error)
            return@Column
        }

        OutlinedTextField(
            value = inputPassword,
            onValueChange = { if (it.length <= 8) inputPassword = it.filter { char -> char.isDigit() } },
            label = { Text("Enter 8-digit Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            isError = lockoutRemainingMs > 0
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.verifyPassword(inputPassword) },
            enabled = inputPassword.length == 8 && lockoutRemainingMs <= 0
        ) {
            Text("Verify")
        }

        if (lockoutRemainingMs > 0) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Locked out. Try again in ${lockoutRemainingMs / 1000} seconds",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
