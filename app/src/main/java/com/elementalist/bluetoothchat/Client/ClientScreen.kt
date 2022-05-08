package com.elementalist.bluetoothchat.Client

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp


@Composable
fun ClientScreen(viewModel : ClientViewModel = ClientViewModel()) {

    val displayState = viewModel.displayState

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(displayState) { text ->
                Text(text = text)
                Divider(Modifier.padding(3.dp), color = Color.Green)
            }
        }

//        if (pairedState.device != null) {
//            Button(onClick = { clientSetUp(pairedState.device) }) {
//                Text(text = "send data")
//            }
//        }
    }

//    if (pairedDevice != null) {
//        clientSetUp(pairedDevice)
//    }
//
//
//    Text(text = "Paired with device: ${state.device?.name}")
//    Log.i(MY_TAG, "Device name: ${state.device?.name}")
//    Log.i(MY_TAG, "Device uuids: ${state.device?.uuids}")
//    Log.i(MY_TAG, "Device address: ${state.device?.address}")
}

