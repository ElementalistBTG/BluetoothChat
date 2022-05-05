package com.elementalist.bluetoothchat

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException

class BluetoothServer(private val socket: BluetoothSocket) : Thread() {
    private val inputStream = this.socket.inputStream

    override fun run() {
        try {
            val available = inputStream.available()
            val bytes = ByteArray(available)
            Log.i("server", "Reading")
            inputStream.read(bytes, 0, available)
            val text = String(bytes)
            Log.i("server", "Message received")
            Log.i("server", text)

        } catch (e: Exception) {
            Log.i("client", "Cannot read data", e)
        } finally {
            inputStream.close()
            socket.close()
        }
    }
}

@SuppressLint("MissingPermission")
class AcceptThread(bluetoothAdapter: BluetoothAdapter) : Thread() {

    private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
        bluetoothAdapter.listenUsingRfcommWithServiceRecord(connectionName, myUuid)
    }

    override fun run() {
        //keep listening until exception occurs or a socket is returned
        var shouldLoop = true
        while (shouldLoop) {
            val socket: BluetoothSocket? = try {
                mmServerSocket?.accept()
            } catch (e: IOException) {
                Log.i(MY_TAG, "Socket's accept method failed")
                shouldLoop = false
                null
            }
            socket?.also {
                BluetoothServer(it)
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