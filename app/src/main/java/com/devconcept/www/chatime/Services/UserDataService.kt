package com.devconcept.www.chatime.Services

import android.graphics.Color
import java.util.*

/**
 * Created by harry on 24/11/2017.
 */
object UserDataService {
    var id = ""
    var avatarColor = ""
    var avatarName = ""
    var email = ""
    var name = ""

    fun returnAvatarColor(components: String): Int {

        //[0.403921568627451, 0.35294117647058826, 0.1803921568627451, 1]
        val strippedColor = components
                .replace("[", "")
                .replace("]", "")
                .replace(",", "")

        var r = 0
        var g = 0
        var b = 0

        val scanner = Scanner(strippedColor)
        if (scanner.hasNext()) {
            r = ((scanner.nextDouble()) * 255).toInt()
            g = ((scanner.nextDouble()) * 255).toInt()
            b = ((scanner.nextDouble()) * 255).toInt()
        }

        return Color.rgb(r,g,b)
    }

    fun logout() {
        id = ""
        avatarColor = ""
        avatarName = ""
        email = ""
        name = ""
        AuthService.authToken = ""
        AuthService.userEmail = ""
        AuthService.isLoggedIn = false
    }
}