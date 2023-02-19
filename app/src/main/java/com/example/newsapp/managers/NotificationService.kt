package com.example.newsapp.managers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.newsapp.R
import com.example.newsapp.activities.ArticleActivity
import com.example.newsapp.data.ApiData
import com.example.newsapp.data.Headline
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Service that displays push notifications to the user every hour,
 * in case a new article of interest is available
 */
class NotificationService: Service() {
    private var mIntentFilter: IntentFilter? = null

    // Broadcast that ends the service in case the main activity is getting killed
    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                getString(R.string.end_activities) -> {
                    onDestroy()
                }
            }
        }
    }

    private var apiData: ApiData? = null
    private var lastHtmlResponse: String = ""
    private var lastHeadlines: MutableList<Headline>? = null

    private lateinit var job: Job

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * Start the service, register the broadcast receiver and launch the coroutine
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        this.mIntentFilter = IntentFilter()
        mIntentFilter!!.addAction("end_activities")

        registerReceiver(mReceiver, mIntentFilter)

        Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show()
        // Normally, the service should wait for 30 to 60 minutes to check
        // if there is a new article to display. However, due to testing
        // purposes, the service will call news api every minute
        job = GlobalScope.launch {
            while (true) {
                delay(30000)
                listenToNewsUpdate()
                delay(30000)
            }
        }

        return START_STICKY
    }

    /**
     * Get the information about news articles that the user wants to receive updated for
     */
    private suspend fun listenToNewsUpdate() {
        val db = Firebase.firestore
        val auth = Firebase.auth
        val database = db.collection("user").document(auth.currentUser!!.uid).get()
        database.await()

        // The user can choose whether or not to receive notifications from the Fast News app
        // The service constantly works, even if the user chose to not be notified
        if (database.result.data!!["beNotified"] == true) {
            var categories = ""
            var countries = ""
            var languages = ""
            var categoryData = mutableListOf<String>()
            if (database.result.data!!["notifications"] != null) {
                categoryData = database.result.data!!["notifications"] as MutableList<String>
            }
            if (categoryData.isNotEmpty()) {
                categories = "&category=" + categoryData.joinToString(separator = ",")
            }

            var countryData = mutableListOf<String>()
            if (database.result.data!!["countries"] != null) {
                countryData = database.result.data!!["countries"] as MutableList<String>
            }
            if (countryData.isNotEmpty()) {
                countries = "&country=" + countryData.joinToString(separator = ",")
            }

            var languageData = mutableListOf<String>()
            if (database.result.data!!["languages"] != null) {
                languageData = database.result.data!!["languages"] as MutableList<String>
            }
            if (languageData.isNotEmpty()) {
                languages = "&language=" + languageData.joinToString(separator = ",")
            }

            // Call the news api
            val url = URL("https://newsdata.io/api/1/news?apikey=pub_14178cd08bf1a5c4fcf7d03e3c2f3be0f78fc$categories$countries$languages")
            val connection = url.openConnection() as HttpsURLConnection
            if (connection.responseCode == 200) {
                // Store the latest response to compare with the previous one
                // and see if there are any changes
                val httpResponse = connection.inputStream
                val httpReader = InputStreamReader(httpResponse, "UTF-8")
                val responseContent = httpReader.readText()

                if (lastHtmlResponse != responseContent && responseContent != "") {
                    lastHtmlResponse = responseContent
                    val connection2 = url.openConnection() as HttpsURLConnection
                    val inputStream = connection2.inputStream
                    val inputStreamReader = InputStreamReader(inputStream, "UTF-8")
                    apiData = Gson().fromJson(inputStreamReader, ApiData::class.java)
                    Log.w(ContentValues.TAG, "apiData content: $apiData")

                    createNotificationChannel(apiData!!.results)

                    inputStreamReader.close()
                    inputStream.close()
                }
            } else {
                println(connection.responseCode)
            }
        }
    }

    /**
     * Method that creates and displays notifications to the user, if there are any updated
     * @param headlines The news headlines, that the user chose to get notifications for
     */
    private fun createNotificationChannel(headlines: MutableList<Headline>) {
        // Compare the newest api call, with the most recent one
        var uniqueHeadlines = headlines
        if (lastHeadlines != null) {
            uniqueHeadlines = headlines.minus(lastHeadlines) as MutableList<Headline>
        }
        Log.w(ContentValues.TAG, "unique headlines $uniqueHeadlines")
        // Get the unique news articles and display in case there is a new one
        if (uniqueHeadlines.isNotEmpty()) {
            val headline = uniqueHeadlines[0]

            val name = headline.title
            val description = headline.description
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelID, name, importance)
            channel.description = description
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Intent(this, NotificationReceiver::class.java)

            val intent = Intent(this, ArticleActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            intent.putExtra("title", headline.title)
            intent.putExtra("source_id", headline.source_id)
            intent.putExtra("pubDate", headline.pubDate)
            intent.putExtra("image_url", headline.image_url)
            intent.putExtra("content", headline.content)
            intent.putExtra("description", headline.description)

            val pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            val notification = NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.drawable.alpine)
                .setContentTitle(headline.title)
                .setContentText(headline.description)
                .setContentIntent(pendingIntent)
                .build()

            val manager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            manager.notify(notificationID, notification)
        }
    }

    /**
     * When destroyed, the service terminates its job
     * and unregisters the broadcast receiver
     */
    override fun onDestroy() {
        unregisterReceiver(mReceiver)
        job.cancel()
        super.onDestroy()
    }
}
