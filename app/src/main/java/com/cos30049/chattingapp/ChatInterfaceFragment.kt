package com.cos30049.chattingapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cos30049.chattingapp.databinding.FragmentChatInterfaceBinding
import com.cos30049.chattingapp.model.ChatUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatInterfaceFragment : Fragment() {
    private lateinit var binding: FragmentChatInterfaceBinding
    private lateinit var database: FirebaseDatabase
    private val users: ArrayList<ChatUser> = ArrayList()
    private lateinit var chatAdapter: ChatAdapter
    private var currentUserUid: String? = null
    private lateinit var currentUserListener: ValueEventListener
    private lateinit var otherUsersListener: ValueEventListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatInterfaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance()
        chatAdapter = ChatAdapter(requireContext(), users)

        val layoutManager = LinearLayoutManager(requireContext())
        binding.chatRecycleView.layoutManager = layoutManager
        binding.chatRecycleView.adapter = chatAdapter

        currentUserUid = FirebaseAuth.getInstance().uid
        currentUserListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(ChatUser::class.java)
                user?.let {
                    users.add(it)
                    chatAdapter.notifyItemInserted(users.size - 1)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        }

        otherUsersListener = object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                users.clear()
                for (snapshot1 in snapshot.children) {
                    val user = snapshot1.getValue(ChatUser::class.java)
                    user?.let {
                        if (it.uid != currentUserUid) {
                            users.add(it)
                        }
                    }
                }
                chatAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        }

        currentUserUid?.let { uid ->
            database.reference.child("users").child(uid).addListenerForSingleValueEvent(currentUserListener)
            database.reference.child("users").addListenerForSingleValueEvent(otherUsersListener)
        }
    }

    override fun onResume() {
        super.onResume()
        currentUserUid?.let { uid ->
            database.reference.child("presence").child(uid).setValue("Online")
        }
    }

    override fun onPause() {
        super.onPause()
        currentUserUid?.let { uid ->
            database.reference.child("presence").child(uid).setValue("Offline")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentUserUid?.let { uid ->
            database.reference.child("users").child(uid).removeEventListener(currentUserListener)
            database.reference.child("users").removeEventListener(otherUsersListener)
        }
    }
}
