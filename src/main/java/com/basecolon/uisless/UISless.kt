package com.basecolon.uisless

import com.basecolon.uisless.database.Database
import com.basecolon.uisless.model.ConfigModel
import com.basecolon.uisless.scraper.CourseScraper
import com.google.gson.Gson
import java.io.InputStream
import java.util.*

object UISless {
    private val CONFIG_FILE_NAME = "sensitive_config.json"

    fun main(args: Array<String>) {
        //Load the config file into memory
        val classloader = Thread.currentThread().contextClassLoader
        val inputStream = classloader.getResourceAsStream(CONFIG_FILE_NAME)

        //Convert config into a Java class
        val configString = convertStreamToString(inputStream)
        val config = Gson().fromJson<ConfigModel>(configString, ConfigModel::class.java!!)

        val database = Database.getInstance(config.database, config.term)

        val scraper = CourseScraper(config.user, config.term)
        val courses = scraper.classRecords

        //Insert every course model into the DB
        courses.forEach({ database.insert(it) })
    }

    private fun convertStreamToString(`is`: InputStream): String {
        val s = Scanner(`is`).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }
}
