package com.elementalist.bluetoothchat.Client

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.elementalist.bluetoothchat.MY_TAG
import com.elementalist.bluetoothchat.myUuid
import java.io.IOException


class BluetoothClient(private val socket: BluetoothSocket) : Thread() {

    override fun run() {
        Log.i(MY_TAG, "Sending")
        val outputStream = socket.outputStream
        try {
            //outputStream.write("1".encodeToByteArray())
            val message = "0".toByteArray()
            outputStream.write(message)
            //outputStream.flush()
            Log.i(MY_TAG, "Sent: $message")
        } catch (e: Exception) {
            Log.i(MY_TAG, "Cannot send $e", e)
        }
        //maybe not needed???
//        finally {
//            Log.i(MY_TAG, "finally")
//            outputStream.close()
//            socket.close()
//        }

    }
}

@SuppressLint("MissingPermission")
class ConnectThread(
    device: BluetoothDevice
) : Thread() {
    private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
        device.createRfcommSocketToServiceRecord(myUuid)
    }

    override fun run() {

        mmSocket?.let { socket ->
            //Connect to the remote device through the socket.
            // This call blocks until it succeeds or throws an exception
            try {
                Log.i(MY_TAG, "attempting connection")
                socket.connect()
                Log.i(MY_TAG, "connection success")
            } catch (e: Exception) {
                Log.i(MY_TAG, "connection was not successful")
            }
            //The connection attempt succeeded.
            //Perform work associated with the connection in a separate thread
            BluetoothClient(socket).start()
        }

        //Closes the client socket and causes the thread to finish
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.i(MY_TAG, "Could not close the client socket", e)
            }
        }

    }
}