package com.basecolon.uisless.model

import com.google.gson.annotations.SerializedName

data class DatabaseModel(
    @SerializedName("db_host")
    var dbHost: String? = null,
    @SerializedName("db_name")
    var dbName: String? = null,

    @SerializedName("db_username")
    var dbUsername: String? = null,
    @SerializedName("db_password")
    var dbPassword: String? = null
)
