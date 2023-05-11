package com.example.chitchat.models

data class Message(
    var message: String = "",
    var senderId: String = "",
    var timeStamp: Long? = 0,
    var messageId: String="",
)