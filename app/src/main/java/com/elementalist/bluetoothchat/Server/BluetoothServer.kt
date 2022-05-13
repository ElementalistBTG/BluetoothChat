package com.elementalist.bluetoothchat.Server

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elementalist.bluetoothchat.MY_TAG
import com.elementalist.bluetoothchat.connectionName
import com.elementalist.bluetoothchat.myUuid
import java.io.IOException

class BluetoothServer(
    private val socket: BluetoothSocket,
    private val viewModel: ServerViewModel
) : Thread() {
    private val inputStream = socket.inputStream
    
    override fun run() {
        while (true) {
            try {
                // Read from the InputStream
                val buffer = ByteArray(1)
                inputStream.read(buffer)
                val text = String(buffer)
                Log.i(MY_TAG, "Message received")
                Log.i(MY_TAG, text)
                // Send the obtained bytes to the UI activity.
                viewModel.changeStateOfServer(
                    newState = StatesOfServer.RESPONSE_RECEIVED,
                    dataReceived = text
                )

                //myViewModel.setInfoState("Received message: $text")
//                val available = inputStream.available()
//                val bytes = ByteArray(available)
//                Log.i(MY_TAG, "Reading")
//                inputStream.read(bytes, 0, available)
//                val text = String(bytes)
//                Log.i(MY_TAG, "Message received")
//                Log.i(MY_TAG, text)

            } catch (e: IOException) {
                Log.i(MY_TAG, "Input stream was disconnected", e)
                viewModel.changeStateOfServer(
                    newState = StatesOfServer.ERROR,
                    dataReceived = e.localizedMessage
                )
                break
            } finally {
                inputStream.close()
                socket.close()
            }

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
    private val myViewModel = viewModel

    override fun run() {
        //keep listening until exception occurs or a socket is returned
        var shouldLoop = true
        //myViewModel.setInfoState("Server listening for connections")
        while (shouldLoop) {
            val socket: BluetoothSocket? = try {
                mmServerSocket?.accept()
            } catch (e: IOException) {
                //myViewModel.setInfoState("Socket's accept method failed")
                shouldLoop = false
                null
            }
            socket?.also {
                BluetoothServer(it, myViewModel).start()
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