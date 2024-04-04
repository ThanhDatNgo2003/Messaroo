package com.cos30049.chattingapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cos30049.chattingapp.databinding.DeleteMsgBinding
import com.cos30049.chattingapp.databinding.ReceiveMsgBinding
import com.cos30049.chattingapp.databinding.SendMsgBinding
import com.cos30049.chattingapp.model.Message
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MessageAdapter(
    var context: Context,
    private val messages: ArrayList<Message>?,
    private val senderChat: String,
    private val receiverChat: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val send = 1
    private val receive = 2


    inner class SendMsgHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var binding:SendMsgBinding = SendMsgBinding.bind(itemView)
    }

    inner class ReceiveMsgHolder(itemView:View): RecyclerView.ViewHolder(itemView){
        var binding:ReceiveMsgBinding = ReceiveMsgBinding.bind(itemView)
    }

//    init {
//        if (messages != null) {
//            this.messages = messages
//        }
//    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == send){
            val view = LayoutInflater.from(context).inflate(R.layout.send_msg, parent, false)
            SendMsgHolder(view)
        }
        else {
            val view = LayoutInflater.from(context).inflate(R.layout.receive_msg, parent, false)
            ReceiveMsgHolder(view)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    override fun getItemViewType(position: Int): Int {
        val messages = messages?.get(position)
            return if (FirebaseAuth.getInstance().uid == messages?.senderId){
                send
            }
            else {
                receive
            }
    }

    override fun getItemCount(): Int {
        return messages?.size ?: 0
    }

    @SuppressLint("CheckResult", "InflateParams")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages?.get(position)
//        Log.d("Message Object", message.toString())
//        Log.d("Message Text", message?.message ?: "Message text is null")
//        Log.d("Image URL", message?.image ?: "Image URL is null")
        if (holder is SendMsgHolder) {
            val viewHolder = holder as SendMsgHolder
            if (message!!.message.equals("photoqwertyuiopqaz1357924680")) {
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.message.visibility = View.GONE
                viewHolder.binding.messageLinear.visibility = View.GONE
                Glide.with(context)
                    .load(message.image)
                    .placeholder(R.drawable.placeholder)
                    .into(viewHolder.binding.image)
            } else {
                viewHolder.binding.image.visibility = View.GONE
                viewHolder.binding.message.visibility = View.VISIBLE
                viewHolder.binding.messageLinear.visibility = View.VISIBLE
                viewHolder.binding.message.text = message.message
            }
            viewHolder.itemView.setOnLongClickListener {
                val view = LayoutInflater.from(context).inflate(R.layout.delete_msg, null)
                val binding: DeleteMsgBinding = DeleteMsgBinding.bind(view)
                val dialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()
                binding.rmEvery.setOnClickListener {
                    message.message = "You unsent a message"
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderChat)
                            .child("messages")
                            .child(it1).setValue(message)
                    }
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(receiverChat)
                            .child("messages")
                            .child(it1).setValue(message)
                    }
                    dialog.dismiss()
                }
                binding.rmOnly.setOnClickListener {
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderChat)
                            .child("messages")
                            .child(it1).setValue(null)
                    }
                    dialog.dismiss()
                }
                binding.rmCancel.setOnClickListener { dialog.dismiss() }
                dialog.show()
                true
            }
        } else {
            val viewHolder = holder as ReceiveMsgHolder
            if (message!!.message.equals("photoqwertyuiopqaz1357924680")) {
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.message.visibility = View.GONE
                viewHolder.binding.messageLinear.visibility = View.GONE
                Glide.with(context)
                    .load(message.image)
                    .placeholder(R.drawable.placeholder)
                    .into(viewHolder.binding.image)
            } else {
                viewHolder.binding.image.visibility = View.GONE
                viewHolder.binding.message.visibility = View.VISIBLE
                viewHolder.binding.messageLinear.visibility = View.VISIBLE
                viewHolder.binding.message.text = message.message
            }
            viewHolder.itemView.setOnLongClickListener() {
                val view = LayoutInflater.from(context).inflate(R.layout.delete_msg, null)
                val binding: DeleteMsgBinding = DeleteMsgBinding.bind(view)
                val dialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()
                binding.rmEvery.visibility = View.GONE
                binding.rmOnly.setOnClickListener {
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderChat)
                            .child("messages")
                            .child(it1).setValue(null)
                    }
                    dialog.dismiss()
                }
                binding.rmCancel.setOnClickListener { dialog.dismiss() }
                dialog.show()
                true
            }
        }
    }

}
