package com.cos30049.chattingapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var bottomNavigationView: BottomNavigationView
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var userNameMain: TextView
    private lateinit var userAvatar: CircleImageView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        userNameMain = findViewById(R.id.userNameMain)
        userAvatar = findViewById(R.id.userAvatar)

        if (auth.currentUser == null) {
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
        } else {
            // Retrieve user's name and avatar from Firebase
            val currentUser = auth.currentUser
            val uid = currentUser!!.uid
            val usersRef = database.reference.child("users").child(uid)
            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        startActivity(Intent(this@MainActivity, SetupProfileActivity::class.java))
                        finish()
                    } else {
                        // Retrieve user's name and avatar
                        val userName = dataSnapshot.child("name").value.toString()
                        val avatarUrl = dataSnapshot.child("imageUrl").value.toString()

                        // Update userBar views
                        userNameMain.text = userName
                        Glide.with(this@MainActivity)
                            .load(avatarUrl)
                            .placeholder(R.drawable.avatar)
                            .into(userAvatar)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                }
            })
        }

        val firstFragment = ChatInterfaceFragment()
        val secondFragment = AddFriendInterfaceFragment()

        setCurrentFragment(firstFragment)

        bottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.chat_navigation -> setCurrentFragment(firstFragment)
                R.id.add_friend_navigation -> setCurrentFragment(secondFragment)
            }
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frame_container,fragment)
            commit()
        }
    }

    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        if (currentId != null) {
            database.reference.child("presence")
                .child(currentId).setValue("Online")
        }
    }


    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        if (currentId != null) {
            database.reference.child("presence")
                .child(currentId).setValue("Offline")
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finishAffinity()"))
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // If the user is not signed in, prevent navigation back to MainActivity
        finishAffinity()
    }


}
