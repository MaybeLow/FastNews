package com.example.newsapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.newsapp.R
import com.example.newsapp.activities.ArticleActivity
import com.example.newsapp.managers.NotificationReceiver
import com.example.newsapp.managers.channelID
import com.example.newsapp.managers.notificationID
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * A deprecated class that receives notifications from the firebase messaging service.
 * The class was deprecated due to the notifications' inconsistency
 */
class MyFirebaseMessagingService: FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        createNotificationChannel()
    }

    /**
     * Create a new notification to be displayed for the user
     */
    private fun createNotificationChannel() {
        val name = "Test title"
        val description = "Test description"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelID, name, importance)
        channel.description = description
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        Intent(this, NotificationReceiver::class.java)

        val intent = Intent(this, ArticleActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, channelID)
            .setSmallIcon(R.drawable.alpine)
            .setContentTitle("headline.title")
            .setContentText("headline.description")
            .setContentIntent(pendingIntent)
            .build()

        val manager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.notify(notificationID, notification)
    }
}
