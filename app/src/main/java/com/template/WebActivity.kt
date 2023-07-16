package com.template

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.viewinterop.AndroidView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.web.WebView
import com.google.accompanist.web.WebViewState
import com.google.accompanist.web.rememberWebViewState
import com.template.ui.theme.JK_0189_server_v1Theme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.Composable
import android.webkit.WebView
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
@AndroidEntryPoint
class WebActivity:ComponentActivity() {

    @RequiresApi(33)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val systemUiController = WindowCompat.getInsetsController(window,window.decorView)
        systemUiController?.hide(WindowInsetsCompat.Type.statusBars())
        setContent {
            JK_0189_server_v1Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Permissions()
                    val isState = remember {
                        mutableStateOf(false)
                    }
                        BackHandler {

                        }
                    val viewModel = hiltViewModel<FirestoreViewModel>()
                    val textState = remember { mutableStateOf(viewModel.serverUrlState.value) }
                    val state = rememberWebViewState(url = textState.value)
                    LaunchedEffect(key1 = true){
                        Log.e("URL_","${textState.value}")
                    }
//                    WebViewComponent("https://github.com")
                    WebView_(siteName = state)
//                    WebViewWithLoadingAnimation(url = textState.value)
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewComponent(url: String) {
    val webViewRef = remember { WebViewWrapper() }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                settings.loadsImagesAutomatically = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.allowFileAccess = true
                settings.allowFileAccessFromFileURLs = true
                settings.allowUniversalAccessFromFileURLs = true
                CookieManager.getInstance().setAcceptCookie(true)
                CookieManager.getInstance().setAcceptThirdPartyCookies(this,true)
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        view.loadUrl(url)
                        return true
                    }
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                        Log.d("WebView", "${consoleMessage.message()} -- From line ${consoleMessage.lineNumber()} of ${consoleMessage.sourceId()}")
                        return true
                    }
                }
            }
        },
        update = { webView ->
            webViewRef.webView = webView
            webView.loadUrl(url)
        }
    )
}

class WebViewWrapper {
    lateinit var webView: WebView
}

class WebViewManager {
    private val webViewsBackStack: MutableList<String> = mutableListOf()

    fun navigateToUrl(url: String) {
        webViewsBackStack.add(url)
        // Загрузка веб-страницы по заданному URL
    }

    fun canGoBack(): Boolean {
        return webViewsBackStack.size > 1
    }

    fun goBack(): Boolean {
        if (canGoBack()) {
            webViewsBackStack.removeAt(webViewsBackStack.size - 1)
            // Возврат на предыдущую веб-страницу
            return true
        }
        return false
    }
}



@Composable
fun WebView_(siteName: WebViewState){
    WebView(state = siteName, factory = { context ->
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.loadsImagesAutomatically = true
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            settings.allowFileAccess = true
            settings.allowFileAccessFromFileURLs = true
            settings.allowUniversalAccessFromFileURLs = true
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this,true)
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    view.loadUrl(url)
                    return true
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                    Log.d("WebView", "${consoleMessage.message()} -- From line ${consoleMessage.lineNumber()} of ${consoleMessage.sourceId()}")
                    return true
                }
            }
        }
    })
}
