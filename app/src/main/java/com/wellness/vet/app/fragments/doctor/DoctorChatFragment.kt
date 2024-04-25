package com.wellness.vet.app.fragments.doctor

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.wellness.vet.app.R
import com.wellness.vet.app.adapters.UserChatListAdapter
import com.wellness.vet.app.databinding.FragmentDoctorChatBinding
import com.wellness.vet.app.databinding.FragmentUserChatBinding
import com.wellness.vet.app.models.UserChatListModel

class DoctorChatFragment : Fragment() {

    private lateinit var binding: FragmentDoctorChatBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDoctorChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dataBase = Firebase.database
        val chatDbRef = dataBase.getReference("chats")
        val profileDbRef = dataBase.getReference("Users")
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val chatList = ArrayList<UserChatListModel>()
        val chatListAdapter = UserChatListAdapter(requireContext(),chatList,"doctor")
        binding.recyclerView.adapter = chatListAdapter

        if (currentUser != null) {
            val userUid = currentUser.uid

            chatDbRef.child(userUid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatList.clear()
                    for (ds in snapshot.children) {
                        Log.d("TAGTest", "onDataChange: ${ds.key}")
                        profileDbRef.child(ds.key.toString()).child("Profile")
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val userProfile = UserChatListModel(
                                        snapshot.child("id").value.toString(),
                                        snapshot.child("name").value.toString(),
                                        snapshot.child("city").value.toString(),
                                        snapshot.child("imgUrl").value.toString()
                                    )
                                    chatList.add(userProfile)
                                    chatListAdapter.updateList(chatList)
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                            })
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        }
    }
}