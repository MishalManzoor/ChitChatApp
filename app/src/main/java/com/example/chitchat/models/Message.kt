package com.example.chitchat.models

data class Message(
    var message: String = "",
    var senderName: String = "",
    var receiverName: String = "",
    var sender: String = "",
    var id: String = "",
    var receiver: String = "",
    var timeStamp: Long? = 0
)