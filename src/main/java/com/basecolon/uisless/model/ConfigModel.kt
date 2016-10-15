package com.basecolon.uisless.model

data class ConfigModel(
    var database: DatabaseModel? = null,
    var user: UserModel? = null,

    var term: String? = null
)
