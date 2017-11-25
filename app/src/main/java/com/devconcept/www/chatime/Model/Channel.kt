package com.devconcept.www.chatime.Model

/**
 * Created by harry on 26/11/2017.
 */
class Channel(val name: String, val description: String, val id: String) {
    override fun toString(): String {
        return "#$name"
    }
}