package com.example.chitchat.models

data class Message(
    var message: String = "",
    var name: String = "",
    var sender: String = "",
    var id: String = "",
    var receiver: String = "",
    var timeStamp: Long? = 0,
    var messageId: String="",
)