package com.example.chitchat.adapter


import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chitchat.R
import com.example.chitchat.models.FriendList
import com.google.firebase.auth.FirebaseAuth

class FriendListAdapter (
    private val mList: List<FriendList>
) :
    RecyclerView.Adapter<FriendListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name2)
        val profileImage : ImageView = itemView.findViewById(R.id.image2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend_list_design, parent, false)
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

        holder.profileImage.setImageResource(R.drawable.e)


        holder.name.text =
            if (encodeSenderEmail == currentEncodeEmail) {
                list.receiverName
            }
            else{
                list.senderName
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