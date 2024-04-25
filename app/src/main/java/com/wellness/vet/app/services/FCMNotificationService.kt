package com.wellness.vet.app.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.common.LoginActivity
import com.wellness.vet.app.activities.doctor.DoctorChatActivity
import com.wellness.vet.app.activities.user.UserChatActivity

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class FCMNotificationService : FirebaseMessagingService() {

    // Notification channel ID and name
    private val channelId = "WellnessVetChatChannel"
    private val channelName = "WellnessVet Chats"

    // Called when a new FCM message is received
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Check if the user is authenticated
        if (FirebaseAuth.getInstance().currentUser != null) {
            // Extract title, body, and user type from the message data

            val title = message.getData()["title"]
            val body = message.getData()["body"]
            val userType = message.getData()["userType"]
            val uid = message.getData()["uid"]
            val name = message.getData()["name"]
            val imgUrl = message.getData()["imgUrl"]

            Log.d("TAGservice", "onMessageReceived: ${userType}")

            // Send notification if user is authenticated
            sendNotification(title!!, body!!, userType!!, uid!!, name!!, imgUrl!!)
        }
    }

    // Function to send notification
    private fun sendNotification(
        title: String,
        messageBody: String,
        userType: String,
        uid: String,
        name: String,
        imgUrl: String
    ) {
        // Create an intent based on the user type to open the appropriate activity
        val intent = Intent(this, getMainActivityClass(userType)).apply {
            putExtra("uid", uid)
            putExtra("name", name)
            putExtra("imgUrl", imgUrl)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        // Create a PendingIntent to open the activity when notification is clicked
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId).apply {
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setSmallIcon(R.drawable.chat)
            setContentTitle(title)
            setContentText(messageBody)
            setDefaults(Notification.DEFAULT_SOUND)
            setAutoCancel(true)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setContentIntent(pendingIntent)
        }

        // Get the system's NotificationManager service
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Show the notification
        notificationManager.notify(0, notificationBuilder.build())
    }

    // Function to get the appropriate MainActivity class based on user type
    private fun getMainActivityClass(userType: String): Class<*> {
        return when (userType) {
            "user" -> DoctorChatActivity::class.java
            "doctor" -> UserChatActivity::class.java
            else -> LoginActivity::class.java
        }
    }
}