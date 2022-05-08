package com.elementalist.bluetoothchat

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startActivity
import com.elementalist.bluetoothchat.Client.ClientActivity
import com.elementalist.bluetoothchat.Server.ServerActivity
import com.elementalist.bluetoothchat.ui.theme.BluetoothChatTheme

private const val SELECT_DEVICE_REQUEST_CODE = 0
private const val REQUEST_BT_CONNECT = 1


//https://towardsdatascience.com/sending-data-from-a-raspberry-pi-sensor-unit-over-serial-bluetooth-f9063f3447af
//https://github.com/ikolomiyets/bluetooth-test/blob/master/app/src/main/java/io/iktech/demo/bttest/MainActivity.kt
//https://github.com/android/connectivity-samples/tree/master/BluetoothChat


class MainActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BluetoothChatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        val intentClient = Intent(LocalContext.current, ClientActivity::class.java)
        val context = LocalContext.current
        Button(modifier = Modifier.fillMaxWidth(),
            onClick = { startActivity(context, intentClient, null) }) {
            Text(text = "Client")
        }
        val intentServer = Intent(LocalContext.current, ServerActivity::class.java)
        Button(modifier = Modifier.fillMaxWidth(),
            onClick = { startActivity(context, intentServer, null) }) {
            Text(text = "Server")
        }
    }
}







