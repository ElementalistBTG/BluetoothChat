package com.elementalist.bluetoothchat

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var state by mutableStateOf(PairedState())
        private set

    fun setPairedDevice(device: BluetoothDevice){
        state = state.copy(
            device = device,
            isLoading = false
        )
    }

}

data class PairedState(
    val device: BluetoothDevice? = null,
    val isLoading: Boolean = true
)