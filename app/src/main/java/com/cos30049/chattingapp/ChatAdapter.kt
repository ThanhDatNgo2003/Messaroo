package com.cos30049.chattingapp

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cos30049.chattingapp.databinding.ItemChatUserBinding
import com.cos30049.chattingapp.model.ChatUser

class ChatAdapter(var context: Context, private var userList: ArrayList<ChatUser>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding: ItemChatUserBinding = ItemChatUserBinding.bind(itemView)

        fun bind(user: ChatUser) {
            binding.userNameFriend.text = user.name
            Glide.with(context).load(user.imageUrl)
                .placeholder(R.drawable.avatar)
                .into(binding.userAvatar)
            Log.d("ChatAdapter", "ChatUser: $user")
//            binding.chatButton.setOnClickListener {
//                val intent = Intent(context, ChatActivity::class.java)
//                intent.putExtra("userId", user.uid) // Pass user ID to ChatActivity
//                context.startActivity(intent)
//            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.item_chat_user, parent, false)
        return ChatViewHolder(v)
    }

    override fun getItemCount(): Int = userList.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(userList[position])
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("name", user.name)
            intent.putExtra("image", user.imageUrl)
            intent.putExtra("uid", user.uid)
            context.startActivity(intent) // Start the ChatActivity when the item is clicked
        }
    }

}