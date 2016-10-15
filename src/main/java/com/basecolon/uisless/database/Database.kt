package com.basecolon.uisless.database

import com.basecolon.uisless.model.CourseModel
import com.basecolon.uisless.model.DatabaseModel
import org.postgresql.Driver

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException

class Database private constructor(private val config: DatabaseModel, term: String) {
    private val term: String

    init {

        this.term = term.replace(" ", "_").toLowerCase()
    }

    fun insert(course: CourseModel) {
        val sqlString = "INSERT INTO ? VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

        try {
            connection!!.use { conn ->
                conn!!.prepareStatement(sqlString).use({ preparedStatement ->
                    preparedStatement.setString(1, term)

                    preparedStatement.setString(2, course.open)

                    preparedStatement.setString(3, course.time)
                    preparedStatement.setString(4, course.days)
                    //preparedStatement.setString(4, course.location);
                    preparedStatement.setString(5, "PLACE_HOLDER")
                    preparedStatement.setString(6, course.date)

                    preparedStatement.setString(7, course.instructor)

                    preparedStatement.setString(8, course.crn)
                    preparedStatement.setString(9, course.subject)

                    preparedStatement.setString(10, course.courseNumber)
                    preparedStatement.setString(11, course.section)

                    preparedStatement.setString(12, course.campus)

                    preparedStatement.setString(13, course.credits)

                    preparedStatement.setString(14, course.title)

                    preparedStatement.setString(15, course.remainingSeats)
                    preparedStatement.setString(16, course.waitListActual)
                    preparedStatement.setString(17, course.waitListRemaining)

                    preparedStatement.execute()
                })
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

    }

    /**
     * Returns a properly configured [Connection] object for Restline.

     * @return Connection to UISless' database
     */
    private val connection: Connection?
        get() {
            try {
                DriverManager.registerDriver(Driver())

                val sslString = "?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory"

                val dbUrl = String.format("jdbc:postgresql://%s/%s%s",
                        config.dbHost,
                        config.dbName,
                        sslString)

                return DriverManager.getConnection(dbUrl, config.dbUsername, config.dbPassword)
            } catch (e: SQLException) {
                e.printStackTrace()
            }

            return null
        }

    companion object {
        private var INSTANCE: Database? = null

        fun getInstance(databaseModel: DatabaseModel, term: String): Database {
            if (INSTANCE == null) {
                INSTANCE = Database(databaseModel, term)
            }
            return INSTANCE
        }
    }
}
