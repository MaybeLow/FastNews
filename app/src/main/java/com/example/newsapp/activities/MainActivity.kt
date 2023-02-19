package com.example.newsapp.activities


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.example.newsapp.R
import com.example.newsapp.managers.NotificationService
import com.example.newsapp.tabadapters.MainTabAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * The main activity that gets displayed when a user logs in
 */
class MainActivity : AppCompatActivity() {
    private var mIntentFilter: IntentFilter? = null

    // Intent that will listen to the broadcast and end the activity
    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                getString(R.string.end_activities) -> {
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If the user cannot ge authenticated, send him to the guest activity
        if (Firebase.auth.currentUser != null) {
            setContentView(R.layout.activity_main)
            this.mIntentFilter = IntentFilter()
            mIntentFilter!!.addAction(getString(R.string.end_activities))

            registerReceiver(mReceiver, mIntentFilter)

            // Attempt to start the notification service at the start of the app lifecycle
            try {
                val serviceIntent = Intent(this, NotificationService::class.java)
                startService(serviceIntent)
            } catch (e: RuntimeException) {
                println("exception $e")
            }

            // Main toolbar
            val mToolbar = findViewById<Toolbar>(R.id.main_toolbar)
            setSupportActionBar(mToolbar)

            // Tab layout and pager
            val tabLayout = findViewById<TabLayout>(R.id.main_tab_layout)
            val viewPager = findViewById<ViewPager2>(R.id.news_pager)

            val tabTitles = resources.getStringArray(R.array.tabTitles)
            viewPager.adapter = MainTabAdapter(this)
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                when (position) {
                    0 -> tab.text = tabTitles[0]
                    1 -> tab.text = tabTitles[1]
                    2 -> tab.text = tabTitles[2]
                    3 -> tab.text = tabTitles[3]
                }
            }.attach()
        } else {
            intent = Intent(this, GuestActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun finish() {
        try {
            unregisterReceiver(mReceiver)
        } catch (e: RuntimeException) {

        }
        super.finish()
    }

    // Create and inflate toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate((R.menu.toolbar_layout), menu)
        return super.onCreateOptionsMenu(menu)
    }

    // Main toolbar buttons
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Button that refreshes the main activity and updates the headlines
            R.id.refresh -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
            // Button that displays friend requests
            R.id.notifications -> {
                val intent = Intent(this, NotificationsActivity::class.java)
                startActivity(intent)
                return true
            }
            // Button that logs the user off and sends him to the authentication activity
            R.id.action_logout -> {
                intent = Intent(this, AuthActivity::class.java)
                Firebase.auth.signOut()
                startActivity(intent)
                sendNotifBroadcast()

                finish()
                return true
            }
            // Button that sends the user to the activity where he can write his own article
            R.id.write_article -> {
                val intent = Intent(this, WriteArticleActivity::class.java)
                startActivity(intent)
                return true
            }
            // Button that allows the user to choose for what chosen categories
            // he wants to receive notifications
            R.id.change_notifications -> {
                val intent = Intent(this, ChipsNotificationsActivity::class.java)
                startActivity(intent)
                return true
            }
            // Button that allows the user to change news preferences
            R.id.change_preferences -> {
                val intent = Intent(this, ChipsCategoriesActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * When the user attempts to log off, send a broadcast that will end the main activity,
     * as well as the notification service
     */
    private fun sendNotifBroadcast() {
        val broadcastIntent = Intent()
        broadcastIntent.action = getString(R.string.end_activities)
        this.sendBroadcast(broadcastIntent)
    }
}
