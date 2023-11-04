package com.example.chitchat.friendList

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chitchat.adapter.FriendListAdapter
import com.example.chitchat.databinding.FragmentFriendListBinding
import com.example.chitchat.models.FriendList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.util.Base64
import android.util.Log

class FriendListFragment : Fragment() {

    private lateinit var binding: FragmentFriendListBinding
    lateinit var adapter: FriendListAdapter
    private lateinit var firebaseDatabase: FirebaseDatabase
    val friendList: ArrayList<FriendList> = ArrayList()

    private var isFragmentAttached: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFriendListBinding.inflate(
            inflater, container, false
        )

        firebaseDatabase = FirebaseDatabase.getInstance()

        fetchFriendList()

        return binding.root
    }

    private fun encodeMail(mail: String): String {
        return Base64.encodeToString(
            mail.toByteArray(),
            Base64.NO_WRAP
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchFriendList() {
        val currentUserEmail = FirebaseAuth.getInstance()
            .currentUser?.email.toString()

        val encodedEmail = encodeMail(currentUserEmail)

        if (isFragmentAttached) {

            firebaseDatabase.reference
                .child("friends")
                .child(encodedEmail)
                .child("friendList")
                .get()
                .addOnCompleteListener { data ->
                    if (data.isSuccessful) {
                        val result = data.result
                        if (result != null && result.exists()) {
                            for (item in result.children) {
                                val senderName = item.child("senderName")
                                    .getValue(String::class.java).toString()
                                val sender = item.child("sender")
                                    .getValue(String::class.java).toString()
                                val receiverName = item.child("receiverName")
                                    .getValue(String::class.java).toString()
                                val receiver = item.child("receiver")
                                    .getValue(String::class.java).toString()
                                val senderId = item.child("senderId")
                                    .getValue(String::class.java).toString()
                                val receiverId = item.child("receiverId")
                                    .getValue(String::class.java).toString()
                                val senderProfilePic = item
                                    .child("senderProfilePic")
                                    .getValue(String::class.java).toString()
                                val receiverProfilePic = item
                                    .child("receiverProfilePic")
                                    .getValue(String::class.java).toString()

                                val addToFriendList = FriendList(
                                    senderName = senderName,
                                    sender = sender,
                                    receiverName = receiverName,
                                    receiver = receiver,
                                    senderId = senderId,
                                    receiverId = receiverId,
                                    senderProfilePic = senderProfilePic,
                                    receiverProfilePic = receiverProfilePic

                                )
                                friendList.add(addToFriendList)
                            }
                            showInAdapter(friendList)
                            adapter.notifyDataSetChanged()
                        }
                    }

                }.addOnFailureListener {
                    Log.d("friendList56", "getInvite: $it")
                }
        }
    }

    private fun showInAdapter(list: ArrayList<FriendList>) {
        if (isFragmentAttached) {
            val context = requireContext()
            binding.recyclerView5.layoutManager =
                LinearLayoutManager(context)
            adapter = FriendListAdapter(
                list
            )
            binding.recyclerView5.adapter = adapter
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        isFragmentAttached = true
    }

    override fun onDetach() {
        super.onDetach()
        isFragmentAttached = false
    }
}

