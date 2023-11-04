package com.example.chitchat.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.chitchat.R
import com.example.chitchat.chatDetails.ChatDetailsActivity
import com.example.chitchat.models.Users
import com.squareup.picasso.Picasso

class UserAdapter(private var mList: List<Users>, var context: Context) :
    RecyclerView.Adapter<UserAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var image: ImageView = itemView.findViewById(R.id.profile_pic)
        var name: TextView = itemView.findViewById(R.id.name)
        var layout: LinearLayout = itemView.findViewById(R.id.linearLayout2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {

        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.chats_desgin, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val list = mList[position]

        holder.name.text = list.name

        if (list.profilePic.trim().isEmpty()) {
            holder.image.setImageResource(R.drawable.avatar3)
        } else {
            Picasso.get().load(list.profilePic)
                .placeholder(R.drawable.avatar3).into(holder.image)
        }

        holder.layout.setOnClickListener {
            val intent = Intent(context, ChatDetailsActivity::class.java)
         //   intent.putExtra("sender", list.sender)
            intent.putExtra("name", list.name)
            intent.putExtra("pic", list.profilePic)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }
}