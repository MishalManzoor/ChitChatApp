package com.example.chitchat.settings

import android.content.Context
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
    private lateinit var binding: ActivitySettingsBinding

    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        mAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        val sp =
            getSharedPreferences(
                "Update1",
                Context.MODE_PRIVATE
            )
        val name1 = sp.getString("name", "")
        val about1 = sp.getString("about", "")
        val pic1 = sp.getString("pic", "")

        binding.userName.setText(name1)
        binding.about.setText(about1)

        if (pic1 != null) {
            if (pic1.isEmpty()) {
                binding.profilePic1
                    .setImageResource(R.drawable.avatar3)
            } else {
                Picasso.get().load(pic1)
                    .into(binding.profilePic1)
            }
        }

        binding.backBtn.setOnClickListener {
            startActivity(
                Intent(this, MainActivity::class.java)
            )
        }

        binding.addBtn.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            previewRequest.launch(intent)
        }

        binding.saveBtn.setOnClickListener {
            val name = binding.userName.text.toString()
            val about = binding.about.text.toString()

            val truncatedName1 = truncatedText(name)
            val truncatedAbout1 = truncatedText(about)

            // get name
            binding.userName.setText(truncatedName1)
            // get about
            binding.about.setText(truncatedAbout1)

            //  to update the value
            val obj = HashMap<String, Any>()
            obj["name"] = name
            obj["status"] = about

            // update Users
            firebaseDatabase.reference
                .child("Users")
                .child(mAuth.currentUser?.uid.toString())
                .updateChildren(obj)

            Toast.makeText(
                this,
                "Profile Updated", Toast.LENGTH_SHORT
            )
                .show()
        }

        firebaseDatabase.reference.child("Users")
            .child(mAuth.currentUser?.uid.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val users = snapshot.getValue(Users::class.java)

                    // set Image
                    if (users != null) {
                        if (users.profilePic.isEmpty()) {
                            binding.profilePic1
                                .setImageResource(R.drawable.avatar3)
                        } else {
                            Picasso.get().load(users.profilePic)
                                .into(binding.profilePic1)
                        }

                        val truncatedName = truncatedText(users.name)
                        val truncatedAbout = truncatedText(users.status)

                        // get name
                        binding.userName.setText(truncatedName)
                        // get about
                        binding.about.setText(truncatedAbout)

                        val sp1 =
                            getSharedPreferences(
                                "Update1",
                                Context.MODE_PRIVATE)
                        val e = sp1.edit()
                        e.putString("name", users.name)
                        e.putString("about", users.status)
                        e.putString("pic", users.profilePic)
                        e.apply()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("UsersUpdate5", error.toString())
                }
            })
    }

    private fun truncatedText(text: String): String {
        val maxLength = 12
        return if (text.length > maxLength) {
            text.substring(0, maxLength)
        } else {
            text
        }
    }

    private val previewRequest =
        registerForActivityResult(
            ActivityResultContracts
                .StartActivityForResult()
        )
        { it ->
            // by default its null, if user is not set any image it is null
                if (it.data?.data != null) {

                    val mFile: Uri? = it.data!!.data
                    binding.profilePic1.setImageURI(mFile)

                    val userId = mAuth.uid
                    val reference: StorageReference =
                        storage.reference
                            .child("users/$userId/profilePic")

                    reference.putFile(mFile!!)
                        .addOnSuccessListener {
                            // get image url
                            reference.downloadUrl.addOnSuccessListener {
                                // Successfully got the download URL
                                val imageURL = it.toString()

                                firebaseDatabase.reference.child("Users")
                                    .child(FirebaseAuth.getInstance().uid.toString())
                                    .child("profilePic")
                                    .setValue(imageURL)
                            }
                        }.addOnFailureListener {
                            Log.d("TAG", "$it")
                        }
                }
        }
}