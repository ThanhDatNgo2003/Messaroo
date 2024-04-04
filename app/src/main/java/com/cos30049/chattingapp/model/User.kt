package com.cos30049.chattingapp.model

data class User(
    val uid: String,
    val name: String,
    val email: String,
    val imageUrl: String
) {
    constructor(): this("", "", "", "")
}
