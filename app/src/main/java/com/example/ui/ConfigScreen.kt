package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class ConfigViewModel : ViewModel() {
    val applicationNumber = MutableStateFlow("3484063226")
    val dateOfBirth = MutableStateFlow("20-10-1987")
    val cov = MutableStateFlow("10002")
    val track = MutableStateFlow("GJ19TRK   ")
    val targetTime = MutableStateFlow("16.00-17.00")
    val triggerMs = MutableStateFlow("100")
}

@Composable
fun ConfigScreen(
    viewModel: ConfigViewModel,
    onStartAutomation: () -> Unit
) {
    val applicationNumber by viewModel.applicationNumber.collectAsState()
    val dateOfBirth by viewModel.dateOfBirth.collectAsState()
    val cov by viewModel.cov.collectAsState()
    val track by viewModel.track.collectAsState()
    val targetTime by viewModel.targetTime.collectAsState()
    val triggerMs by viewModel.triggerMs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Automation Configuration", style = MaterialTheme.typography.titleLarge)
        
        OutlinedTextField(
            value = applicationNumber,
            onValueChange = { viewModel.applicationNumber.value = it },
            label = { Text("Application Number") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = dateOfBirth,
            onValueChange = { viewModel.dateOfBirth.value = it },
            label = { Text("Date of Birth (dd-mm-yyyy)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = cov,
            onValueChange = { viewModel.cov.value = it },
            label = { Text("COV Value (e.g., 10002 for 4-Wheeler)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = track,
            onValueChange = { viewModel.track.value = it },
            label = { Text("Track Value (e.g., GJ19TRK   )") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = targetTime,
            onValueChange = { viewModel.targetTime.value = it },
            label = { Text("Preferred Time Slot (e.g., 16.00-17.00)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = triggerMs,
            onValueChange = { viewModel.triggerMs.value = it },
            label = { Text("Trigger Time (ms after 8:00:00)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Button(
            onClick = onStartAutomation,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Automation")
        }
    }
}
