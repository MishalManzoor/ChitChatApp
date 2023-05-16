package com.example.chitchat.createAccount

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.chitchat.MainActivity
import com.example.chitchat.R
import com.example.chitchat.databinding.ActivitySignInBinding
import com.example.chitchat.models.Users
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class SignInActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySignInBinding
    private lateinit var mAuth : FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var progressDialog: ProgressDialog

    private var verify : Boolean = false
    private var isAlreadyLogin : Boolean = false

    private lateinit var client : GoogleSignInClient

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

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        client = GoogleSignIn.getClient(this , gso)


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

                            verify = mAuth.currentUser?.isEmailVerified == true

                            if (verify) {
                                isAlreadyLogin = true

                               startActivity(Intent(this,
                                      MainActivity::class.java))
                              }
                           else{
                             Toast.makeText(
                                  this,
                                       "Please Verify your account",
                                             Toast.LENGTH_SHORT)
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

        binding.userGoogle.setOnClickListener {
            signIn()
        }

        if (mAuth.currentUser?.email?.isNotEmpty() == true && verify){
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private var RC_SIGN_IN = 65

    private fun signIn(){

        val intent = client.signInIntent
        // we will get data from second activity to first activity
        // we will get image, email etc
        startActivityForResult(intent , RC_SIGN_IN)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RC_SIGN_IN){
           val task : Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account : GoogleSignInAccount = task.getResult(ApiException::class.java)
                Log.d("check", " firebaseAuthWithGoogle ${account.id}")

                progressDialog.show()

                firebaseAuthWithGoogle(account.idToken)
            }
            catch (e : ApiException){
                Log.w("check", "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {

        val credential = GoogleAuthProvider.getCredential(idToken , null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // sign in successful, update ui
                    Log.e("check", "signInWithCredential : success")

                    val user = mAuth.currentUser

                    val users = Users()
                    if (user != null) {
                        users.id = user.uid
                        users.profilePic = user.photoUrl.toString()
                        users.name = user.displayName.toString()
                        firebaseDatabase.reference
                            .child("Users")
                            .child(user.uid).setValue(users)
                    }


                    startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                    Toast.makeText(
                        this@SignInActivity,
                        "Sign in with Google",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {
                    Log.w("check", "signInWithCredential : failure", task.exception)
                }
            }
    }
}