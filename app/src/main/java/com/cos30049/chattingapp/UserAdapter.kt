package com.cos30049.chattingapp

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cos30049.chattingapp.databinding.ItemUserBinding
import com.cos30049.chattingapp.model.User

class UserAdapter(var context: Context, private var userList: ArrayList<User>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding: ItemUserBinding = ItemUserBinding.bind(itemView)

        fun bind(user: User) {
            binding.userNameFriend.text = user.name
            Glide.with(context).load(user.imageUrl)
                .placeholder(R.drawable.avatar)
                .into(binding.userAvatar)
            Log.d("UserAdapter", "User: $user")

            binding.chatButton.setOnClickListener {
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("name", user.name)
                intent.putExtra("image", user.imageUrl)
                intent.putExtra("uid", user.uid)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(v)
    }

    override fun getItemCount(): Int = userList.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(userList[position])
    }
}