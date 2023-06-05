package com.template

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.web.WebView
import com.google.accompanist.web.WebViewState
import com.google.accompanist.web.rememberWebViewState
import com.template.ui.theme.JK_0189_server_v1Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WebActivity:ComponentActivity() {

    @RequiresApi(33)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JK_0189_server_v1Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Permissions()
                    BackHandler(enabled = true, onBack = {
                    })
                    val viewModel = hiltViewModel<FirestoreViewModel>()
                    val textState = remember { mutableStateOf(viewModel.serverUrlState.value) }
                    val state = rememberWebViewState(url = textState.value)
                    WebView_(siteName = state)
                }
            }
        }
    }
}

@Composable
fun WebView_(siteName: WebViewState){
    WebView(state = siteName)
}
