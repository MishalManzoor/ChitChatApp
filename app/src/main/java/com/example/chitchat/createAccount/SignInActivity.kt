package com.example.chitchat.createAccount

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.chitchat.MainActivity
import com.example.chitchat.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignInActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySignInBinding
    private lateinit var mAuth : FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var progressDialog: ProgressDialog

    private var verify : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        mAuth = FirebaseAuth.getInstance()

        firebaseDatabase = FirebaseDatabase.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Login")
        progressDialog.setMessage("Please Wait\nValidation in Progress")

        binding.userSignIn.setOnClickListener {

            if (binding.userEmailLogIn.text.toString().isNotEmpty()
                &&
                binding.userPasswordLognIn.text.toString().isNotEmpty()) {

                progressDialog.show()

                mAuth.signInWithEmailAndPassword(
                    binding.userEmailLogIn.text.toString(),
                    binding.userPasswordLognIn.text.toString())

                    .addOnCompleteListener { task ->
                        progressDialog.dismiss()
                        if (task.isSuccessful) {

                            verify = mAuth
                                .currentUser?.isEmailVerified == true

                            if (verify) {
                                startActivity(
                                    Intent(
                                        this,
                                        MainActivity::class.java
                                    )
                                )
                            } else {
                                mAuth.signOut()
                                Toast.makeText(
                                    this,
                                    "Please Verify your account",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }

                        } else {
                            Toast.makeText(
                                this,
                                task.exception.toString(),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }.addOnFailureListener {
                        Toast.makeText(this,
                            "Something is wrong!",
                            Toast.LENGTH_SHORT).show()
                    }
            } else {
                if(binding.userEmailLogIn.text.toString().isEmpty()){
                    binding.userEmailLogIn.error = "Enter your email"

                    return@setOnClickListener
                }
                if (binding.userPasswordLognIn.text.toString().isEmpty()){
                    binding.userPasswordLognIn.error = "Enter your Password"
                    return@setOnClickListener
                }
            }
        }

        binding.CreateAccount.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        if (mAuth.currentUser?.email?.isNotEmpty() == true && verify){
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}