package com.devconcept.www.chatime.Adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.devconcept.www.chatime.Model.Message
import com.devconcept.www.chatime.R
import com.devconcept.www.chatime.Services.UserDataService

/**
 * Created by harry on 27/11/2017.
 */
class MessageAdapter(val context: Context, val messages: ArrayList<Message>): RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return messages.count()
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.bindMessage(context, messages[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.message_list_view, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val userImage = itemView?.findViewById<ImageView>(R.id.messageUserImage)
        val timeStamp = itemView?.findViewById<TextView>(R.id.timeStampLabel)
        val username = itemView?.findViewById<TextView>(R.id.messageUsernameLabel)
        val messageBody = itemView?.findViewById<TextView>(R.id.messageBodyLabel)

        fun bindMessage(context: Context, messages: Message) {
            val resourceId = context.resources.getIdentifier(messages.userAvatar, "drawable", context.packageName)
            userImage?.setImageResource(resourceId)
            userImage?.setBackgroundColor(UserDataService.returnAvatarColor(messages.userAvatarColor))
            username?.text = messages.username
            timeStamp?.text = messages.timeStamp
            messageBody?.text = messages.message
        }
    }

}