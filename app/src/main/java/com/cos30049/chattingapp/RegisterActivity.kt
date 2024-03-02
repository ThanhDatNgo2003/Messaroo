package com.cos30049.chattingapp

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import org.mindrot.jbcrypt.BCrypt

//
//class RegisterActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_register)
//    }
//}



class RegisterActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth
    private lateinit var regisEmail: EditText
    private lateinit var regisPassword: EditText
    private lateinit var regisConfirmPassword: EditText
    private lateinit var regis: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth

        // Initialize UI elements
        regisEmail = findViewById(R.id.regisEmail)
        regisPassword = findViewById(R.id.regisPassword)
        regisConfirmPassword = findViewById(R.id.regisConfirmPassword)
        regis = findViewById(R.id.regis)

        // Register button click listener
        regis.setOnClickListener {
            val email = regisEmail.text.toString()
            val password = regisPassword.text.toString()
            val confirmPassword = regisConfirmPassword.text.toString()

            // Validate input fields
            if (TextUtils.isEmpty(email)) {
                regisEmail.error = "Email is required"
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                regisEmail.error = "Invalid email format"
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(password)) {
                regisPassword.error = "Password is required"
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(confirmPassword)) {
                regisConfirmPassword.error = "Confirm password is required"
                return@setOnClickListener
            }

            // Validate password and confirm password match
            if (password == confirmPassword) {
                // Disable the register button and show a progress indicator
                regis.isEnabled = false
                val progressDialog = ProgressDialog(this@RegisterActivity)
                progressDialog.setMessage("Registering user...")
                progressDialog.show()

                // Perform registration logic (e.g., create user account)
                // You can use Firebase Authentication or any other backend service

                // Hash the password before storing it (using BCrypt for secure hashing)
                val passwordSalt = BCrypt.gensalt()
                val hashedPassword = BCrypt.hashpw(password, passwordSalt)
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        // Hide the progress indicator and enable the register button
                        progressDialog.dismiss()
                        regis.isEnabled = true

                        if (task.isSuccessful) {
                            // Save the user data (username, email, hashed password, etc.) to your database
                            // Here, you would typically use an API call to your backend server

                            // After successful registration, sign out the user
                            FirebaseAuth.getInstance().signOut()

                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Handle the possible exceptions
                            when (val exception = task.exception) {
                                is FirebaseAuthUserCollisionException -> {
                                    Toast.makeText(this@RegisterActivity, "Email is already in use", Toast.LENGTH_SHORT).show()
                                }

                                is FirebaseNetworkException -> {
                                    Toast.makeText(this@RegisterActivity, "No network connection", Toast.LENGTH_SHORT).show()
                                }

                                else -> {
                                    Toast.makeText(this@RegisterActivity, "Registration failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
            } else {
                // Show an error message (passwords do not match)
                regisConfirmPassword.error = "Password does not match"
                return@setOnClickListener            }
        }

    }

    // Optional: Handle login button click separately
    fun onLogInClicked(view: View) {
        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

//    private fun createAccount(email: String, password: String) {
//        // [START create_user_with_email]
//        auth.createUserWithEmailAndPassword(email, password)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    // Sign in success, update UI with the signed-in user's information
//                    Log.d(TAG, "createUserWithEmail:success")
//                    val user = auth.currentUser
//                    // After successful registration, sign out the user
//                    FirebaseAuth.getInstance().signOut()
//
//                    updateUI(user)
//                } else {
//                    // If sign in fails, display a message to the user.
//                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
//                    Toast.makeText(
//                        baseContext,
//                        "Authentication failed.",
//                        Toast.LENGTH_SHORT,
//                    ).show()
//                    updateUI(null)
//                }
//            }
//        // [END create_user_with_email]
//    }

    private fun updateUI(user: FirebaseUser?) {
        TODO("Not yet implemented")
    }

}



