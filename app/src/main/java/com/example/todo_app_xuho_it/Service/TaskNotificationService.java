package com.example.todo_app_xuho_it.Service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.todo_app_xuho_it.MainActivity;
import com.example.todo_app_xuho_it.Model.Task;
import com.example.todo_app_xuho_it.R;

import java.util.List;

public class TaskNotificationService {
    private static final String CHANNEL_ID = "TASK_NOTIFICATIONS";
    private static final String CHANNEL_NAME = "Task Alerts";
    private static final String CHANNEL_DESCRIPTION = "Notifications for task reminders and due dates";
    
    private Context context;
    private NotificationManager notificationManager;

    public TaskNotificationService(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void checkAndNotifyTasks(List<Task> tasks) {
        int notificationId = 1;
        
        for (Task task : tasks) {
            if (!task.isCompleted()) {
                if (task.isDueToday()) {
                    showTodayNotification(task, notificationId++);
                } else if (task.isOverdue()) {
                    showOverdueNotification(task, notificationId++);
                }
            }
        }
    }

    private void showTodayNotification(Task task, int notificationId) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_today)
            .setContentTitle("📅 Task Due Today!")
            .setContentText(task.getTitle())
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(task.getContent()))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0xFF4CAF50);

        notificationManager.notify(notificationId, builder.build());
        Log.d("TaskNotification", "Today notification sent for: " + task.getTitle());
    }

    private void showOverdueNotification(Task task, int notificationId) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle("⚠️ Task Overdue!")
            .setContentText(task.getTitle())
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText("Due: " + task.getFormattedDate() + "\n" + task.getContent()))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0xFFF44336);

        notificationManager.notify(notificationId, builder.build());
        Log.d("TaskNotification", "Overdue notification sent for: " + task.getTitle());
    }

    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }
}
