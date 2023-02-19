package com.example.newsapp.managers

import android.content.ContentValues
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapp.data.ApiData
import com.example.newsapp.data.Headline
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Data request manager that accesses Firebase database and makes a news api call
 * The class has the current user id as the default configuration
 */
class NewsDataRequestManager(val userId: String = Firebase.auth.currentUser!!.uid,
                             private val db: FirebaseFirestore = Firebase.firestore) : AppCompatActivity() {
    private var apiData: ApiData? = null

    /**
     * Method that launches a coroutine for news data request.
     * @param forCategory indicated whether categories should be taken into account
     * @param forLanguage indicated whether languages should be taken into account
     * @param forCountry indicated whether countries should be taken into account
     * @param keywords stores the keywords that are used when an api call is made
     */
    fun requestData(forCountry: Boolean = true,
                    forLanguage: Boolean = true,
                    forCategory: Boolean = true,
                    keywords: MutableList<String> = mutableListOf()): MutableList<Headline> {
        GlobalScope.launch {
            val tags = async {getTags(forCountry, forLanguage, forCategory, keywords)}
            tags.join()
            val response = launch {requestNewsData(tags.await())}
            response.join()
        }

        while (apiData == null) {
            Thread.sleep(10)
            Log.w(ContentValues.TAG, "Waiting for API call")
        }
        return apiData!!.results
    }

    /**
     * Get the information from the database and make a news api call,
     * taking the parameters into account
     */
    private suspend fun getTags(forCountry: Boolean,
                                forLanguage: Boolean,
                                forCategory: Boolean,
                                keywords: MutableList<String>): String {
        var tags = ""
        val database = db.collection("user").document(userId).get()
        database.await()

        var countries = ""
        var languages = ""
        var categories = ""
        var keywordss = ""

        if (forCountry) {
            var countryData = mutableListOf<String>()
            if (database.result.data!!["countries"] != null) {
                countryData = database.result.data!!["countries"] as MutableList<String>
            }
            // Process the result so it can be user to call the api
            if (countryData.isNotEmpty()) {
                countries = "&country=" + countryData.joinToString(separator = ",")
            }
        }

        if (forLanguage) {
            var languageData = mutableListOf<String>()
            if (database.result.data!!["languages"] != null) {
                languageData = database.result.data!!["languages"] as MutableList<String>
            }
            // Process the result so it can be user to call the api
            if (languageData.isNotEmpty()) {
                languages = "&language=" + languageData.joinToString(separator = ",")
            }
        }

        if (forCategory) {
            var categoryData = mutableListOf<String>()
            if (database.result.data!!["categories"] != null) {
                categoryData = database.result.data!!["categories"] as MutableList<String>
            }
            // Process the result so it can be user to call the api
            if (categoryData.isNotEmpty()) {
                categories = "&category=" + categoryData.joinToString(separator = ",")
            }
        }

        // Process the result so it can be user to call the api
        if (keywords.isNotEmpty()) {
            keywordss = "&q=" + keywords.joinToString(separator = "%20AND%20")
        }

        tags += countries + languages + categories + keywordss
        Log.w(ContentValues.TAG, "Database reading: $tags")

        return tags
    }

    /**
     * Method that makes the api call to search for news articles
     */
    private fun requestNewsData(tags: String) {
        val url = URL("https://newsdata.io/api/1/news?apikey=pub_14178cd08bf1a5c4fcf7d03e3c2f3be0f78fc$tags")
        val connection = url.openConnection() as HttpsURLConnection

        // On success, get the article data and assign the variables to the ApiData data class
        if (connection.responseCode == 200) {
            val inputStream = connection.inputStream
            val inputStreamReader = InputStreamReader(inputStream, "UTF-8")
            apiData = Gson().fromJson(inputStreamReader, ApiData::class.java)
            inputStreamReader.close()
            inputStream.close()
        } else {
            println(connection.responseCode)
        }
    }
}
