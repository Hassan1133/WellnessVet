package com.wellness.vet.app.fragments.user

import android.media.audiofx.Visualizer.OnDataCaptureListener
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.wellness.vet.app.R
import com.wellness.vet.app.adapters.UserChatListAdapter
import com.wellness.vet.app.models.DoctorDetailProfileModel
import com.wellness.vet.app.models.UserChatListModel
import com.wellness.vet.app.activities.user.FindDoctorFromListActivity
import com.wellness.vet.app.databinding.FragmentUserChatBinding

class UserChatFragment : Fragment() , OnClickListener{

    private lateinit var binding: FragmentUserChatBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserChatBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        init()
        return binding.root
    }

    private fun init()
    {
        binding.addChatBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id)
        {
            R.id.addChatBtn -> goToFindDoctorFromListActivity()
        }
    }

    private fun goToFindDoctorFromListActivity() {
        val intent = Intent(requireActivity(), FindDoctorFromListActivity::class.java)
        intent.putExtra("flag", "chat")
        startActivity(intent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dataBase = Firebase.database
        val chatDbRef = dataBase.getReference("chats")
        val profileDbRef = dataBase.getReference("Doctors")
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val chatList = ArrayList<UserChatListModel>()
        val chatListAdapter = UserChatListAdapter(requireContext(),chatList)
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
                                    val doctorProfile = UserChatListModel(
                                        snapshot.child("id").value.toString(),
                                        snapshot.child("name").value.toString(),
                                        "${snapshot.child("startTime").value.toString()} - ${snapshot.child("endTime").value.toString()}",
                                        "Rs ${snapshot.child("fees").value.toString()}/-"
                                    )
                                    chatList.add(doctorProfile)
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