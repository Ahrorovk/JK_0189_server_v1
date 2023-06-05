package com.template

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


const val channel_name="com.template"
const val channel_id="notification_channel"
class MyFirebaseMessagingService: FirebaseMessagingService() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.notification != null) {
            generateNotification(remoteMessage.notification!!.title?:"", remoteMessage.notification!!.body?:"")
        }
    }
    fun getRemoteView(title: String, body:String): RemoteViews {
        val remoteView= RemoteViews(channel_name, R.layout.notification)
        remoteView.setTextViewText(R.id.title, title)
        remoteView.setTextViewText(R.id.message, body)
        remoteView.setImageViewResource(R.id.app_logo, R.drawable.ic_launcher_foreground)
        return remoteView
    }
    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("UnspecifiedImmutableFlag")
    fun generateNotification(title:String, body:String){
        val intent= Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)

        var builder: NotificationCompat.Builder=
            NotificationCompat.Builder(applicationContext, channel_id)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setVibrate(longArrayOf(2000, 2000, 2000, 2000))
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
        builder=builder.setContent(getRemoteView(title, body))

        val notificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel=
                NotificationChannel(channel_id, channel_name, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(0, builder.build())
    }
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e("FCM_TOKENN","-> $token")
    }
    override fun onCreate() {
        super.onCreate()
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful){
                return@addOnCompleteListener
            }
            val token=task.result
            Log.e("Hello", "FCM-Token-> \n $token")}
    }
}