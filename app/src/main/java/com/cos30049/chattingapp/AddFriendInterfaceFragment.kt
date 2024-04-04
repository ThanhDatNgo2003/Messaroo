package com.cos30049.chattingapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.cos30049.chattingapp.databinding.FragmentAddFriendInterfaceBinding
import com.cos30049.chattingapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class AddFriendInterfaceFragment : Fragment() {
    private lateinit var binding: FragmentAddFriendInterfaceBinding
    private var database : FirebaseDatabase? = null
    private var users: ArrayList<User>? = null
    private var usersAdapter: UserAdapter? = null
    private var user: User? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddFriendInterfaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance()
        users = ArrayList<User>()
        usersAdapter = UserAdapter(requireContext(), users!!)
        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.addFriendRecycleView.layoutManager = layoutManager
        binding.addFriendRecycleView.adapter = usersAdapter

        database!!.reference.child("users")
            .child(FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(User::class.java)
                    users!!.add(user!!)
                    usersAdapter!!.notifyItemInserted(users!!.size - 1)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        database!!.reference.child("users").addValueEventListener(object :ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                users!!.clear()
                for(snapshot1 in snapshot.children){
                    val user:User? = snapshot1.getValue(User::class.java)
                    if (user!!.uid != FirebaseAuth.getInstance().uid) users!!.add(user)
                }
                usersAdapter!!.notifyDataSetChanged()
                for (user in users!!) {
                    Log.d("AddFriendInterfaceFragment", "User: $user")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence")
            .child(currentId!!).setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence")
            .child(currentId!!).setValue("Offline")
    }

}