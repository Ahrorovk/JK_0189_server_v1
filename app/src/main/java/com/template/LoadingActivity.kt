package com.template

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import com.template.ui.theme.JK_0189_server_v1Theme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


@AndroidEntryPoint
class LoadingActivity:ComponentActivity(),CoroutineScope {
    @Inject

    lateinit var dataStoreManager: DataStoreManager

    private lateinit var job: Job
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main + CoroutineName("Activity Scope") + CoroutineExceptionHandler { coroutineContext, throwable ->
            println("Exception $throwable in context:$coroutineContext")
        }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setContent {
            JK_0189_server_v1Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background,
                ) {
                    if (Build.VERSION.SDK_INT >= 33) {
                        Permissions()
                    }
                    val viewModel = hiltViewModel<FirestoreViewModel>()
                    val data = viewModel.getLink().observeAsState().value

                    if(isConnectedToInternet(context = applicationContext)) {
                        dataStoreManager.getIsInServer.onEach { isInServer ->
                            when (isInServer) {
                                0 -> {
                                    data?.let { dataResp ->
                                        if (dataResp.isNotEmpty() && dataResp != "Failed to get document because the client is offline.") {
                                            Log.e("Firebase Connect", "True")
                                            viewModel.getFromServer(
                                                dataResp,
                                                applicationContext.packageName,
                                                UUID.randomUUID().toString(),
                                                SimpleTimeZone.getDefault().id
                                            )
                                        }
                                        else if (dataResp == "Failed to get document because the client is offline.") {
                                            Log.e("Firebase Disconnect","True")
                                            CoroutineScope(Dispatchers.Default).launch {
                                                dataStoreManager.updateIsInServer(0)
                                            }
                                            val intent =
                                                Intent(
                                                    applicationContext,
                                                    MainActivity::class.java
                                                )
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            applicationContext.startActivity(intent)

                                            val resultIntent = Intent()
                                            resultIntent.putExtra(
                                                "resultKey",
                                                "Some result data"
                                            )
                                            setResult(Activity.RESULT_OK, resultIntent)
                                            finish()
                                        } else {
                                            CoroutineScope(Dispatchers.Default).launch {
                                                dataStoreManager.updateIsInServer(1)
                                            }
                                        }
                                    }
                                }
                                1 -> {
                                    val intent =
                                        Intent(applicationContext, MainActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    applicationContext.startActivity(intent)

                                    val resultIntent = Intent()
                                    resultIntent.putExtra("resultKey", "Some result data")
                                    setResult(Activity.RESULT_OK, resultIntent)
                                    finish()
                                }
                                2 -> {
                                    val intent =
                                        Intent(applicationContext, WebActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    applicationContext.startActivity(intent)

                                    val resultIntent = Intent()
                                    resultIntent.putExtra("resultKey", "Some result data")
                                    setResult(Activity.RESULT_OK, resultIntent)
                                    finish()
                                }
                            }
                        }.launchIn(this)
                    }
                    else {
                        LaunchedEffect(key1 = true) {
                            val intent =
                                Intent(applicationContext, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            applicationContext.startActivity(intent)

                            val resultIntent = Intent()
                            resultIntent.putExtra("resultKey", "Some result data")
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        }
                    }
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column( horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_launcher_background),
                                    contentDescription = null
                                )
                                Image(
                                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                    contentDescription = null
                                )
                            }
                            Spacer(modifier = Modifier.padding(15.dp))
                            LinearProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun isConnectedToInternet(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connectivityManager.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}