package com.example.todomap.profile

data class UserAccount(
    val idToken: String, // firebase user's Uid
    val email: String,
    var userName: String,
    var profileImgUrl: String
): java.io.Serializable
