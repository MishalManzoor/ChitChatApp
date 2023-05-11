package com.example.chitchat.createAccount

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chitchat.databinding.ActivityVerifyEmailBinding
import com.google.firebase.auth.FirebaseAuth

class VerifyEmail : AppCompatActivity() {

    lateinit var binding : ActivityVerifyEmailBinding
    lateinit var mAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        binding.vLogin.setOnClickListener {

            startActivity(Intent(this, SignInActivity::class.java))
        }
    }
}