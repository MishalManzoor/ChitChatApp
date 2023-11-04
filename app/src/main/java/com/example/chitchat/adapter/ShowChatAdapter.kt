package com.example.chitchat.adapter

import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chitchat.R
import com.example.chitchat.models.FriendList
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
class ShowChatAdapter(
    private val mList: List<FriendList>,
    private val onActionClick: OnActionClick
) :
    RecyclerView.Adapter<ShowChatAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name : TextView = itemView.findViewById(R.id.name)
        var chat : LinearLayout = itemView.findViewById(R.id.linearLayout2)
        val profileImage : ImageView = itemView.findViewById(R.id.profile_pic)
    }

    interface OnActionClick {
        fun onClick(
            clickedFriendEmail: String,
            senderName: String, senderEmail: String,
            receiverEmail: String, receiverName: String,
            senderId : String , receiverId : String
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chats_desgin, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val list = mList[position]
        val currentUserId = FirebaseAuth.getInstance().currentUser?.
        uid.toString()

        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.
        email.toString()

        val encodeSenderEmail = encodeEmail(list.sender)
        val currentEncodeEmail = encodeEmail(currentUserEmail)

        val profilePic = if (encodeSenderEmail == currentUserId){
            list.receiverProfilePic
        }else{
            list.senderProfilePic
        }

        if (profilePic.isNotEmpty()) {
            Picasso.get().load(profilePic).into(holder.profileImage)
        } else {
            // Load a default image if the URL is empty
            holder.profileImage.setImageResource(R.drawable.avatar3)
        }

        holder.name.text =
            if (encodeSenderEmail == currentEncodeEmail) {
                list.receiverName
            }
            else{
                list.senderName
            }

        holder.chat.setOnClickListener {
            val friendEmail = if (list.sender == currentUserId) {
                list.receiver
            } else {
                list.sender
            }
            onActionClick.onClick(
                friendEmail,
                list.senderName,
                list.sender,
                list.receiver,
                list.receiverName,
                list.senderId,
                list.receiverId
            )
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    // fun Encode the email address to make it a valid Firebase
    // Database key
    private fun encodeEmail(mail: String): String {
        return Base64.encodeToString(
            mail.toByteArray(),
            Base64.NO_WRAP
        )
    }

}