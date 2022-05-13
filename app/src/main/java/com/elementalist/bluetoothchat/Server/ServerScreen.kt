package com.elementalist.bluetoothchat.Server

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


@Composable
fun ServerScreen(viewModel: ServerViewModel = ServerViewModel()) {
    //we observe some viewModel's variables to dynamically change the screen
    val buttonAction = viewModel.buttonAction
    val buttonText = viewModel.buttonText
    val displayedText = viewModel.displayedText
    val imageShown = viewModel.image

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
            .border(5.dp, MaterialTheme.colors.secondary)
            .padding(5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, true)
        ) {
            //hide button if it is not needed
            if (buttonText != "") {
                Button(modifier = Modifier.align(Alignment.Center), onClick = { buttonAction() }) {
                    Text(text = buttonText)
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .weight(1f, true)
        ) {
            Text(text = displayedText, textAlign = TextAlign.Center)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(6f, true)
        ) {
            //We hide the image-result when it is not needed
            if (imageShown != 0) {
                Image(
                    painter = painterResource(imageShown),
                    contentDescription = "Result from sniffing",
                    contentScale = ContentScale.Fit
                )
            }

        }

    }
}

