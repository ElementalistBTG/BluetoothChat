package com.elementalist.bluetoothchat.Server

import android.bluetooth.BluetoothDevice
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ServerViewModel : ViewModel() {
    var displayState by mutableStateOf(listOf(""))
        private set

    fun addToDisplayState(text: String) {
        displayState = displayState.plus(text)
    }

    fun clearDisplayState() {
        displayState = listOf("")
    }

    init {
        addToDisplayState("Server Mode")
    }
}