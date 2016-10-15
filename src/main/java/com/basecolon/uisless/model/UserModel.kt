package com.basecolon.uisless.model

import com.google.gson.annotations.SerializedName

data class UserModel(
    @SerializedName("x_number")
    var xNumber: String? = null,

    var pin: String? = null
)
