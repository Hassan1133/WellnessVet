package com.wellness.vet.app.fragments.user

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.user.FindDoctorFromListActivity
import com.wellness.vet.app.adapters.UserChatListAdapter
import com.wellness.vet.app.databinding.FragmentUserChatBinding
import com.wellness.vet.app.main_utils.LoadingDialog
import com.wellness.vet.app.models.UserChatListModel

class UserChatFragment : Fragment() , OnClickListener{

    private lateinit var binding: FragmentUserChatBinding
    private lateinit var loadingDialog: Dialog

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


    }

    override fun onResume() {
        super.onResume()
        val dataBase = Firebase.database
        val chatDbRef = dataBase.getReference("chats")
        val profileDbRef = dataBase.getReference("Doctors")
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val chatList = ArrayList<UserChatListModel>()
        val chatListAdapter = UserChatListAdapter(requireContext(), chatList, "user")
        binding.recyclerView.adapter = chatListAdapter

        if (currentUser != null) {
            val userUid = currentUser.uid

            loadingDialog = LoadingDialog.showLoadingDialog(requireActivity())

            chatDbRef.child(userUid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tempList = mutableListOf<UserChatListModel>()
                    if (snapshot.exists()) {
                        var count = 0
                        for (ds in snapshot.children) {
                            Log.d("TAGTest", "onDataChange: ${ds.key}")
                            profileDbRef.child(ds.key.toString()).child("Profile")
                                .addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val userProfile = UserChatListModel(
                                            snapshot.child("id").value.toString(),
                                            snapshot.child("name").value.toString(),
                                            snapshot.child("city").value.toString(),
                                            snapshot.child("imgUrl").value.toString(),
                                            snapshot.child("chatStatus").value.toString()
                                        )
                                        if (!tempList.any { it.uid == userProfile.uid }) {
                                            tempList.add(userProfile)
                                        }
                                        count++
                                        if (count == snapshot.childrenCount.toInt()) {
                                            chatList.clear()
                                            chatList.addAll(tempList)
                                            chatListAdapter.updateList(chatList)
                                            LoadingDialog.hideLoadingDialog(loadingDialog)
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        LoadingDialog.hideLoadingDialog(loadingDialog)
                                    }

                                })
                        }
                    } else {
                        LoadingDialog.hideLoadingDialog(loadingDialog)
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    LoadingDialog.hideLoadingDialog(loadingDialog)
                }

            })

        }
    }
}