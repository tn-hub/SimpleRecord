package com.tnp.simplerecord.navigation.util

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.tnp.simplerecord.navigation.model.PushDTO
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

class FcmPush {
    var JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
    var url = "https://fcm.googleapis.com/fcm/send"
    var serverKey = "AAAAbQ1CWT4:APA91bFBEmfvFNfJc6tRc39c9fSjkaDSCvcPS6-mMDFGScACD-Uz_VdLY5A6b9i3E8SPRiK4KFLZ7UneCU8idSkLqcT2LrPSsNefL6NwRYkchLvnqNx40cIG2RmNJ4O3yqMNXYy2RUnG"
    var gson : Gson? = null
    var okHttpClient : OkHttpClient? = null
    companion object{
        var instance = FcmPush()
    }

    init {
        gson = Gson()
        okHttpClient = OkHttpClient()
    }

    fun sendMessage(destinationUid: String, title : String, message : String){
        FirebaseFirestore.getInstance().collection("pushtokens").document(destinationUid).get().addOnCompleteListener {
            task ->
            if(task.isSuccessful){
                var token = task?.result?.get("pushToken").toString()

                var pushDTO = PushDTO()
                pushDTO.to = token
                pushDTO.notification.title = title
                pushDTO.notification.body = message

                var body = RequestBody.create(JSON, gson?.toJson(pushDTO)!!)
                var request = Request.Builder()
                    .addHeader("Content-Type","application/json")
                    .addHeader("Authorization", "key="+serverKey)
                    .url(url)
                    .post(body)
                    .build()

                okHttpClient?.newCall(request)?.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("sendMessage() ","onFailure")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        Log.d("sendMessage() ","onResponse")
                        println(response.body?.string())
                    }
                })
            }
        }
    }
}