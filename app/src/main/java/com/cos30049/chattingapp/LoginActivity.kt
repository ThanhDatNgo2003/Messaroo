package com.cos30049.chattingapp

//import android.content.Intent
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.view.View
//import androidx.databinding.DataBindingUtil
//import androidx.databinding.ViewDataBinding
//import com.cos30049.chattingapp.databinding.ActivityLoginBinding
//import com.google.firebase.auth.FirebaseAuth
//
//class LoginActivity : AppCompatActivity() {
//
//    var binding: ActivityLoginBinding? = null
//
//    private var auth: FirebaseAuth? = null
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val binding = ActivityLoginBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        auth = FirebaseAuth.getInstance()
//        if (auth!!.currentUser != null){
//            val intent = Intent(this@LoginActivity,MainActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//        supportActionBar!!.hide()
//        binding.loginPhoneNum.requestFocus()
//        binding.loginPassword.requestFocus()
//        binding.login.setOnClickListener {
//            val intent = Intent(this@LoginActivity,MainActivity::class.java)
//            intent.putExtra("loginPhoneNum", binding.loginPhoneNum.text.toString())
//            startActivity(intent)
//        }
//    }
//
//    fun onSignUpClicked(view: View) {}
//    fun onLogInClicked(view: View) {}
//}


// LoginActivity.kt

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.mindrot.jbcrypt.BCrypt

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var loginPassword: EditText
    private lateinit var loginEmail: EditText
    private lateinit var login: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        loginEmail = findViewById(R.id.loginEmail)
        loginPassword = findViewById(R.id.loginPassword)
        login = findViewById(R.id.login)


        // Check if a user is already authenticated
        if (auth.currentUser != null) {
            val intent = Intent(this@LoginActivity, SetupProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Set focus on input fields (optional)
        loginEmail.requestFocus()
        loginPassword.requestFocus()

        // Login button click listener
        login.setOnClickListener {

            val email = loginEmail.text.toString()
            val password = loginPassword.text.toString()

            // Validate input fields
            if (TextUtils.isEmpty(email)) {
                loginEmail.error = "Email is required"
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                loginEmail.error = "Invalid email format"
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(password)) {
                loginPassword.error = "Password is required"
                return@setOnClickListener
            }

            // Disable the login button and show a progress indicator
            login.isEnabled = false
            val progressDialog = ProgressDialog(this@LoginActivity)
            progressDialog.setMessage("Login to Messaroo...")
            progressDialog.show()

            // Perform login logic
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    // Hide the progress indicator and enable the register button
                    login.isEnabled = true
                    if (task.isSuccessful) {
                        val currentUser = auth.currentUser
                        val uid = currentUser!!.uid
                        val usersRef = database.reference.child("users").child(uid)
                        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                progressDialog.dismiss()
                                if (dataSnapshot.exists() && dataSnapshot.child("name").value != null) {
                                    // Username exists, navigate to MainActivity
                                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                    finish()
                                } else {
                                    val intent = Intent(this@LoginActivity, SetupProfileActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }

                            }
                            override fun onCancelled(databaseError: DatabaseError) {
                            // Handle database error
                            }
                        })
                    } else {
                        // Handle the possible exceptions
                        when (val exception = task.exception) {

                            is FirebaseNetworkException -> {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "No network connection",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            else -> {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Login failed: ${exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
        }
    }


    fun onSignUpClicked(view: View) {
        val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

//    private fun signIn(email: String, password: String) {
//        // [START sign_in_with_email]
//        auth.signInWithEmailAndPassword(email, password)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    // Sign in success, update UI with the signed-in user's information
//                    Log.d(TAG, "signInWithEmail:success")
//                    val user = auth.currentUser
//                    updateUI(user)
//                } else {
//                    // If sign in fails, display a message to the user.
//                    Log.w(TAG, "signInWithEmail:failure", task.exception)
//                    Toast.makeText(
//                        baseContext,
//                        "Authentication failed.",
//                        Toast.LENGTH_SHORT,
//                    ).show()
//                    updateUI(null)
//                }
//            }
//        // [END sign_in_with_email]
//    }

    private fun updateUI(user: FirebaseUser?) {
        TODO("Not yet implemented")
    }

}
