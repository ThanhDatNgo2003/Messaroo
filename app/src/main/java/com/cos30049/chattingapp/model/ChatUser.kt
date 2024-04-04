package com.cos30049.chattingapp.model

data class ChatUser(
    val uid: String,
    val name: String,
    val imageUrl: String,
    val lastmsg: String
) {
    constructor(): this("", "", "", "")
}
