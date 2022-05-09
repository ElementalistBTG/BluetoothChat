package com.elementalist.bluetoothchat.Server

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.elementalist.bluetoothchat.MY_TAG
import com.elementalist.bluetoothchat.connectionName
import com.elementalist.bluetoothchat.myUuid
import java.io.IOException

class BluetoothServer(private val socket: BluetoothSocket) : Thread() {
    private val inputStream = this.socket.inputStream

    override fun run() {
        try {
            val available = inputStream.available()
            val bytes = ByteArray(available)
            Log.i(MY_TAG, "Reading")
            inputStream.read(bytes, 0, available)
            val text = String(bytes)
            Log.i(MY_TAG, "Message received")
            Log.i(MY_TAG, text)

        } catch (e: Exception) {
            Log.i(MY_TAG, "Cannot read data", e)
        } finally {
            inputStream.close()
            socket.close()
        }
    }
}


@SuppressLint("MissingPermission")
class AcceptThread(
    bluetoothAdapter: BluetoothAdapter,
    viewModel: ServerViewModel
) : Thread() {
        
    private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
        bluetoothAdapter.listenUsingRfcommWithServiceRecord(connectionName, myUuid)
    }
    val myViewModel = viewModel

    override fun run() {
        //keep listening until exception occurs or a socket is returned
        var shouldLoop = true
        myViewModel.addToDisplayState("Server listening for connections")
        while (shouldLoop) {
            val socket: BluetoothSocket? = try {
                mmServerSocket?.accept()
            } catch (e: IOException) {
                myViewModel.addToDisplayState("Socket's accept method failed")
                shouldLoop = false
                null
            }
            myViewModel.addToDisplayState("socket used")
            socket?.also {
                BluetoothServer(it).start()
                mmServerSocket?.close()
                shouldLoop = false
            }
        }
    }

    //closes the connect socket and causes the thread to finish
    fun cancel() {
        try {
            mmServerSocket?.close()
        } catch (e: IOException) {
            Log.i(MY_TAG, "Could not close the connect socket", e)
        }
    }
}