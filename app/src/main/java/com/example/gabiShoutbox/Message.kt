package com.example.gabiShoutbox

import java.io.Serializable


class Message : Serializable {
    var content: String? = null
    var login: String? = null
    var date: String? = null
    var id: String? = null
}