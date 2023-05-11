package com.example.chitchat.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chitchat.R
import com.example.chitchat.databinding.LeftChatBinding
import com.example.chitchat.databinding.RightChatBinding
import com.example.chitchat.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private var c: Context, private var mList: List<Message>,
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
        val dialogBuilder = AlertDialog.Builder(c)

        if (holder.itemViewType == SENT_BY_USER) {
            val viewHolder = holder as SentViewHolder
            viewHolder.binding.msgChat.text = list.message

            val formatTime = formatTime(date)
            viewHolder.binding.timeSt.text = formatTime

            viewHolder.binding.layout.setOnLongClickListener {
                dialogBuilder.setTitle("Delete")
                    .setMessage("Are you sure you want to delete this message?")
                    // positive button text and action
                    .setPositiveButton("Yes") { _, _ ->

                        val firebaseDatabase = FirebaseDatabase.getInstance()
                        val senderRoom = FirebaseAuth.getInstance().uid + rId

                        firebaseDatabase.reference.child("Chats")
                            .child(senderRoom)
                            .child(list.senderId)
                            .setValue(null)
                    }
                    // negative button text and action
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.cancel()
                    }


                // create dialog box
                val alert = dialogBuilder.create()
                // set title for alert dialog box
                alert.setTitle("AlertDialogExample")
                // show alert dialog
                alert.show()
                return@setOnLongClickListener false
            }
        } else {
            val viewHolder = holder as ReceiverViewHolder

            viewHolder.binding.msgChat.text = list.message

            val formatTime = formatTime(date)
            viewHolder.binding.timeSt.text = formatTime

            viewHolder.binding.layout.setOnLongClickListener {

                dialogBuilder.setTitle("Delete")
                    .setMessage("Are you sure you want to delete this message?")
                    // positive button text and action
                    .setPositiveButton("Yes") { _, _ ->

                        val firebaseDatabase = FirebaseDatabase.getInstance()
                        val senderRoom = FirebaseAuth.getInstance().uid + rId

                        firebaseDatabase.reference.child("Chats")
                            .child(senderRoom)
                            .child(list.senderId)
                            .setValue(null)
                    }
                    // negative button text and action
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.cancel()
                    }


                // create dialog box
                val alert = dialogBuilder.create()
                // set title for alert dialog box
                alert.setTitle("AlertDialogExample")
                // show alert dialog
                alert.show()
                return@setOnLongClickListener false
            }
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