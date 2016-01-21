package com.basecolon.uisless.database;

import com.basecolon.uisless.model.CourseModel;
import com.basecolon.uisless.model.DatabaseModel;
import org.postgresql.Driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Database {
    private static Database INSTANCE;

    private DatabaseModel config;
    private String term;

    private Database(DatabaseModel config, String term) {
        this.config = config;

        this.term = term.replace(" ", "_").toLowerCase();
    }

    public void insert(CourseModel course) {
        String sqlString = "INSERT INTO ? VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try(Connection conn = getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(sqlString)
        ) {
            preparedStatement.setString(1, term);

            preparedStatement.setString(2, course.open);

            preparedStatement.setString(3, course.time);
            preparedStatement.setString(4, course.days);
            //preparedStatement.setString(4, course.location);
            preparedStatement.setString(5, "PLACE_HOLDER");
            preparedStatement.setString(6, course.date);

            preparedStatement.setString(7, course.instructor);

            preparedStatement.setString(8, course.crn);
            preparedStatement.setString(9, course.subject);

            preparedStatement.setString(10, course.courseNumber);
            preparedStatement.setString(11, course.section);

            preparedStatement.setString(12, course.campus);

            preparedStatement.setString(13, course.credits);

            preparedStatement.setString(14, course.title);

            preparedStatement.setString(15, course.remainingSeats);
            preparedStatement.setString(16, course.waitListActual);
            preparedStatement.setString(17, course.waitListRemaining);

            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a properly configured {@link Connection} object for Restline.
     * @return Connection to UISless' database
     */
    private Connection getConnection() {
        try {
            DriverManager.registerDriver(new Driver());

            final String sslString = "?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";

            String dbUrl = String.format("jdbc:postgresql://%s/%s%s",
                    config.dbHost,
                    config.dbName,
                    sslString
            );

            return DriverManager.getConnection(dbUrl, config.dbUsername, config.dbPassword);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Database getInstance(DatabaseModel databaseModel, String term) {
        if(INSTANCE == null) {
            INSTANCE = new Database(databaseModel, term);
        }
        return INSTANCE;
    }
}
