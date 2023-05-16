package com.example.chitchat.createAccount

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.chitchat.databinding.ActivityVerifyEmailBinding
import com.google.firebase.auth.FirebaseAuth

class VerifyEmail : AppCompatActivity() {

    private lateinit var binding : ActivityVerifyEmailBinding
    private lateinit var mAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        binding.vLogin.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

        val dialogBuilder = AlertDialog.Builder(this@VerifyEmail)

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