package com.example.todomap.user

data class UserAccount(
    val idToken: String, // firebase user's Uid
    val email: String,
    var userName: String,
    var infor: String,
    var profileImgUrl: String
) {
    constructor() : this("", "","", "","")
}

//    : java.io.Serializable
