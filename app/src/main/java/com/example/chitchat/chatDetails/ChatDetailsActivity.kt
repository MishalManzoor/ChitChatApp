package com.example.chitchat.chatDetails

import android.annotation.SuppressLint
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
import com.squareup.picasso.Picasso
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

        list = ArrayList()

        adapter = ChatAdapter(this, list)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        val profilePic = intent.getStringExtra("pic")!!
        val name = intent.getStringExtra("name")!!

        // set name
        binding.userName.text = name

        // set Image
        if (profilePic.isEmpty()) {
//            Glide.with(this).load(R.drawable.avatar3).into(binding.pic)
            binding.pic.setImageResource(R.drawable.avatar3)
        } else {
            //Glide.with(this).load(pic).into(binding.pic)
            Picasso.get().load(profilePic)
                .placeholder(R.drawable.avatar3).into(binding.pic)
        }

        // sender id
        val senderId = FirebaseAuth.getInstance().uid.toString()
        // receiver id
        val receiverId = intent.getStringExtra("id")!!
        // create rooms
        val senderRoom = senderId + receiverId
        val receiverRoom = receiverId + senderId

        binding.send.setOnClickListener {
            if (binding.message.text.isEmpty()) {
                Toast.makeText(
                    this@ChatDetailsActivity,
                    "Please Enter your message", Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                val message = Message(
                    binding.message.text.toString(), senderId, Date().time
                )

                // this random key will store this message. This key will be unique
                val randomKey = firebaseDatabase.reference.push().key

                // create note and save data in sender room
                firebaseDatabase.reference.child("Chats")
                    .child(senderRoom)
                    .child("message")
                    // .child(randomKey)
                    .push()
                    .setValue(message)
                    .addOnSuccessListener {

                        // save data in receive room too
                        firebaseDatabase.reference.child("Chats")
                            .child(receiverRoom)
                            .child("message")
                           // .child(randomKey)
                            .push()
                            .setValue(message)
                            .addOnSuccessListener {
                                binding.message.text = null
                                Toast.makeText(
                                    this@ChatDetailsActivity,
                                    "Message sent!!", Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                    }
            }
        }

        // back button
        binding.back.setOnClickListener {
            startActivity(
                Intent(this@ChatDetailsActivity, MainActivity::class.java)
            )
        }

        firebaseDatabase.reference.child("Chats")
            .child(senderRoom)
            .child("message")
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {

                    (list as ArrayList).clear()

                    for (snap in snapshot.children) {

                        val model = snap.getValue(Message::class.java)

                        (list as ArrayList).add(model!!)
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@ChatDetailsActivity,
                        "Error $error", Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })
    }
}