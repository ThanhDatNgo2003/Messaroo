package com.cos30049.chattingapp

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.cos30049.chattingapp.databinding.ActivityChatBinding
import com.cos30049.chattingapp.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class ChatActivity : AppCompatActivity() {

    private var binding: ActivityChatBinding? = null
    private var adapter: MessageAdapter? = null
    private var messages: ArrayList<Message>? = null
    private var senderChat: String? = null
    private var receiverChat: String? = null
    private var database: FirebaseDatabase? = null
    private var storage: FirebaseStorage? = null
    private var dialog: ProgressDialog? = null
    private var senderUid: String? = null
    private var receiverUid: String? = null
    private var typingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)

        setContentView(binding!!.root)
        setSupportActionBar(binding!!.toolbar)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        dialog = ProgressDialog(this@ChatActivity)
        dialog!!.setMessage("Uploading image...")
        dialog!!.setCancelable(false)
        messages = ArrayList()
        val name = intent.getStringExtra("name")
        val imageuser = intent.getStringExtra("image")
        val receiverUid = intent.getStringExtra("uid")
        Log.d("Image URL", imageuser ?: "Image URL is null")
        binding!!.name.text = name
        Glide.with(this@ChatActivity).load(imageuser)
            .placeholder(R.drawable.avatar)
            .into(binding!!.userAvatar)
        binding!!.back.setOnClickListener{
            finish()
        }
        senderUid = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence").child(receiverUid!!)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val status = snapshot.getValue(String::class.java)
                        if (status == "Offline") {
                            binding!!.status.visibility = View.GONE
                        }
                        else{
                            binding!!.status.text = status
                            binding!!.status.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        senderChat = senderUid + receiverUid
        receiverChat = receiverUid + senderUid
        adapter = MessageAdapter(this@ChatActivity, messages, senderChat!!, receiverChat!!)

        binding!!.chatRecycleView.layoutManager = LinearLayoutManager(this@ChatActivity)
        binding!!.chatRecycleView.adapter = adapter
        database!!.reference.child("chats")
            .child(senderChat!!)
            .child("messages")
            .addValueEventListener(object : ValueEventListener{
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages!!.clear()
                        for (snapshot1 in snapshot.children){
                            val message :Message? = snapshot1.getValue(Message::class.java)
                            message!!.messageId = snapshot1.key
                            messages!!.add(message)
                            Log.d("Image URL", message.image.toString())
                        }
                    adapter!!.notifyDataSetChanged()
                    binding!!.chatRecycleView.scrollToPosition(adapter!!.itemCount - 1)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        binding!!.send.setOnClickListener{
            val messageText: String = binding!!.messageBox.text.toString()
            val date = Date()
            val message = Message(messageText, senderUid, date.time)

            binding!!.messageBox.setText("")
            val randomKey = database!!.reference.push().key
            val lastMsgObj = HashMap<String, Any>()
            lastMsgObj["lastMsg"] = message.message!!
            lastMsgObj["lasMsgTime"] = date.time

            database!!.reference.child("chats").child(senderChat!!)
                .updateChildren(lastMsgObj)
            database!!.reference.child("chats").child(receiverChat!!)
                .updateChildren(lastMsgObj)
            database!!.reference.child("chats").child(senderChat!!)
                .child("messages")
                .child(randomKey.toString())
                .setValue(message).addOnCompleteListener {
                    database!!.reference.child("chats")
                        .child(receiverChat!!)
                        .child("messages")
                        .child(randomKey.toString())
                        .setValue(message)
                        .addOnCompleteListener {  }
                }


        }
        binding!!.camera.setOnClickListener{
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 25)
        }

        val coroutineScope = CoroutineScope(Dispatchers.Main)
        binding!!.messageBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed, so leave it empty
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No action needed, so leave it empty
            }

            override fun afterTextChanged(s: Editable?) {
                // Cancel any existing job
                typingJob?.cancel()
                // Start a new job to set the presence back to "Online" after 1 second delay
                typingJob = coroutineScope.launch {
                    delay(1000)
                    senderUid?.let {
                        database!!.reference.child("presence")
                            .child(it)
                            .setValue("Online")
                    }
                }
            }
        })

        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 25){
            if (data != null){
                if (data.data != null){
                    val selectedImage = data.data
                    val calendar = Calendar.getInstance()
                    val reference = storage!!.reference.child("chats")
                        .child(calendar.timeInMillis.toString()+"")
                    dialog!!.show()
                    reference.putFile(selectedImage!!)
                        .addOnCompleteListener {task ->
                            dialog!!.dismiss()
                            if (task.isSuccessful) {
                                reference.downloadUrl.addOnCompleteListener { uri ->
                                    val filePath = uri.result.toString()
                                    val messageText : String = binding!!.messageBox.text.toString()
                                    val date = Date()
                                    val message = Message(messageText, senderUid, date.time)
                                    message.message = "photoqwertyuiopqaz1357924680"
                                    message.image = filePath
                                    binding!!.messageBox.setText("")
                                    val randomKey = database!!.reference.push().key
                                    val lastMsgObj = HashMap<String, Any>()
                                    database!!.reference.child("chats")
                                        .updateChildren(lastMsgObj)
                                    database!!.reference.child("chats")
                                        .child(receiverChat!!)
                                        .updateChildren(lastMsgObj)
                                    database!!.reference.child("chats")
                                        .child(senderChat!!)
                                        .child("messages")
                                        .child(randomKey!!)
                                        .setValue(message).addOnCompleteListener {
                                            database!!.reference.child("chats")
                                                .child(receiverChat!!)
                                                .child("messages")
                                                .child(randomKey)
                                                .setValue(message)
                                                .addOnCompleteListener {  }
                                        }
                                }
                            }
                        }

                }
            }
        }
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