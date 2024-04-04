package com.cos30049.chattingapp.model

class Message {
    var messageId: String? = null
    var message: String? = null
    var senderId: String? = null
    var image: String? = null
    private var timeStamp: Long = 0

    constructor() {}

    constructor(
        messageId: String?,
        message: String?,
        senderId: String?,
        image: String?,
        timeStamp: Long
    ) {
        this.messageId = messageId
        this.message = message
        this.senderId = senderId
        this.image = image
        this.timeStamp = timeStamp
    }

    constructor(
        message: String?,
        senderId: String?,
        timeStamp: Long
    ) {
        this.message = message
        this.senderId = senderId
        this.timeStamp = timeStamp
    }
}
