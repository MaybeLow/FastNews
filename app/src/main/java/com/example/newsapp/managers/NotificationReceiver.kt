package com.example.newsapp.managers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

const val notificationID = 1
const val channelID = "channel1"

/**
 * Class that receives broadcast and restores the service to work when the app is killed.
 * The broadcast receiver is deprecated for the app due to its inconsistency.
 * Thus, the class is only used for its constant variables
 */
class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i("Broadcast Listened", "Service tried to stop")

        context.startForegroundService(Intent(context, NotificationService::class.java))
    }
}
