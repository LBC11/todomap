package com.example.todomap.profile

data class UserAccount(
    var idToken: String? = null, // firebase user's Uid
    var emailId: String? = null,
    var userName: String? = null,
    var profileImgUrl: String? = null,
): java.io.Serializable
