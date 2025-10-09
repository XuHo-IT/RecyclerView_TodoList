package com.example.todo_app_xuho_it.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.todo_app_xuho_it.Database.DBHelper;
import com.example.todo_app_xuho_it.Model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskDAO {
    private DBHelper dbhelper;
    private SQLiteDatabase database;

    public TaskDAO(Context context) {
        dbhelper = new DBHelper(context);
        database = dbhelper.getWritableDatabase();
    }
    public long addTask(Task task)
    {
        ContentValues values = new ContentValues();
        values.put("TITLE",task.getTitle());
        values.put("CONTENT",task.getContent());
        values.put("DATE",task.getDate());
        values.put("TYPE",task.getType());
        values.put("IS_COMPLETED", task.isCompleted() ? 1 : 0);
        long check = database.insert("TASKS", null, values);
        if(check<=0){
            return -1;
        }
        return 1;
    }
    public long updateTask(Task task){
        ContentValues values = new ContentValues();
        values.put("ID",task.getId());
        values.put("TITLE",task.getTitle());
        values.put("CONTENT",task.getContent());
        values.put("DATE",task.getDate());
        values.put("TYPE",task.getType());
        values.put("IS_COMPLETED", task.isCompleted() ? 1 : 0);
        long check = database.update("TASKS", values,"ID=?", new String[]{String.valueOf(task.getId())});
        if(check<=0){
            return -1;
        }
        return 1;
    }
    public long delTask(int id){
        long check = database.delete("TASKS","ID=?", new String[]{String.valueOf(id)});
        if(check<=0){
            return -1;
        }
        return 1;
    }
    public List<Task> getAllTasks(){
        List<Task> list = new ArrayList<>();
        database = dbhelper.getReadableDatabase();
        try{
            Cursor cursor = database.rawQuery("Select * from TASKS",null);
            if(cursor.getCount()>0){
                cursor.moveToFirst();
                do{
                    boolean isCompleted = cursor.getInt(5) == 1;
                    list.add(new Task(cursor.getInt(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4), isCompleted));

                }while(cursor.moveToNext());
            }
        }catch (Exception ex){
            Log.e("Error DB", ex.getMessage());
        }
        return list;
    }
}
