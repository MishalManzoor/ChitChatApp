package com.example.chitchat.group

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chitchat.MainActivity
import com.example.chitchat.adapter.ChatAdapter
import com.example.chitchat.databinding.ActivityGroupChatBinding
import com.example.chitchat.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class GroupChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupChatBinding

    private lateinit var mauth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    private lateinit var list: List<Message>
    private lateinit var adapter: ChatAdapter

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGroupChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        list = ArrayList()

        mauth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        adapter = ChatAdapter(this, list, "")
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // sender id
        val senderId = FirebaseAuth.getInstance().uid.toString()

        binding.userName1.text = "Friends Group"

        binding.back1.setOnClickListener {
            startActivity(
                Intent(this, MainActivity::class.java)
            )
        }

        binding.send1.setOnClickListener {
            if (binding.message1.text.isEmpty()) {
                Toast.makeText(
                    this@GroupChatActivity,
                    "Please Enter your message", Toast.LENGTH_SHORT
                ).show()
            } else {
                val message = Message(
                    binding.message1.text.toString(), senderId, Date().time
                )

                firebaseDatabase.reference.child("Group Chat")
                    .push()// to get unique id's
                    .setValue(message)
                    .addOnSuccessListener {
                        binding.message1.setText("")
                    }
            }
        }

        // get data in recyclerView
        firebaseDatabase.reference.child("Group Chat")
            .addValueEventListener(object : ValueEventListener { // get value from database
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {

                    (list as ArrayList).clear()

                    for (snap in snapshot.children) {

                        val model = snap.getValue(Message::class.java)

                        (list as ArrayList).add(model!!)
                    }
                    adapter.notifyDataSetChanged()
                    //adapter.notifyItemChanged(updateIndex);
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("TAG", error.toString())
                }
            })
    }
}