package com.example.chitchat.friendList

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chitchat.adapter.InviteAdapter
import com.example.chitchat.databinding.FragmentInviteBinding
import com.example.chitchat.models.Invite
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class InviteFragment : Fragment(), InviteAdapter.OnActionClick {

    private var isFragmentAttached: Boolean = false
    private lateinit var binding: FragmentInviteBinding
    private var list: ArrayList<Invite> = ArrayList()
    lateinit var adapter: InviteAdapter
    private lateinit var firebaseDatabase: FirebaseDatabase

    // define a constant for the SharedPreferences key
    private val DELETEDREQUESTSKEY = "deleted_requests"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentInviteBinding.inflate(
            inflater, container, false
        )

        firebaseDatabase = FirebaseDatabase.getInstance()

        binding.invite1.setOnClickListener {
            sendInvite()
        }

        getInvites()

        return binding.root
    }

    // fun to add request to the list of deleted request
    // in SharedPreferences
    private fun addDeletedRequest(requestId: String) {
        val sharedPreferences = requireActivity()
            .getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Get the current set of deleted requests from SharedPreferences
        val deletedRequests = getDeletedRequests()

        // Add the new request to the set
        deletedRequests.add(requestId)

        // Save the updated set back to SharedPreferences
        editor.putStringSet(DELETEDREQUESTSKEY, deletedRequests)
        editor.apply()
    }

    // fun to get the set of deleted requests from SharedPreferences
    private fun getDeletedRequests(): MutableSet<String> {
        val sharedPreferences = requireActivity()
            .getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getStringSet(
            DELETEDREQUESTSKEY,
            mutableSetOf()
        ) ?: mutableSetOf()
    }

    // fun to check if request is deleted
    private fun isRequestDeleted(requestId: String): Boolean {
        val deletedRequest = getDeletedRequests()
        return deletedRequest.contains(requestId)
    }

    // fun to remove a deleted request from SharedPreferences
    private fun removeDeletedRequest(requestId: String) {

        val sharedPreferences = requireActivity()
            .getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Get the current set of deleted requests from SharedPreferences
        val deletedRequests = getDeletedRequests()

        // Remove the request from the set
        deletedRequests.remove(requestId)

        // Save the updated set back to SharedPreferences
        editor.putStringSet(DELETEDREQUESTSKEY, deletedRequests)
        editor.apply()
    }

    // fun Encode the email address to make it a valid Firebase
    // Database key
    private fun encodeEmail(mail: String): String {
        return Base64.encodeToString(
            mail.toByteArray(),
            Base64.NO_WRAP
        )
    }

    private fun sendInvite() {
        val mail = binding.mail.text.toString().trim()

        if (isFragmentAttached) {
            val context = requireContext()

            if (mail.isEmpty() || mail == FirebaseAuth.getInstance().currentUser?.email) {
                Toast.makeText(context, "Invalid email address", Toast.LENGTH_SHORT).show()
                return
            }

            // Retrieve the sender's information
            val currentUser = FirebaseAuth.getInstance().currentUser
            val senderId = currentUser?.uid
            val senderEmail = currentUser?.email
            val senderName = currentUser?.displayName
            val senderProfilePic = currentUser?.photoUrl.toString()

            if (senderId == null || senderEmail == null || senderName == null) {
                Toast.makeText(
                    context, "Sender information not available",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            // Encode the email addresses to make them valid Firebase Database keys
            val encodedMail = encodeEmail(mail)
            val encodedSenderEmail = encodeEmail(senderEmail)

            // Check if the email exists in Firebase Authentication
            FirebaseAuth.getInstance().fetchSignInMethodsForEmail(mail)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val signInMethods = task.result?.signInMethods

                        if (signInMethods.isNullOrEmpty()) {
                            // The email does not exist in Firebase Authentication
                            Toast.makeText(
                                context,
                                "Email does not exist. Please enter a valid email address.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // Check if the recipient is already in the friend list
                            firebaseDatabase.reference
                                .child("friends")
                                .child(encodedSenderEmail)
                                .child("friendList")
                                .get()
                                .addOnCompleteListener { friendListData ->
                                    if (friendListData.isSuccessful) {
                                        val friendList = friendListData.result
                                        if (friendList != null && friendList.exists()) {
                                            if (friendList.hasChild(encodedMail)) {
                                                // The recipient is already in the friend list
                                                Toast.makeText(
                                                    context,
                                                    "You are already friends with this person.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                // The recipient is not in the friend list, proceed with sending the request
                                                Log.d("send67", "sent Invite: $mail")
                                                val invite = Invite(
                                                    senderName = senderName,
                                                    receiver = mail,
                                                    senderId = senderId,
                                                    sender = senderEmail,
                                                    senderProfilePic = senderProfilePic
                                                )
                                                // Send the request with the encoded email as the key
                                                firebaseDatabase.reference
                                                    .child("Requests")
                                                    .child(encodedMail)
                                                    .child("invites")
                                                    .child(senderId) // Use the encoded email as the key
                                                    .setValue(invite)
                                                    .addOnSuccessListener {
                                                        Toast.makeText(
                                                            context,
                                                            "Request Sent",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Log.e(
                                                            "send67",
                                                            "Failed to send request: $e"
                                                        )
                                                        Toast.makeText(
                                                            context,
                                                            "Failed to send request",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                            }
                                        } else {
                                            // The recipient is not in the friend list, proceed with sending the request
                                            Log.d("send67", "sent Invite: $mail")
                                            val invite = Invite(
                                                senderName = senderName,
                                                receiver = mail,
                                                senderId = senderId,
                                                sender = senderEmail,
                                                senderProfilePic = senderProfilePic
                                            )
                                            // Send the request with the encoded email as the key
                                            firebaseDatabase.reference
                                                .child("Requests")
                                                .child(encodedMail)
                                                .child("invites")
                                                .child(senderId) // Use the encoded email as the key
                                                .setValue(invite)
                                                .addOnSuccessListener {
                                                    Toast.makeText(
                                                        context,
                                                        "Request Sent",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e("send67", "Failed to send request: $e")
                                                    Toast.makeText(
                                                        context,
                                                        "Failed to send request",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Error in getting friend list data",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }.addOnFailureListener {
                                    Log.e("send67", "Error checking friend list: $it")
                                    Toast.makeText(
                                        context,
                                        "Error checking friend list",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    } else {
                        Log.e("send67", "Error checking email existence: ${task.exception}")
                        Toast.makeText(
                            context,
                            "Error checking email existence",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
        }
    }

    private fun getInvites() {

        val receiver = FirebaseAuth.getInstance()
            .currentUser?.email.toString()

        val receiverName = FirebaseAuth.getInstance()
            .currentUser?.displayName.toString()

        val receiverId = FirebaseAuth.getInstance()
            .currentUser?.uid.toString()

        val receiverProfilePic = FirebaseAuth.getInstance()
            .currentUser?.photoUrl.toString()

        //Encode the email address to make it a valid Firebase
        // Database key
        val encodedMail = encodeEmail(receiver)

        if (isFragmentAttached) {
            if (isRequestDeleted(DELETEDREQUESTSKEY)) {
                removeDeletedRequest(DELETEDREQUESTSKEY)
            } else {
                firebaseDatabase.reference
                    .child("Requests")
                    .child(encodedMail)
                    .child("invites")
                    .get()
                    .addOnCompleteListener { data ->
                        if (data.isSuccessful) {
                            val result = data.result
                            if (result != null && result.exists()) {
                                for (item in result.children) {
                                    val senderName = item.child("senderName")
                                        .getValue(String::class.java).toString()
                                    val senderEmail = item.child("sender")
                                        .getValue(String::class.java).toString()
                                    val senderId = item.child("senderId")
                                        .getValue(String::class.java).toString()
                                    val invite = Invite(
                                        senderName = senderName,
                                        sender = senderEmail,
                                        receiver = receiver,
                                        receiverName = receiverName,
                                        senderId = senderId,
                                        receiverId = receiverId,
                                        receiverProfilePic = receiverProfilePic
                                    )
                                    list.add(invite)
                                }
                                Log.d("invite89", "getInvite: $list")
                                showInviteInAdapter(list)
                            }
                        }
                    }.addOnFailureListener {
                        Log.d(
                            "invite89", "error in getting " +
                                    "invite request data: $list"
                        )
                    }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showInviteInAdapter(list: ArrayList<Invite>) {
        if (isFragmentAttached) {
            val context = requireContext()
            binding.recyclerView.layoutManager =
                LinearLayoutManager(context)
            adapter = InviteAdapter(list, this)
            binding.recyclerView.adapter = adapter
        }
    }

    override fun onAcceptClick(
        senderName: String,
        sender: String,
        receiver: String,
        receiverName: String,
        position: Long,
        senderId: String,
        receiverId: String
    ) {

        if (isFragmentAttached) {
            val context = requireContext()
            Toast.makeText(
                context,
                "accept",
                Toast.LENGTH_SHORT
            ).show()

            addRequest(sender, senderName, receiver, receiverName, senderId, receiverId)
            deleteRequest(position, receiver, senderId)
        }
    }

    override fun onDenyClick(
        senderName: String,
        sender: String,
        receiver: String,
        position: Long,
        senderId: String
    ) {
        if (isFragmentAttached) {
            val context = requireContext()
            Toast.makeText(
                context,
                "Deny",
                Toast.LENGTH_SHORT
            ).show()

            deleteRequest(position, receiver, senderId)
        }
    }

    private fun addRequest(
        sender: String, senderName: String,
        receiver: String, receiverName: String,
        sId: String, rId: String
    ) {
        addDeletedRequest(receiver)

        if (isFragmentAttached) {
            val context = requireContext()

            //Encode the email address to make it a valid Firebase
            // Database key
            val encodeSenderEmail = encodeEmail(sender)
            val encodeReceiverEmail = encodeEmail(receiver)

            // sender side
            val senderRequest = Invite(
                receiver = receiver,
                sender = sender,
                receiverName = receiverName,
                senderName = senderName,
                senderId = sId,
                receiverId = rId
            )

            firebaseDatabase.reference
                .child("friends")
                .child(encodeSenderEmail)
                .child("friendList")
                .child(encodeReceiverEmail)
                .setValue(senderRequest)
                .addOnCompleteListener {
                    // receiver side
                    firebaseDatabase.reference
                        .child("friends")
                        .child(encodeReceiverEmail)
                        .child("friendList")
                        .child(encodeSenderEmail)
                        .setValue(senderRequest)
                        .addOnCompleteListener {
                            Toast.makeText(
                                context,
                                "added successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }.addOnFailureListener {
                            Log.d(
                                "send67",
                                "Failed to add receiver Request: $it"
                            )
                        }

                }.addOnFailureListener {
                    Log.d(
                        "send67",
                        "Failed to sender Request: $it"
                    )
                }

            // share preferences
            val sp = requireActivity().getSharedPreferences(
                "MyFriends",
                Context.MODE_PRIVATE
            )
            val editor = sp?.edit()
            if (editor != null) {
                editor.putString("sName", senderName)
                editor.putString("rName", receiverName)
                editor.putString("rEmail", receiver)
                editor.putString("sEmail", sender)
                editor.apply()
            }
        }
    }

    private fun deleteRequest( position: Long,
        receiver: String,
        senderId: String
    ) {
        addDeletedRequest(receiver)

        if (isFragmentAttached) {
            val context = requireContext()
            //Encode the email address to make it a valid Firebase
            // Database key
            val encodeReceiverEmail = encodeEmail(receiver)

            val databaseReference = firebaseDatabase.reference
                .child("Requests")
                .child(encodeReceiverEmail)
                .child("invites")
                .child(senderId)

            databaseReference.removeValue()
                .addOnSuccessListener {
                    list.removeAt(position.toInt())
                    adapter.notifyItemRemoved(position.toInt())
                    Toast.makeText(
                        context,
                        "deleted Permanently",
                        Toast.LENGTH_SHORT
                    ).show()

                }.addOnFailureListener {
                    Log.d("delete23", "deleted $it")
                }
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