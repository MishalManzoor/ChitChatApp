package com.example.chitchat.createAccount

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.chitchat.MainActivity
import com.example.chitchat.databinding.ActivitySignUpBinding
import com.example.chitchat.models.Users
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var progressDialog: ProgressDialog
    private lateinit var verify: Task<Void>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        supportActionBar?.hide()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Creating Account")
        progressDialog.setMessage("We are Creating you Account")

        binding.AlreadyHaveAccount.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        binding.userSignUp.setOnClickListener {
            signUp()
        }

        if (mAuth.currentUser?.email?.isNotEmpty() == true) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun signUp() {
        if (binding.userName.text.toString().isNotEmpty()
            &&
            binding.userEmail.text.toString().isNotEmpty()
            &&
            binding.userPassword.text.toString().isNotEmpty()
        ) {
            progressDialog.show()

            // Username is unique, proceed with user registration
            mAuth.createUserWithEmailAndPassword(
                binding.userEmail.text.toString(),
                binding.userPassword.text.toString()
            )
                .addOnCompleteListener { task ->
                    progressDialog.dismiss()

                    if (task.isSuccessful) {

                        val user1 = mAuth.currentUser
                        // Create a UserProfileChangeRequest to set the display name
                        val update = UserProfileChangeRequest.Builder()
                            .setDisplayName(binding.userName.text.toString())
                            .build()

                        // Update the user's profile with the display name
                        user1?.updateProfile(update)
                            ?.addOnSuccessListener {
                                // Profile updated successfully
                                verify = mAuth.currentUser?.sendEmailVerification()
                                    ?.addOnSuccessListener {

                                        Toast.makeText(
                                            this@SignUpActivity,
                                            "Please Verify your Email",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        val id = task.result.user?.uid.toString()

                                        val user = Users(
                                           name = binding.userName
                                                .text
                                                .toString(),
                                            email = binding.userEmail.text.toString(),
                                            password = binding.userPassword.text.toString(),
                                            id = id
                                        )

                                        firebaseDatabase.reference
                                            .child("Users")
                                            .child(id)
                                            .setValue(user)

                                        binding.userName.setText("")
                                        binding.userEmail.setText("")
                                        binding.userPassword.setText("")

                                        startActivity(
                                            Intent(
                                                this@SignUpActivity,
                                                VerifyEmail::class.java
                                            )
                                        )
                                    }?.addOnFailureListener {
                                        Toast.makeText(
                                            this@SignUpActivity,
                                            "Sign up failed: " +
                                                    "${task.exception?.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } as Task<Void>

                            }?.addOnFailureListener {
                                Toast.makeText(
                                    this@SignUpActivity,
                                    "Error updating profile$it",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            this@SignUpActivity,
                            task.exception.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            if (binding.userName.text.toString().isEmpty()) {
                binding.userName.error = "Enter your name"
                return
            }
            if (binding.userEmail.text.toString().isEmpty()) {
                binding.userEmail.error = "Enter your email"
                return
            }

            if (binding.userPassword.text.toString().isEmpty()) {
                binding.userPassword.error = "Enter your password"
                return
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

        val dialogBuilder = AlertDialog.Builder(this@SignUpActivity)

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