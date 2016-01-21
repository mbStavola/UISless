package com.basecolon.uisless;

import com.basecolon.uisless.database.Database;
import com.basecolon.uisless.model.ConfigModel;
import com.basecolon.uisless.model.CourseModel;
import com.basecolon.uisless.scraper.CourseScraper;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

public class UISless {
    private static final String CONFIG_FILE_NAME = "sensitive_config.json";

    public static void main(String[] args) throws FileNotFoundException {
        //Load the config file into memory
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        final InputStream inputStream = classloader.getResourceAsStream(CONFIG_FILE_NAME);

        //Convert config into a Java class
        final String configString = convertStreamToString(inputStream);
        final ConfigModel config =  new Gson().fromJson(configString, ConfigModel.class);

        final Database database = Database.getInstance(config.database, config.term);

        final CourseScraper scraper = new CourseScraper(config.user, config.term);
        final List<CourseModel> courses = scraper.getClassRecords();

        //Insert every course model into the DB
        courses.forEach(database::insert);
    }

    public static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
