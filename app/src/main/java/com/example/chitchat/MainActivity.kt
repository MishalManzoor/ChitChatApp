package com.example.chitchat

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chitchat.adapter.ShowChatAdapter
import com.example.chitchat.chatDetails.ChatDetailsActivity
import com.example.chitchat.friendList.ActivityForFragments
import com.example.chitchat.createAccount.SignInActivity
import com.example.chitchat.databinding.ActivityMainBinding
import com.example.chitchat.models.FriendList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() , ShowChatAdapter.OnActionClick{

    private lateinit var binding: ActivityMainBinding

    private lateinit var mAuth: FirebaseAuth

    private lateinit var firebaseDatabase: FirebaseDatabase

    lateinit var adapter : ShowChatAdapter

    val friendList: ArrayList<FriendList> = ArrayList()

    @SuppressLint("MissingInflatedId", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        firebaseDatabase = FirebaseDatabase.getInstance()

        binding.recyclerView.layoutManager =
            LinearLayoutManager(this)
        adapter = ShowChatAdapter(friendList, this)
        binding.recyclerView.adapter = adapter

        val currentUserEmail = FirebaseAuth.getInstance()
            .currentUser?.email.toString()

        val encodedEmail = encodeMail(currentUserEmail)

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
                            val receiverName = item.child("receiverName")
                                .getValue(String::class.java).toString()
                            val senderName = item.child("senderName")
                                .getValue(String::class.java).toString()
                            val sender = item.child("sender")
                                .getValue(String::class.java).toString()
                            val receiver = item.child("receiver")
                                .getValue(String::class.java).toString()
                            val senderId = item.child("senderId")
                                .getValue(String::class.java).toString()
                            val receiverId = item.child("receiverId")
                                .getValue(String::class.java).toString()

                            val addToFriendList = FriendList(
                                senderName = senderName,
                                sender = sender,
                                receiverName = receiverName,
                                receiver = receiver,
                                senderId = senderId,
                                receiverId = receiverId

                            )
                            friendList.add(addToFriendList)
                        }
                        adapter.notifyDataSetChanged()
                    }
                }
            }

        binding.floatingActionButton.setOnClickListener {
            startActivity(Intent(this,
                ActivityForFragments::class.java))
        }
    }

    private fun showInAdapter(list : ArrayList<FriendList>){
        binding.recyclerView.layoutManager =
            LinearLayoutManager(this)
        adapter = ShowChatAdapter(list, this)
        binding.recyclerView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val inflate = menuInflater
        inflate.inflate(R.menu.menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                mAuth.signOut()
                startActivity(Intent(this, SignInActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @Deprecated(
        "Deprecated in Java",
        ReplaceWith("super.onBackPressed()",
            "androidx.appcompat.app.AppCompatActivity")
    )
    override fun onBackPressed() {

        val dialogBuilder = AlertDialog.Builder(this@MainActivity)

        dialogBuilder.setTitle("Exist")
            .setMessage("Are you sure you want to exist this App?")
            // positive button text and action
            .setPositiveButton("Exist") { _, _ ->
                finishAffinity()
            }
            // negative button text and action
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

        // create dialog box
        val alert = dialogBuilder.create()
        // show alert dialog
        alert.show()
    }

    private fun encodeMail(mail : String) : String{
        return Base64.encodeToString(
            mail.toByteArray(),
            Base64.NO_WRAP
        )
    }

    override fun onClick(
        clickedFriendEmail: String,
        senderName: String,
        senderEmail: String,
        receiverEmail: String,
        receiverName: String,
        senderId: String,
        receiverId: String)
    {

        val intent = Intent(this, ChatDetailsActivity::class.java)
        intent.putExtra("clickedEmail", clickedFriendEmail)
        intent.putExtra("sName", senderName)
        intent.putExtra("rName", receiverName)
        intent.putExtra("s", senderEmail)
        intent.putExtra("sId", senderId)
        intent.putExtra("rId", receiverId)
        startActivity(intent)
    }
}
