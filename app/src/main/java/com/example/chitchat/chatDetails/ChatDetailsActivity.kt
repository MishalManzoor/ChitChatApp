package com.example.chitchat.chatDetails

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chitchat.MainActivity
import com.example.chitchat.R
import com.example.chitchat.adapter.ChatAdapter
import com.example.chitchat.databinding.ActivityChatBinding
import com.example.chitchat.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Date

class ChatDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding

    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    private lateinit var list: List<Message>
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        mAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        val i = intent?.extras
        val receiverName = i?.getString("rName") ?: ""
        val senderName = i?.getString("sName") ?: ""
        val sId = i?.getString("sId") ?: ""
        val rId = i?.getString("rId") ?: ""

        list = ArrayList()

        // create rooms
        val senderRoom = sId + rId
        val receiverRoom = rId + sId

        adapter = ChatAdapter(this, list, rId,
            sId, senderRoom , receiverRoom)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager =
            LinearLayoutManager(this)

        val currentUserId = FirebaseAuth.getInstance().currentUser?.
        uid.toString()
        // set name
        binding.userName.text = if (sId == currentUserId) {
            receiverName
        }
        else{
            senderName
        }

        binding.pic.setImageResource(R.drawable.e)

        binding.send.setOnClickListener {
            if (binding.message.text.isEmpty()) {
                Toast.makeText(
                    this@ChatDetailsActivity,
                    "Please Enter your message", Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                val message = Message(
                    message = binding.message.text.toString(),
                    id = FirebaseAuth.getInstance().uid.toString(),//current user
                    timeStamp = Date().time,
                    senderName = senderName
                )

                // create note and save data in sender room
                firebaseDatabase.reference
                    .child("Chats")
                    .child(senderRoom)
                    .child("message")
                    .push()
                    .setValue(message)
                    .addOnSuccessListener {
                        // save data in receive room too
                        val message1 = Message(
                            message = binding.message.text.toString(),
                            id = FirebaseAuth.getInstance().uid.toString(),//current user
                            timeStamp = Date().time,
                            receiverName = receiverName
                        )
                        firebaseDatabase.reference
                            .child("Chats")
                            .child(receiverRoom)
                            .child("message")
                            .push()
                            .setValue(message1)
                            .addOnSuccessListener {
                                binding.message.text = null
                                Toast.makeText(
                                    this@ChatDetailsActivity,
                                    "Message sent!!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
            }
        }

        // back button
        binding.back.setOnClickListener {
            startActivity(
                Intent(
                    this@ChatDetailsActivity,
                    MainActivity::class.java
                )
            )
        }

        // get message
        firebaseDatabase.reference
            .child("Chats")
            .child(senderRoom)
            .child("message")
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    (list as ArrayList).clear()
                    if (snapshot.exists()) {
                        for (snap in snapshot.children) {

                            val model = snap.getValue(Message::class.java)

                            if (model != null) {
                                (list as ArrayList).add(model)
                            }
                        }
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@ChatDetailsActivity,
                        "Error $error", Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })

        val sp =
            getSharedPreferences("Room", Context.MODE_PRIVATE)
        val e = sp.edit()
        e.putString("sRoom", senderRoom)
        e.apply()
    }
}