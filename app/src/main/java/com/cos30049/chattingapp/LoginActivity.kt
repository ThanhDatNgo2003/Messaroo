package com.cos30049.chattingapp

import android.annotation.SuppressLint
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
                    // Hide the progress indicator
                    progressDialog.dismiss()
                    // Re-enable the login button
                    login.isEnabled = true

                    if (task.isSuccessful) {
                        // Handle successful login
                        val currentUser = auth.currentUser
                        val uid = currentUser!!.uid
                        val usersRef = database.reference.child("users").child(uid)
                        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
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
                        // Handle the failed login attempt
                        Toast.makeText(
                            this@LoginActivity,
                            "Login failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

        }
    }


    @Deprecated("Deprecated in Java", ReplaceWith("finishAffinity()"))
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // If the user is not signed in, prevent navigation back to MainActivity
        finishAffinity()
    }


    fun onSignUpClicked(view: View) {
        val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }


    private fun updateUI(user: FirebaseUser?) {
        TODO("Not yet implemented")
    }

}
