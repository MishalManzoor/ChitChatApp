package com.example.chitchat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chitchat.R
import com.example.chitchat.models.Invite
import com.google.firebase.auth.FirebaseAuth

class InviteAdapter (private var mList : List<Invite>,
                    private val onActionClick: OnActionClick,
)
    : RecyclerView.Adapter<InviteAdapter.ViewHolder>() {

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val name : TextView = itemView.findViewById(R.id.name2)
        val accept : ImageView = itemView.findViewById(R.id.accept) // here error
        val deny : ImageView = itemView.findViewById(R.id.deny)
        val profileImage : ImageView = itemView.findViewById(R.id.image24)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
    : ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.invite_friend_design, parent, false)
        return ViewHolder(view) // error
    }

    override fun onBindViewHolder(holder: ViewHolder,
                                  position: Int) {
        val list = mList[position]
        val currentUserId = FirebaseAuth.getInstance().currentUser?.
        uid.toString()

        holder.name.text = list.senderName

        holder.profileImage.setImageResource(R.drawable.e)


        holder.accept.setOnClickListener {
            onActionClick.onAcceptClick(list.senderName,
            list.sender,
            list.receiver,
            list.receiverName,
            position.toLong(),
            list.senderId,
            list.receiverId)
        }

        holder.deny.setOnClickListener {
            onActionClick.onDenyClick(list.senderName,
            list.sender,
            list.receiver,
            position.toLong(),
            list.senderId)
        }

    }

    override fun getItemCount(): Int {
        return mList.size
    }

    interface OnActionClick{
        fun onAcceptClick(senderName : String ,
                          sender : String ,
                          receiver : String,
                          receiverName : String ,
                          position: Long,
                          senderId : String,
                          receiverId: String)

        fun onDenyClick(senderName : String ,
                        sender : String ,
                        receiver : String,
                        position: Long,
                        senderId : String)
    }
}
