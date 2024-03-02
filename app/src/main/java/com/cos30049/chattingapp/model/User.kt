package com.cos30049.chattingapp.model

class User {
    private var uid : String? = null
    var name : String? = null
    private var email : String? = null
    private var profileImage: String? = null
    constructor(){}
    constructor(
        uid:String?,
        name: String?,
        email: String?,
        profileImage: String?,
    ){
        this.uid = uid
        this.name = name
        this.email = email
        this.profileImage = profileImage
    }
}