package com.example.todo_app_xuho_it.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(@Nullable Context context) {
        super(context, "DBTask", null, 3); //database
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sSQLTasks = "CREATE TABLE TASKS(ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "TITLE TEXT, CONTENT TEXT, DATE TEXT, TYPE TEXT, IS_COMPLETED INTEGER DEFAULT 0)";
        db.execSQL(sSQLTasks);
        String sSQLInsert = "INSERT INTO TASKS (ID, TITLE, CONTENT, DATE, TYPE, IS_COMPLETED) VALUES\n" +
                "('1', 'Buy groceries', 'Milk, eggs, bread, and fruits', '2025-06-15', 'MEDIUM', 0),\n" +
                "('2', 'Do homework', 'Math exercises chapter 4', '2025-06-20', 'HIGH', 0),\n" +
                "('3', 'Read a book', 'Finish Atomic Habits', '2025-06-17', 'LOW', 0),\n" +
                "('4', 'Exercise', '30 minutes of cardio', '2025-06-16', 'MEDIUM', 0);";
        db.execSQL(sSQLInsert);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion < 2){
            db.execSQL("ALTER TABLE TASKS ADD COLUMN IS_COMPLETED INTEGER DEFAULT 0");
        }
    }
}
