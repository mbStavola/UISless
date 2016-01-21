package com.basecolon.uisless.model;

import com.google.gson.annotations.SerializedName;

public class DatabaseModel {
    @SerializedName("db_host")
    public String dbHost;

    @SerializedName("db_name")
    public String dbName;

    @SerializedName("db_username")
    public String dbUsername;

    @SerializedName("db_password")
    public String dbPassword;
}
