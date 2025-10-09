package com.example.todo_app_xuho_it.Model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Task {
    public enum Priority {
        HIGH, MEDIUM, LOW
    }
    
    private int id;
    private String title;
    private String content;
    private String date;
    private Priority priority;
    private boolean isCompleted;
    private Date dueDate;

    public Task() {
    }

    public Task(int id, String title, String content, String date, Priority priority) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
        this.priority = priority;
        this.isCompleted = false;
        this.dueDate = parseDate(date);
    }

    public Task(int id, String title, String content, String date, Priority priority, boolean isCompleted) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
        this.priority = priority;
        this.isCompleted = isCompleted;
        this.dueDate = parseDate(date);
    }

    // Legacy constructor for backward compatibility
    public Task(int id, String title, String content, String date, String type) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
        this.priority = parsePriorityFromString(type);
        this.isCompleted = false;
        this.dueDate = parseDate(date);
    }

    public Task(int id, String title, String content, String date, String type, boolean isCompleted) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
        this.priority = parsePriorityFromString(type);
        this.isCompleted = isCompleted;
        this.dueDate = parseDate(date);
    }

    private Priority parsePriorityFromString(String priorityString) {
        if (priorityString == null) return Priority.MEDIUM;
        
        switch (priorityString.toUpperCase()) {
            case "HIGH":
                return Priority.HIGH;
            case "MEDIUM":
                return Priority.MEDIUM;
            case "LOW":
                return Priority.LOW;
            default:
                return Priority.MEDIUM;
        }
    }

    public String getPriorityColor() {
        switch (priority) {
            case HIGH:
                return "#F44336"; // Red
            case MEDIUM:
                return "#FF9800"; // Orange
            case LOW:
                return "#4CAF50"; // Green
            default:
                return "#FF9800"; // Default to Orange
        }
    }

    public String getPriorityString() {
        return priority != null ? priority.toString() : "MEDIUM";
    }

    private Date parseDate(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.parse(dateString);
        } catch (Exception e) {
            return new Date(); // Return current date if parsing fails
        }
    }

    public String getFormattedDate() {
        if (dueDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.format(dueDate);
        }
        return date;
    }

    public boolean isDueToday() {
        if (dueDate == null) return false;
        
        Calendar today = Calendar.getInstance();
        Calendar taskDate = Calendar.getInstance();
        taskDate.setTime(dueDate);
        
        return today.get(Calendar.YEAR) == taskDate.get(Calendar.YEAR) &&
               today.get(Calendar.DAY_OF_YEAR) == taskDate.get(Calendar.DAY_OF_YEAR);
    }

    public boolean isOverdue() {
        if (dueDate == null) return false;
        return dueDate.before(new Date()) && !isCompleted;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return getPriorityString();
    }

    public void setType(String type) {
        this.priority = parsePriorityFromString(type);
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
        if (dueDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            this.date = sdf.format(dueDate);
        }
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", date='" + date + '\'' +
                ", priority=" + priority +
                ", isCompleted=" + isCompleted +
                '}';
    }
}
