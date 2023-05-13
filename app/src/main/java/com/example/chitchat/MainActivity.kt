package com.example.chitchat

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chitchat.adapter.UserAdapter
import com.example.chitchat.createAccount.SignInActivity
import com.example.chitchat.databinding.ActivityMainBinding
import com.example.chitchat.group.GroupChatActivity
import com.example.chitchat.models.Users
import com.example.chitchat.settings.SettingsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private lateinit var mauth: FirebaseAuth

    private lateinit var firebaseDatabase: FirebaseDatabase

    private var mList: List<Users> = ArrayList()

    private lateinit var adapter: UserAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mauth = FirebaseAuth.getInstance()

        firebaseDatabase = FirebaseDatabase.getInstance()

        adapter = UserAdapter(mList, this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        firebaseDatabase.reference.child("Users")
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {

                    (mList as ArrayList).clear()

                    for (snap: DataSnapshot in snapshot.children) {

                        val users = snap.getValue(Users::class.java)

                        if (users != null) {
                            users.id = snap.key.toString()
                            if (users.id != FirebaseAuth.getInstance().uid) {
                                (mList as ArrayList).add(users)
                            }
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("TAG", error.toString())
                }
            })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val inflate = menuInflater
        inflate.inflate(R.menu.menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.group_chat -> {
                startActivity(Intent(this, GroupChatActivity::class.java))
            }

            R.id.setting -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }

            R.id.logout -> {

                mauth.signOut()
                startActivity(Intent(this, SignInActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @Deprecated("Deprecated in Java",
        ReplaceWith("super.onBackPressed()", "androidx.appcompat.app.AppCompatActivity")
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
}