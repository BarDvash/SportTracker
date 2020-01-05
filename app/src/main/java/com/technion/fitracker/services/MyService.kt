package com.technion.fitracker.services

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.technion.fitracker.R
import com.technion.fitracker.login.FlashSignInActivity


class MyService : FirebaseMessagingService() {
    private val CHANNEL_ID: String =  "M_CH_ID"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

// Create an explicit intent for an Activity in your app
        val intent = Intent(this, FlashSignInActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        //
        val notification_title : String?
        val notification_body: String?
        //

        if (remoteMessage.notification != null) {
//            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody())
            notification_title = remoteMessage.notification!!.title
            notification_body = remoteMessage.notification!!.body
        }
        else {
            return
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(notification_title)
                .setContentText(notification_body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)


        val notificationId = 1 //TODO: default id for now, if it important change it maybe according to the notification kind

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(notificationId, builder.build())
        }

    }






}