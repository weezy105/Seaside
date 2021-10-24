package com.example.seaside;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DatabaseAccess extends SQLiteOpenHelper {

    public DatabaseAccess(@Nullable Context context) {
        super(context, "seaside-service.db", null, 1);
    }

    // Creates table when needed
    @Override
    public void onCreate(SQLiteDatabase db) {

        String createTable = "CREATE TABLE students (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, email TEXT, password TEXT)";
        db.execSQL(createTable);

        createTable = "CREATE TABLE admins (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, email TEXT, password TEXT)";
        db.execSQL(createTable);

        createTable = "CREATE TABLE events (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, description TEXT, location TEXT, time TEXT, volunteers INTEGER)";
        db.execSQL(createTable);

        createTable = "CREATE TABLE registered (event_id INTEGER, user_id INTEGER, FOREIGN KEY(event_id) REFERENCES events(id), FOREIGN KEY(user_id) REFERENCES students(id))";
        db.execSQL(createTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // for updating app
    }

    // Adds a user to the table, type is either "students" or "admins", returns true or false based on it's success
    public boolean addUser(String name, String email, String password, String type) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("name", name);
        cv.put("email", email);
        cv.put("password", password);

        long success = db.insert(type, null, cv);

        if (success == -1) {
            db.close();
            return false;
        }

        db.close();
        return true;

    }

    // Adds an event to the table, returns true or false based on success
    public boolean addEvent(String title, String description, String location, String time, int volunteers) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("title", title);
        cv.put("description", description);
        cv.put("location", location);
        cv.put("time", time);
        cv.put("volunteers", volunteers);

        long success = db.insert("events", null, cv);

        if (success == -1) {
            db.close();
            return false;
        }

        db.close();
        return true;

    }

    // Checks a user's login, type is either "students" or "admins", returns the user's id or -1 if failed
    public int checkLogin(String email, String password) {

        SQLiteDatabase db = this.getReadableDatabase();

        String[] inputs = {email};

        Cursor correctPassword = null;
        try {
            correctPassword = db.rawQuery("SELECT password, id FROM students WHERE email = ?", inputs, null);
        } catch (Exception e) {
            // L
        }

        if (!correctPassword.moveToFirst()) {
            try {
                correctPassword = db.rawQuery("SELECT password, id FROM admins WHERE email = ?", inputs, null);
            } catch (Exception e) {
                // L
            }
        }
        
        if (correctPassword.moveToFirst()) {
            if (password.equals(correctPassword.getString(0))) {
                db.close();
                correctPassword.close();
                return correctPassword.getInt(1);
            }
        }

        db.close();
        correctPassword.close();
        return -1;

    }

    // Gets the id of each event and returns it
    public java.util.ArrayList<Integer> loadEvents() {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor events;
        try {
            events = db.rawQuery("SELECT id FROM events", null, null);
        } catch (Exception e) {
            events = null;
        }

        java.util.ArrayList<Integer> ids = new java.util.ArrayList<Integer>();
        do {
            ids.add(events.getInt(0));
        } while (events.moveToNext());

        if (ids.isEmpty()) {
            ids.add(-1);
        }

        events.close();
        db.close();
        return ids;

    }

    // Registers a student for an event, returns true or false based on success
    public boolean register(int user_id, int event_id) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("user_id", user_id);
        cv.put("event_id", event_id);

        long success = db.insert("registered", null, cv);

        if (success == -1) {
            db.close();
            return false;
        }

        db.close();
        return true;

    }

    // Returns the title, description, location, time, # of volunteers, and # of registered users, returns a string array of length 1 if failed, else returns string array of length 6
    public String[] eventInfo(int id) {

        SQLiteDatabase db = this.getReadableDatabase();
        String[] arguments = {Integer.toString(id)};

        Cursor information, registered;
        try {
            information = db.rawQuery("SELECT title, description, location, time, volunteers FROM events WHERE id = ?", arguments, null);
            registered = db.rawQuery("SELECT COUNT(*) FROM registered WHERE event_id = ?", arguments, null);
        } catch(Exception e) {
            return arguments;
        }

        if (information.moveToFirst() && registered.moveToFirst()) {
            String[] info = {information.getString(0), information.getString(1), information.getString(2), information.getString(3), information.getString(4), Integer.toString(registered.getInt(0))};
            return info;
        }

        return arguments;

    }
}