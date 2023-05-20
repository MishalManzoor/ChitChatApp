package com.example.chitchat.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chitchat.R
import com.example.chitchat.databinding.LeftChatBinding
import com.example.chitchat.databinding.RightChatBinding
import com.example.chitchat.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private var c: Context,
    private var mList: List<Message>,
    private var rId: String
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val SENT_BY_USER = 0
        const val RECEIVE_BY_User = 1
    }

    inner class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding = RightChatBinding.bind(itemView)
    }

    inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding = LeftChatBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == SENT_BY_USER) {

            SentViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.right_chat, parent, false)
            )
        } else {
            ReceiverViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.left_chat, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val list = mList[position]
        val date = Date(list.timeStamp!!)

        if (holder.itemViewType == SENT_BY_USER) {
            val viewHolder = holder as SentViewHolder
            viewHolder.binding.msgChat.text = list.message

            val formatTime = formatTime(date)
            viewHolder.binding.timeSt.text = formatTime

            // to delete message
            viewHolder.binding.layout.setOnLongClickListener {

                val dialogBuilder = AlertDialog.Builder(c)
                dialogBuilder.setMessage("Are you sure to delete this message?")
                    .setPositiveButton("Delete") { _, _ ->

                        val ref = FirebaseDatabase.getInstance().reference
                        val applesQuery: Query =
                            ref.child("Chats")
                                .child(FirebaseAuth.getInstance().uid.toString() + rId)
                                .child("message")
                                .orderByChild("message")
                                .equalTo(viewHolder.binding.msgChat.text.toString())
                                // to retrieve the most recent items in a database
                                // and delete this message
                                .limitToLast(1)

                        applesQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (appleSnapshot in dataSnapshot.children) {
                                    appleSnapshot.ref.setValue(null)
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.e("TAG", "onCancelled", databaseError.toException())
                            }
                        })
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }

                val alert = dialogBuilder.create()
                alert.setTitle("Test")
                alert.show()

                return@setOnLongClickListener true
            }

        } else {
            val viewHolder = holder as ReceiverViewHolder

            viewHolder.binding.msgChat.text = list.message

            val formatTime = formatTime(date)
            viewHolder.binding.timeSt.text = formatTime
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (FirebaseAuth.getInstance().uid == mList[position].senderId) {
            SENT_BY_USER
        } else {
            RECEIVE_BY_User
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun formatTime(date: Date): String {
        val formatTime = SimpleDateFormat("hh:mm a")
        return formatTime.format(date)
    }
}