package com.elementalist.bluetoothchat.Server


import android.bluetooth.BluetoothAdapter
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.elementalist.bluetoothchat.MY_TAG
import com.elementalist.bluetoothchat.R.drawable.nok
import com.elementalist.bluetoothchat.R.drawable.ok

class ServerViewModel : ViewModel() {

    lateinit var bluetoothAdapter: BluetoothAdapter

    var displayedText by mutableStateOf("Permissions granting phase")
        private set

    var buttonText by mutableStateOf("")
        private set

    var buttonAction by mutableStateOf({})

    var image by mutableStateOf(0)
        private set

    //Function to externally modify state for ServerScreen
    fun changeStateOfServer(
        newState: StatesOfServer,
        dataReceived: String? = null
    ) {
        when (newState) {
            StatesOfServer.APP_STARTED -> {
                displayedText = "Press button to start listening. Be sure to allow all permissions"
                buttonText = "Open server socket"
                buttonAction = { startServer() }
            }
            StatesOfServer.SERVER_STARTED -> {
                displayedText = "Server is listening for connections..."
                buttonText = "Restart Server?"
                buttonAction = { startServer() }
                image = 0
            }
            StatesOfServer.RESPONSE_RECEIVED -> {
                when {
                    dataReceived.equals("1") -> {
                        displayedText = "Your food is good for consumption"
                        buttonText = "Listen again for messages from raspberry?"
                        buttonAction = { startServer() }
                        image = ok
                    }
                    dataReceived.equals("0") -> {
                        displayedText = "You should NOT eat that food"
                        buttonText = "Listen again for messages from raspberry?"
                        buttonAction = { startServer() }
                        image = nok
                    }
                    else -> {
                        displayedText = "Not correct response message: $dataReceived"
                        buttonText = ""
                    }
                }
            }
            StatesOfServer.ERROR -> {
                buttonText = ""
                displayedText = "An error occurred: $dataReceived"
                image = 0
            }
        }
    }

    private fun startServer() {
        Log.i(MY_TAG, "server start")
        AcceptThread(bluetoothAdapter, this).start()
        changeStateOfServer(StatesOfServer.SERVER_STARTED)
    }

}

enum class StatesOfServer {
    APP_STARTED,
    SERVER_STARTED,
    ERROR,
    RESPONSE_RECEIVED
}


