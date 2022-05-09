package com.elementalist.bluetoothchat.Client

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.*
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import com.elementalist.bluetoothchat.MY_TAG
import com.elementalist.bluetoothchat.ui.theme.BluetoothChatTheme

class ClientActivity : ComponentActivity() {

    lateinit var pairedDevice: BluetoothDevice
    val viewModel by lazy { ClientViewModel() }

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val mDevice: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (mDevice?.bondState == BluetoothDevice.BOND_BONDED) {
                        Log.i(MY_TAG, "bonded")
                        viewModel.addToDisplayState("Device ${mDevice.name} bonded")
                        clientSetUp(mDevice)
                    } else if (mDevice?.bondState == BluetoothDevice.BOND_BONDING) {
                        viewModel.addToDisplayState("Creating bonding with device: ${mDevice.name}")
                        Log.i(MY_TAG, "bonding")
                        //pairedDevice = mDevice
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(receiver, filter)

        if (!bluetoothAdapter.isEnabled) {
            enableBluetooth()
        }

        companionModeOn()

        setContent {
            BluetoothChatTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ClientScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private val enableBluetoothResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == ComponentActivity.RESULT_OK) {
            Toast.makeText(this, "Bluetooth Enabled!", Toast.LENGTH_SHORT).show()
            viewModel.addToDisplayState("Bluetooth Enabled")
            //companionModeOn()
        } else {
            Log.i(MY_TAG, result.resultCode.toString())
            Log.i(MY_TAG, result.data.toString())
            Toast.makeText(this, "Bluetooth is required for this app to run", Toast.LENGTH_SHORT)
                .show()
            this.finish()
        }
    }

    private fun enableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enableBluetoothResultLauncher.launch(enableBtIntent)
    }

    private val companionDeviceManager: CompanionDeviceManager by lazy {
        getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager
    }

    @SuppressLint("MissingPermission")
    private val pairBluetoothDeviceResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode == ComponentActivity.RESULT_OK) {
            Log.i(MY_TAG, "result code OK")
            viewModel.addToDisplayState("Device selected. Pairing process begins")
            val data = activityResult.data
            //the user chose to pair the app with a bluetooth device
            val deviceToPair: BluetoothDevice? =
                data?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
            deviceToPair?.let { device ->
                device.createBond()
                viewModel.addToDisplayState("Pairing with device: ${device.name}")
                //maintain continuous interaction with a paired device
            }
        } else {
            Toast.makeText(this, "Device not paired", Toast.LENGTH_SHORT)
                .show()
            Log.i(MY_TAG, "Device not paired")
        }
    }



    private fun companionModeOn() {
        val deviceFilter: BluetoothDeviceFilter = BluetoothDeviceFilter.Builder()
            //.addServiceUuid(ParcelUuid(UUID(0x123abcL,-1L)),null)
            //.setNamePattern(Pattern.compile("raspberry"))
            .build()

        val pairingRequest: AssociationRequest = AssociationRequest.Builder()
            .addDeviceFilter(deviceFilter)
            //stop scanning as soon as one device matching the filter is found
            //.setSingleDevice(true)
            .build()

        //when the user tries to pair with a Bluetooth device we show a dialog
        companionDeviceManager.associate(
            pairingRequest,
            object : CompanionDeviceManager.Callback() {
                override fun onDeviceFound(chooserLauncher: IntentSender?) {
                    Log.i(MY_TAG, "Device found")
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(
                            chooserLauncher!!
                        ).build()

                    pairBluetoothDeviceResultLauncher.launch(intentSenderRequest)
                }

                override fun onFailure(error: CharSequence?) {
                    Toast.makeText(
                        applicationContext,
                        "Failure on companion with error: $error",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.i(MY_TAG, "Companion Error: $error")
                }
            }, null
        )
    }

    fun clientSetUp(device: BluetoothDevice) {
        viewModel.addToDisplayState("Initializing RFCOMM for data transfer")
        ConnectThread(
            device = device)
    }


}