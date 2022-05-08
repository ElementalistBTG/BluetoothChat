package com.elementalist.bluetoothchat.Client

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.elementalist.bluetoothchat.Server.AcceptThread

class ClientViewModel : ViewModel() {
    var displayState by mutableStateOf(listOf(""))
        private set

    fun addToDisplayState(text: String) {
        displayState = displayState.plus(text)
    }

    fun clearDisplayState() {
        displayState = listOf("")
    }

    init {
        addToDisplayState("Client set up")
    }
}