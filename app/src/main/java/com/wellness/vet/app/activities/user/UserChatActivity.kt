package com.wellness.vet.app.activities.user

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ServerValue
import com.google.firebase.database.database
import com.google.firebase.storage.storage
import com.wellness.vet.app.R
import com.wellness.vet.app.adapters.ChatListAdapter
import com.wellness.vet.app.databinding.ActivityUserChatBinding
import com.wellness.vet.app.models.ChatDataModel

class UserChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserChatBinding
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

        if (currentUser != null) {
            val doctorUid = "yiGsWIhGIsMXz7Onilymj4LAR2m2"
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
                                    msgMap["type"] = "image"
                                    msgMap["time"] = ServerValue.TIMESTAMP
                                    msgMap["from"] = userUid

                                    senderDbRef.child("$pushId").setValue(msgMap)
                                        .addOnCompleteListener(OnCompleteListener { send ->
                                            if (send.isSuccessful) {
                                                receiverDbRef.child("$pushId").setValue(msgMap)
                                                    .addOnCompleteListener(OnCompleteListener { receive ->
                                                        if (receive.isSuccessful) {
                                                            Toast.makeText(
                                                                this@UserChatActivity,
                                                                "Success",
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
                    binding.message.error = "enter some text"
                    return@setOnClickListener
                }
                sendMessage(textMessage, "text", userUid, senderDbRef, receiverDbRef)
            }
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
                                Toast.makeText(this@UserChatActivity, "Success", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        })
                }
            })
    }
}