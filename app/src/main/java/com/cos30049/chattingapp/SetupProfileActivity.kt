package com.cos30049.chattingapp

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import com.cos30049.chattingapp.databinding.ActivitySetupProfileBinding
import com.cos30049.chattingapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.Date

class SetupProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private var selectedImage: Uri? = null
    private lateinit var userName: EditText
    private lateinit var setUpProfile: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        supportActionBar?.hide()

        binding.uploadImage.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 45)
        }

        userName = findViewById(R.id.userName)
        setUpProfile = findViewById(R.id.setUpProfile)

        userName.requestFocus()

        setUpProfile.setOnClickListener {
            val name: String = binding.userName.text.toString()

            if (TextUtils.isEmpty(name)) {
                userName.error = "Please type a name"
                return@setOnClickListener
            }

            val progressDialog = ProgressDialog(this@SetupProfileActivity)
            progressDialog.setMessage("Uploading Profile...")
            progressDialog.show()

            val uid = auth.uid ?: ""
            val email = auth.currentUser?.email ?: ""

            if (selectedImage != null) {
                // User uploaded an image
                val reference = storage.reference.child("Profile").child(uid)
                reference.putFile(selectedImage!!)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            reference.downloadUrl.addOnCompleteListener { uriTask ->
                                if (uriTask.isSuccessful) {
                                    val imageUrl = uriTask.result.toString()
                                    val user = User(uid, name, email, imageUrl)
                                    database.reference
                                        .child("users")
                                        .child(uid)
                                        .setValue(user)
                                        .addOnCompleteListener {
                                            val intent =
                                                Intent(this@SetupProfileActivity, MainActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                } else {
                                    // Handle failure to get image URL
                                    // For example, display an error message
                                }
                            }
                        } else {
                            // Handle image upload failure
                            // For example, display an error message
                        }
                    }
            } else {
                // User did not upload an image
                val user = User(uid, name, email, "No Image")
                database.reference
                    .child("users")
                    .child(uid)
                    .setValue(user)
                    .addOnCompleteListener {
                        val intent = Intent(this@SetupProfileActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && data.data != null) {
            val uri = data.data
            binding.avatar.setImageURI(uri)
            selectedImage = uri
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}
