package com.example.chitchat.models

data class Invite (
    var accept : String = "",
    var deny : String = "",
    var senderName : String = "",
    var receiverName : String = "",
    var sender : String = "",
    var receiver : String = "",
    var receiverId : String = "",
    var senderId : String = "",
    var senderProfilePic : String = "",
    var receiverProfilePic : String = ""
    )