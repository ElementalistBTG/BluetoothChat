package com.elementalist.bluetoothchat.Server

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.elementalist.bluetoothchat.MY_TAG
import com.elementalist.bluetoothchat.connectionName
import com.elementalist.bluetoothchat.myUuid
import java.io.IOException

/**
 * Thread for receiving data once from socket
 *
 * @property socket
 * @property viewModel
 */
class BluetoothServer(
    private val socket: BluetoothSocket,
    private val viewModel: ServerViewModel
) : Thread() {
    private val inputStream = socket.inputStream

    override fun run() {
        while (true) {
            try {
                //Read from the InputStream
                //We only need 1Byte for reading 0 or 1 from raspberry result
                val buffer = ByteArray(1)
                inputStream.read(buffer)
                val text = String(buffer)
                Log.i(MY_TAG, "Message received: $text")
                // Send the obtained bytes to the UI activity.
                viewModel.changeStateOfServer(
                    newState = StatesOfServer.RESPONSE_RECEIVED,
                    dataReceived = text
                )
            } catch (e: IOException) {
                Log.i(MY_TAG, "Input stream was disconnected", e)
                break
            } finally {
                inputStream.close()
                socket.close()
            }

        }
    }
}

/**
 * Thread for creating socket for incoming connections
 *
 * @constructor
 *
 * @param bluetoothAdapter
 * we use the bluetoothAdapter to open socket
 * @param viewModel
 * we use the viewModel to update the state when receiving data
 */
@SuppressLint("MissingPermission")
class AcceptThread(
    bluetoothAdapter: BluetoothAdapter,
    private val viewModel: ServerViewModel
) : Thread() {

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
                Log.e(MY_TAG, "Socket's accept() method failed", e)
                viewModel.changeStateOfServer(
                    newState = StatesOfServer.ERROR,
                    dataReceived = e.localizedMessage
                )
                shouldLoop = false
                null//we pass null to socket
            }

            socket?.also {
                BluetoothServer(it, viewModel).start()
                //we close the socket after
                mmServerSocket?.close()
                shouldLoop = false
            }
        }
    }
}