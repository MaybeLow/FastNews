package com.example.newsapp.activities

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.items.MessageItem
import com.example.newsapp.recycleadapters.MessagerAdapter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Activity that allows the user to chat with other Fast News users
 */
class MessagerActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messager)
        // Get the chatter user id from the caller activity
        val companionId = intent.getStringExtra("userId")

        if (companionId != null) {
            getMessages(companionId)
            createEnterMessageFunction(companionId)
        }
    }

    /**
     * Get the messages in case the users had a chat previously
     */
    private fun getMessages(companionId: String): MutableList<MessageItem> {
        var skip = true
        val auth = Firebase.auth
        val finalMessages = mutableListOf<MessageItem>()
        val dbRef = Firebase.database("https://newsnetwork-cfe31-default-rtdb.europe-west1.firebasedatabase.app/").reference
        // Database calls
        val userMessageData = dbRef.child("chat").child(auth.currentUser!!.uid).child("chatters").child(companionId).child("messages")
        val companionMessageData = dbRef.child("chat").child(companionId).child("chatters").child(auth.currentUser!!.uid).child("messages")

        userMessageData.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // This method is called once with the initial value,
                // and again whenever data at this location is updated
                if (snapshot.value != 0) {
                    try {
                        val userMessages = snapshot.getValue<HashMap<String, MessageItem>>()
                        Log.d(ContentValues.TAG, "messages value is: $userMessages")

                        if (userMessages != null) {
                            for (userMessage in userMessages.values) {
                                finalMessages.add(userMessage)
                            }
                        }
                    } catch (e: DatabaseException) {
                        println("database exception $e")
                    }

                    companionMessageData.addValueEventListener(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            // This method is called once with the initial value,
                            // and again whenever data at this location is updated.
                            if (snapshot.value != null) {
                                try {
                                    val companionMessages = snapshot.getValue<HashMap<String, MessageItem>>()
                                    Log.d(ContentValues.TAG, "messages value is: $companionMessages")

                                    if (companionMessages != null) {
                                        for (companionMessage in companionMessages.values) {
                                            finalMessages.add(companionMessage)
                                        }
                                    }

                                    inflateMessager(finalMessages)
                                    // Skip the next inflateMessager method call, if it was called from here
                                    skip = false
                                } catch (e: DatabaseException) {
                                    println("database exception $e")
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                        }
                    })
                    if (skip) {
                        inflateMessager(finalMessages)
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }
        })
        return finalMessages
    }

    /**
     * Inflate the messager activity with messages
     */
    private fun inflateMessager(myMessages: MutableList<MessageItem>) {
        val recyclerView = findViewById<View>(R.id.messager_recycler) as RecyclerView // Bind to the recyclerview in the layout
        val layoutManager = LinearLayoutManager(this) // Get the layout manager
        recyclerView.layoutManager = layoutManager
        val mAdapter = MessagerAdapter(myMessages)
        recyclerView.adapter = mAdapter
    }

    /**
     * A function that allows the user to type a message that will be displayed in the chat
     */
    private fun createEnterMessageFunction(companionId: String) {
        val enterMessageButton = findViewById<Button>(R.id.send_message_button)
        enterMessageButton.setOnClickListener {
            val message = findViewById<TextView>(R.id.enter_message)
            val input = message.text.toString()
            Log.w(ContentValues.TAG, "entered message $input")

            sendMessage(input, companionId)
        }
    }

    /**
     * The function that sends the message to the database.
     * The function also updated the activity so the new message is displayed
     */
    private fun sendMessage(message: String, companionId: String) {
        val auth = Firebase.auth
        val userId = auth.currentUser!!.uid
        val dbRef = Firebase.database("https://newsnetwork-cfe31-default-rtdb.europe-west1.firebasedatabase.app/").reference

        // Get the current time to create an id for the message
        val formatter = DateTimeFormatter.ofPattern("dd-M-yyyy hh:mm:ss", Locale.GERMANY)
        val currentDate = LocalDateTime.now().format(formatter)

        // Store the message to the current user chat database
        val userMessageDatabase = dbRef.child("chat").child(userId).child("chatters").child(companionId).child("messages").child(currentDate)
        userMessageDatabase.setValue(MessageItem(message, currentDate, userId))

        // Store the last sent message that is displayed on the chat activity
        val userLastMessageDatabase = dbRef.child("chat").child(userId).child("chatters").child(companionId).child("last_message")
        userLastMessageDatabase.setValue(message)

        // Store a temp value to the chatter database. This is needed to display the current user's messages in case the chatter has not sent a message before
        val companionMessageDatabase = dbRef.child("chat").child(companionId).child("chatters").child(userId).child("temp")
        companionMessageDatabase.setValue("temp value")

        // Store the last message to the chatter's database for the same reason
        val companionLastMessageDatabase = dbRef.child("chat").child(companionId).child("chatters").child(userId).child("last_message")
        companionLastMessageDatabase.setValue(message)
        // Add current user to the companion's database if one does not exist yet

        // Update the activity to apply all the changes
        getMessages(companionId)
    }
}
