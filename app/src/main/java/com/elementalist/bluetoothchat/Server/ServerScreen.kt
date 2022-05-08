package com.elementalist.bluetoothchat.Server

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ServerScreen(viewModel: ServerViewModel = ServerViewModel()) {
    val displayState = viewModel.displayState

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(displayState) { text ->
            Text(text = text)
            Divider(Modifier.padding(3.dp), color = Color.Green)
        }
    }
}

