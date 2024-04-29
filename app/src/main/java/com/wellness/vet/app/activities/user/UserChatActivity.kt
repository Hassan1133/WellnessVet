package com.wellness.vet.app.activities.user

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.database
import com.google.firebase.storage.storage
import com.wellness.vet.app.R
import com.wellness.vet.app.adapters.ChatListAdapter
import com.wellness.vet.app.databinding.ActivityUserChatBinding
import com.wellness.vet.app.main_utils.AppConstants
import com.wellness.vet.app.main_utils.AppSharedPreferences
import com.wellness.vet.app.models.ChatDataModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class UserChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserChatBinding
    private lateinit var doctorUid: String
    private lateinit var userRef: DatabaseReference
    private lateinit var doctorRef: DatabaseReference
    private lateinit var appSharedPreferences: AppSharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val dataBase = Firebase.database
        val storage = Firebase.storage
        val chatDbRef = dataBase.getReference("chats")
        val chatStorageRef = storage.getReference("chats")
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        userRef = FirebaseDatabase.getInstance().getReference(AppConstants.USER_REF)
        doctorRef = FirebaseDatabase.getInstance().getReference(AppConstants.DOCTOR_REF)
        appSharedPreferences = AppSharedPreferences(this@UserChatActivity)

        if (currentUser != null) {
            doctorUid = intent.getStringExtra("uid")!!
            binding.docName.text = intent.getStringExtra("name")!!
            Glide.with(this@UserChatActivity).load(intent.getStringExtra("imgUrl")!!)
                .diskCacheStrategy(
                    DiskCacheStrategy.DATA
                ).into(binding.docImage)
            val userUid = currentUser.uid

            val senderDbRef = chatDbRef.child(userUid).child(doctorUid).child("messages")
            val receiverDbRef = chatDbRef.child(doctorUid).child(userUid).child("messages")
            val storageDbRef = chatStorageRef.child(userUid).child(doctorUid).child("messages")

            val list: ArrayList<ChatDataModel> = ArrayList()
            val adapter = ChatListAdapter(this@UserChatActivity, list, userUid)
            binding.recyclerChat.adapter = adapter

            senderDbRef.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val model = ChatDataModel(
                        snapshot.child("from").value.toString(),
                        snapshot.child("message").value.toString(),
                        snapshot.child("time").value.toString(),
                        snapshot.child("type").value.toString()
                    )
                    list.add(model)
                    adapter.updateList(list)
                    binding.recyclerChat.scrollToPosition(list.size - 1)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    TODO("Not yet implemented")
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }
            })


            val pickMedia =
                registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                    if (uri != null) {
                        val mimeType: String? = contentResolver.getType(uri)
                        var dataType = "image"
                        if(mimeType!!.contains("video")){
                            dataType = "video"
                        }
                        Toast.makeText(this@UserChatActivity,"$mimeType",Toast.LENGTH_SHORT).show()
                        val pushIdRef = senderDbRef.push()
                        val pushId = pushIdRef.key
                        val filePath = storageDbRef.child("messageImages").child("$pushId.jpg")
                        filePath.putFile(uri).addOnProgressListener {
                            binding.relativeProgress.visibility = View.VISIBLE
                            val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
                            binding.progressIndicator.progress = progress.toInt()
                        }.addOnCompleteListener(OnCompleteListener {
                            binding.relativeProgress.visibility = View.GONE
                            if (it.isSuccessful) {
                                filePath.downloadUrl.addOnSuccessListener { downloadUri ->
                                    val msgMap = HashMap<String, Any>()
                                    msgMap["message"] = "$downloadUri"
                                    msgMap["type"] = dataType
                                    msgMap["time"] = ServerValue.TIMESTAMP
                                    msgMap["from"] = userUid

                                    senderDbRef.child("$pushId").setValue(msgMap)
                                        .addOnCompleteListener(OnCompleteListener { send ->
                                            if (send.isSuccessful) {
                                                receiverDbRef.child("$pushId").setValue(msgMap)
                                                    .addOnCompleteListener(OnCompleteListener { receive ->
                                                        if (receive.isSuccessful) {
                                                            updateChatReadStatus(
                                                                appSharedPreferences.getString("userUid")!!
                                                            )
                                                            Toast.makeText(
                                                                this@UserChatActivity,
                                                                getString(R.string.message_sent),
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    })
                                            }
                                        })

//                                    sendMessage("$downloadUri","image",userUid,senderDbRef,receiverDbRef)
                                }
                            }
                        })
                    } else {
                        Log.d("PhotoPicker", "No media selected")
                    }
                }

            binding.btnAddFile.setOnClickListener {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
            }

            binding.btnSend.setOnClickListener {
                val textMessage = binding.message.text.toString()
                if (textMessage.isEmpty()) {
                    binding.message.error = getString(R.string.enter_text)
                    return@setOnClickListener
                }
                sendMessage(textMessage, "text", userUid, senderDbRef, receiverDbRef)
                binding.message.setText("")
            }
        }
    }


    override fun onResume() {
        super.onResume()
        updateDoctorChatStatus()
    }
    private fun updateDoctorChatStatus()
    {
        val map = HashMap<String, Any>()
        map["chatStatus"] = "seen"

        doctorRef.child(doctorUid).child(AppConstants.PROFILE_REF).updateChildren(map).addOnSuccessListener {
            Toast.makeText(
                this@UserChatActivity, getString(R.string.messages_seen), Toast.LENGTH_SHORT
            ).show()
        }.addOnFailureListener{
            Toast.makeText(this@UserChatActivity, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendMessage(
        msg: String,
        type: String,
        uid: String,
        senderDbRef: DatabaseReference,
        receiverDbRef: DatabaseReference
    ) {
        val msgMap = HashMap<String, Any>()
        msgMap["message"] = msg
        msgMap["type"] = type
        msgMap["time"] = ServerValue.TIMESTAMP
        msgMap["from"] = uid

        val pushIdRef = senderDbRef.push()
        val pushId = pushIdRef.key

        senderDbRef.child("$pushId").setValue(msgMap)
            .addOnCompleteListener(OnCompleteListener { send ->
                if (send.isSuccessful) {
                    receiverDbRef.child("$pushId").setValue(msgMap)
                        .addOnCompleteListener(OnCompleteListener { receive ->
                            if (receive.isSuccessful) {
                                updateChatReadStatus(appSharedPreferences.getString("userUid")!!)
                                Toast.makeText(
                                    this@UserChatActivity,
                                    getString(R.string.message_sent),
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        })
                }
            })
    }

    private fun updateChatReadStatus(userId: String) {

        val map = HashMap<String, Any>()
        map["chatStatus"] = "unseen"

        userRef.child(userId).child(AppConstants.PROFILE_REF).updateChildren(map)
            .addOnSuccessListener {
                getDoctorFCMToken(doctorUid)
            }.addOnFailureListener {
                Toast.makeText(this@UserChatActivity, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun getDoctorFCMToken(doctorId: String) {
        doctorRef.child(doctorId).child(AppConstants.PROFILE_REF).child("fcmToken").get()
            .addOnCompleteListener { task -> sendNotification(task.result.value.toString()) }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@UserChatActivity,
                    e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun sendNotification(token: String) {
        try {
            val jsonObject = JSONObject().apply {
                val dataObj = JSONObject().apply {
                    put("title", appSharedPreferences.getString("userName"))
                    put("body", "sent you a message.")
                    put("userType", "user")
                    put("uid", appSharedPreferences.getString("userUid"))
                    put("name", appSharedPreferences.getString("userName"))
                    put("imgUrl", appSharedPreferences.getString("userImgUrl"))
                }
                put("data", dataObj)
                put("to", token)
            }
            callApi(jsonObject)
        } catch (e: Exception) {
            // Handle exception
        }
    }

    fun callApi(jsonObject: JSONObject) {
        val json: MediaType = "application/json; charset=utf-8".toMediaType()
        val client = OkHttpClient()
        val url = "https://fcm.googleapis.com/fcm/send"
        val body: RequestBody = jsonObject.toString().toRequestBody(json)
        val request: Request = Request.Builder().url(url).post(body).header(
            "Authorization",
            "Bearer AAAACUPVG6c:APA91bGNRoGWs-hbWTuUYz96UqpZNI2appZUTIIpf6L1of3PMNAcfCpHRaS-HkwohhXAOxAl1uzB0FXJVgURP1aOi9EnU1W5hLAv9gC0bbMsGlAiwL41z1SbqyWgQln8eE0GLEhTTD4z"
        ).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
                this@UserChatActivity.runOnUiThread {
                    Toast.makeText(this@UserChatActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {

            }
        })
    }
}