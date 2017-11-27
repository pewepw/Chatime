package com.devconcept.www.chatime.Controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.devconcept.www.chatime.Adapter.MessageAdapter
import com.devconcept.www.chatime.Model.Channel
import com.devconcept.www.chatime.Model.Message
import com.devconcept.www.chatime.R
import com.devconcept.www.chatime.Services.AuthService
import com.devconcept.www.chatime.Services.MessageService
import com.devconcept.www.chatime.Services.UserDataService
import com.devconcept.www.chatime.Utilities.BROADCAST_USER_DATA_CHANGE
import com.devconcept.www.chatime.Utilities.SOCKET_URL
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {

    val socket = IO.socket(SOCKET_URL)
    lateinit var channelAdapter: ArrayAdapter<Channel>
    lateinit var messageAdapter: MessageAdapter
    var selectedChannel: Channel? = null

    private fun setupAdapter() {
        channelAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, MessageService.channels)
        channel_list.adapter = channelAdapter

        messageAdapter = MessageAdapter(this, MessageService.messages)
        messageListView.adapter = messageAdapter

        val layoutManager = LinearLayoutManager(this)
        messageListView.layoutManager = layoutManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver, IntentFilter(BROADCAST_USER_DATA_CHANGE))

        socket.connect()
        socket.on("channelCreated", onNewChannel)
        socket.on("messageCreated", onNewMessage)
        setupAdapter()

        usernameNavHeader.findViewById<TextView>(R.id.usernameNavHeader)
        userEmailNavHeader.findViewById<TextView>(R.id.userEmailNavHeader)
        loginButtonNavHeader.findViewById<Button>(R.id.loginButtonNavHeader)
        userImageNavHeader.findViewById<ImageView>(R.id.userImageNavHeader)

        channel_list.setOnItemClickListener { _, _, i, _ ->
            selectedChannel = MessageService.channels[i]
            drawer_layout.closeDrawer(GravityCompat.START)
            updateWithChannel()
        }

        if (App.prefs.isLoggedIn) {
            AuthService.findUserByEmail(this) {}
        }
    }

    override fun onDestroy() {
        socket.disconnect()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userDataChangeReceiver)
        super.onDestroy()
    }

    private val userDataChangeReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context, p1: Intent?) {
            if (App.prefs.isLoggedIn) {

                usernameNavHeader.text = UserDataService.name
                userEmailNavHeader.text = UserDataService.email

                val resouceId = resources.getIdentifier(UserDataService.avatarName, "drawable", packageName)
                userImageNavHeader.setImageResource(resouceId)
                userImageNavHeader.setBackgroundColor(UserDataService.returnAvatarColor(UserDataService.avatarColor))

                loginButtonNavHeader.text = "LOGOUT"

                MessageService.getChannels(context) { complete ->
                    if (complete) {
                        if (MessageService.channels.count() > 0) {
                            selectedChannel = MessageService.channels[0]
                            channelAdapter.notifyDataSetChanged()
                            updateWithChannel()
                        }
                    }
                }
            }
        }
    }

    fun updateWithChannel() {
        mainChannelName.text = "#${selectedChannel?.name}"

        if (selectedChannel != null) {
            MessageService.getMessages(this, selectedChannel!!.id) { complete ->
                if (complete) {
                    messageAdapter.notifyDataSetChanged()

                    if (messageAdapter.itemCount > 0) {
                        messageListView.smoothScrollToPosition(messageAdapter.itemCount - 1)
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun loginButtonNavClicked(view: View) {
        if (App.prefs.isLoggedIn) {
            channelAdapter.notifyDataSetChanged()
            messageAdapter.notifyDataSetChanged()

            UserDataService.logout()
            usernameNavHeader.text = ""
            userEmailNavHeader.text = ""
            userImageNavHeader.setImageResource(R.drawable.profiledefault)
            userImageNavHeader.setBackgroundColor(Color.TRANSPARENT)
            loginButtonNavHeader.text = "LOGIN"
            mainChannelName.text = "Please log in"
        } else {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }
    }

    fun addChannelClicked(view: View) {
        if (App.prefs.isLoggedIn) {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_channel_dialog, null)
            builder.setView(dialogView)
                    .setPositiveButton("Add") { dialogInterface, i ->
                        val nameTextField = dialogView.findViewById<EditText>(R.id.addChannelNameText)
                        val descTextField = dialogView.findViewById<EditText>(R.id.addChannelDescriptionText)
                        val channelName = nameTextField.text.toString()
                        val channelDesc = descTextField.text.toString()

                        socket.emit("newChannel", channelName, channelDesc)

                    }
                    .setNegativeButton("Cancel") { dialogInterface, i ->

                    }
                    .show()
        }
    }

    private val onNewChannel = Emitter.Listener { args ->
        if (App.prefs.isLoggedIn) {
            runOnUiThread {
                val channelName = args[0] as String
                val channelDescription = args[1] as String
                val channelId = args[2] as String

                val newChannel = Channel(channelName, channelDescription, channelId)
                MessageService.channels.add(newChannel)
                channelAdapter.notifyDataSetChanged()
            }
        }
    }

    private val onNewMessage = Emitter.Listener { args ->
        if (App.prefs.isLoggedIn) {
            runOnUiThread {
                val channelId = args[2] as String

                if (channelId == selectedChannel?.id) {
                    val messageBody = args[0] as String
                    val username = args[3] as String
                    val userAvatar = args[4] as String
                    val userAvatarColor = args[5] as String
                    val id = args[6] as String
                    val timeStamp = args[7] as String

                    val newMessage = Message(messageBody, username, channelId, userAvatar, userAvatarColor, id, timeStamp)
                    MessageService.messages.add(newMessage)
                    messageAdapter.notifyDataSetChanged()
                    messageListView.smoothScrollToPosition(messageAdapter.itemCount - 1)
                }
            }
        }
    }

    fun sendMessageButtonClicked(view: View) {
        if (App.prefs.isLoggedIn && messageTextView.text.isNotEmpty() && selectedChannel != null) {
            val userId = UserDataService.id
            val channelId = selectedChannel!!.id
            socket.emit("newMessage", messageTextView.text.toString(), userId, channelId, UserDataService.name, UserDataService.avatarName, UserDataService.avatarColor)
            messageTextView.text.clear()
            //hideKeyboard()
        }

    }

    fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }

}
