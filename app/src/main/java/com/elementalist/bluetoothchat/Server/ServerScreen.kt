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

//    LazyColumn(modifier = Modifier.fillMaxWidth()) {
//        items(displayState) { text ->
//            Text(text = text)
//            Divider(Modifier.padding(3.dp), color = Color.Green)
//        }
//    }

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
            if (buttonText != "") {
                Button(modifier = Modifier.align(Alignment.Center), onClick = { buttonAction() }) {
                    Text(text = buttonText)
                }
            }
        }
        //Spacer(modifier = Modifier.padding(2.dp))
        //description Text
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

