package com.example.chitchat.settings

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.chitchat.MainActivity
import com.example.chitchat.R
import com.example.chitchat.databinding.ActivitySettingsBinding
import com.example.chitchat.models.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySettingsBinding

    private lateinit var mauth : FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var storage : FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        mauth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        binding.backBtn.setOnClickListener {
            startActivity(
                Intent(this, MainActivity::class.java))
        }

        binding.addBtn.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            previewRequest.launch(intent)
        }

        binding.saveBtn.setOnClickListener{
            val name = binding.userName.text.toString()
            val about = binding.about.text.toString()

            //  to update the value
            val obj = HashMap<String , Any>()
            obj["name"] = name
            obj["status"] = about

            firebaseDatabase.reference.child("Users")
                .child(FirebaseAuth.getInstance().uid.toString())
                .updateChildren(obj)

            Toast.makeText(
                this,
                "Profile Updated", Toast.LENGTH_SHORT
            )
                .show()
        }

        firebaseDatabase.reference.child("Users")
            .child(FirebaseAuth.getInstance().uid.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    val users = snapshot.getValue(Users::class.java)
                    // get Image
                    // set Image
                    if (users!!.profilePic.isEmpty()) {
                        binding.profilePic1.setImageResource(R.drawable.avatar3)
                    } else {
                        Picasso.get().load(users.profilePic)
                            .placeholder(R.drawable.avatar3).into(binding.profilePic1)
                    }

                    // get name
                    binding.userName.setText(users.name)
                    // get about
                    binding.about.setText(users.status)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("TAG",error.toString())
                }
            })
    }

    private val previewRequest =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
            // by default its null, if user is not set any image it is null
            if (it.data?.data != null) {

                val mFile : Uri? = it.data!!.data
                binding.profilePic1.setImageURI(mFile)

                val reference : StorageReference =
                    storage.reference.child("Profile_picture")
                    .child(FirebaseAuth.getInstance().uid.toString())

                reference.putFile(mFile!!)
                    .addOnSuccessListener {
                        // get image url
                        reference.downloadUrl.addOnSuccessListener {
                            firebaseDatabase.reference.child("Users")
                                .child(FirebaseAuth.getInstance().uid.toString())
                                .child("profilePic").setValue(it.toString())
                        }
                    }
            }
        }
}