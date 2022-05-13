package com.elementalist.bluetoothchat.Client

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.elementalist.bluetoothchat.MY_TAG

class ClientViewModel(
    private val bluetoothAdapter: BluetoothAdapter
) : ViewModel() {

    var discoveredDevices = mutableStateListOf<BluetoothDevice>()
        private set

    fun addDiscoveredDevice(device: BluetoothDevice) {
        if (!discoveredDevices.contains(device)) {
            discoveredDevices.add(device)
        }
    }

    var pairedDevices = mutableStateListOf<BluetoothDevice>()
        private set

    private fun addPairedDevice(device: BluetoothDevice) {
        pairedDevices.add(device)
    }

    @SuppressLint("MissingPermission")
    fun scanForDevices() {
        val pairedDevicesFound: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
        for (pairDevice in pairedDevicesFound) {
            if (!pairedDevices.contains(pairDevice)) {
                addPairedDevice(pairDevice)
            }
        }
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        bluetoothAdapter.startDiscovery()
        Log.i(MY_TAG, "discovery started")
    }

    var selectedDevice by mutableStateOf<BluetoothDevice?>(null)
        private set

    fun selectDevice(device: BluetoothDevice) {
        selectedDevice = device
    }

    /**
     * Connect to the selected device and send data
     *
     */
    @SuppressLint("MissingPermission")
    fun sendDataToDevice() {
        // Cancel discovery because it otherwise slows down the connection.
        bluetoothAdapter.cancelDiscovery()
        //Send data to selected device
        selectedDevice?.let {
            ConnectThread(it).start()
        }
    }

}