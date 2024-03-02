package com.cos30049.chattingapp

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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.util.Date

class SetupProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var selectedImage : Uri
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



        binding.uploadImage.setOnClickListener{
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 45)
        }
//        val storageRef = storage.reference.child("avatars/${auth.currentUser?.uid}.jpg")
//
//        val uploadTask = storageRef.putFile(selectedImage)

        userName = findViewById(R.id.userName)
        setUpProfile = findViewById(R.id.setUpProfile)


//        if (auth.currentUser == null) {
//            val intent = Intent(this@SetupProfileActivity, LoginActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
        userName.requestFocus()

        binding.setUpProfile.setOnClickListener {

            val uid = auth.uid!!
            val email = auth.currentUser!!.email
            val name:String = binding.userName.text.toString()

            if (TextUtils.isEmpty(name)) {
                userName.error = "Please type a name"
                return@setOnClickListener
            }

            val progressDialog = ProgressDialog(this@SetupProfileActivity)
            progressDialog.setMessage("Uploading Profile...")
            progressDialog.show()

            val reference = storage.reference.child("Profile")
                .child(auth.uid!!)
            reference.putFile(selectedImage).addOnCompleteListener{task ->
                progressDialog.dismiss()
                if (task.isSuccessful){
                    reference.downloadUrl.addOnCompleteListener{ uri ->
                        val imageUrl = uri.toString()
                        val user = User(uid, name, email, imageUrl)
                        database.reference
                            .child("users")
                            .child(uid)
                            .setValue(user)
                            .addOnCompleteListener{
                                val intent = Intent(this@SetupProfileActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                    }
                } else {
                    val user = User(uid, name, email, "No Image")
                    database.reference
                        .child("users")
                        .child(uid)
                        .setValue(user)
                        .addOnCanceledListener{
                            val intent = Intent(this@SetupProfileActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && data.data != null) {
            val uri = data.data
            val storage = FirebaseStorage.getInstance()
            val time = Date().time
            val reference = storage.reference
                .child("Profile")
                .child(time.toString() + "")
            reference.putFile(uri!!)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        reference.downloadUrl.addOnCompleteListener { uri ->
                            val filePath = uri.toString()
                            val obj = HashMap<String, Any>()
                            obj["image"] = filePath
                            database.reference
                                .child("users")
                                .child(auth.uid!!)
                                .updateChildren(obj)
                                .addOnSuccessListener { }
                        }
                    }
                }
                .addOnSuccessListener {
                    binding.avatar.setImageURI(uri)
                    selectedImage = uri
                }
        }
    }
}