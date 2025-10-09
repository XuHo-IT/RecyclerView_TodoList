package com.example.todo_app_xuho_it.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.todo_app_xuho_it.Model.Task;
import com.example.todo_app_xuho_it.R;

import java.util.List;

public class TaskAdapter extends BaseAdapter {
    private Context context;
    private List<Task> taskList;
    private LayoutInflater inflater;

    public interface OnTaskActionListener {
        void onTaskComplete(Task task);
        void onTaskEdit(Task task);
        void onTaskDelete(Task task);
    }

    private OnTaskActionListener actionListener;

    public TaskAdapter(Context context, List<Task> taskList) {
        this.context = context;
        this.taskList = taskList;
        this.inflater = LayoutInflater.from(context);
    }

    public void setOnTaskActionListener(OnTaskActionListener listener) {
        this.actionListener = listener;
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public Object getItem(int position) {
        return taskList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return taskList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_task, parent, false);
            holder = new ViewHolder();
            holder.priorityHeader = convertView.findViewById(R.id.priorityHeader);
            holder.tvPriority = convertView.findViewById(R.id.tvPriority);
            holder.tvTitle = convertView.findViewById(R.id.tvTitle);
            holder.tvContent = convertView.findViewById(R.id.tvContent);
            holder.tvDate = convertView.findViewById(R.id.tvDate);
            holder.ivComplete = convertView.findViewById(R.id.ivComplete);
            holder.ivEdit = convertView.findViewById(R.id.ivEdit);
            holder.ivDelete = convertView.findViewById(R.id.ivDelete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Task task = taskList.get(position);
        
        // Set task details
        holder.tvTitle.setText(task.getTitle());
        holder.tvContent.setText(task.getContent());
        holder.tvDate.setText("Deadline: " + task.getFormattedDate());
        holder.tvPriority.setText(task.getPriorityString());

        // Set priority header color
        String priorityColor = task.getPriorityColor();
        holder.priorityHeader.setBackgroundColor(Color.parseColor(priorityColor));

        // Set completion status with enhanced visual differences
        if (task.isCompleted()) {
            // Completed task styling
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvContent.setPaintFlags(holder.tvContent.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setTextColor(Color.parseColor("#9E9E9E")); // Light gray
            holder.tvContent.setTextColor(Color.parseColor("#BDBDBD")); // Even lighter gray
            holder.tvDate.setTextColor(Color.parseColor("#9E9E9E")); // Light gray for date
            
            // Fade the entire card
            convertView.setAlpha(0.6f);
            
            // Change priority header to muted color
            String mutedColor = getMutedPriorityColor(task.getPriorityColor());
            holder.priorityHeader.setBackgroundColor(Color.parseColor(mutedColor));
            
            // Show completed checkmark
            holder.ivComplete.setImageResource(R.drawable.ic_check_circle);
            holder.ivComplete.setAlpha(1.0f);
            
        } else {
            // Incomplete task styling (full vibrancy)
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvContent.setPaintFlags(holder.tvContent.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvTitle.setTextColor(Color.BLACK);
            holder.tvContent.setTextColor(Color.parseColor("#424242")); // Dark gray for better contrast
            holder.tvDate.setTextColor(Color.parseColor("#F44336")); // Red for urgency
            
            // Full opacity
            convertView.setAlpha(1.0f);
            
            // Full priority color
            holder.priorityHeader.setBackgroundColor(Color.parseColor(task.getPriorityColor()));
            
            // Show incomplete circle
            holder.ivComplete.setImageResource(R.drawable.ic_radio_button_unchecked);
            holder.ivComplete.setAlpha(1.0f);
        }

        // Set action listeners
        holder.ivComplete.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onTaskComplete(task);
            }
        });

        holder.ivEdit.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onTaskEdit(task);
            }
        });

        holder.ivDelete.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onTaskDelete(task);
            }
        });

        return convertView;
    }

    static class ViewHolder {
        LinearLayout priorityHeader;
        TextView tvPriority;
        TextView tvTitle;
        TextView tvContent;
        TextView tvDate;
        ImageView ivComplete;
        ImageView ivEdit;
        ImageView ivDelete;
    }

    public void updateTaskList(List<Task> newTaskList) {
        this.taskList = newTaskList;
        notifyDataSetChanged();
    }

    private String getMutedPriorityColor(String originalColor) {
        switch (originalColor) {
            case "#F44336": // High - Red
                return "#FFCDD2"; // Light red
            case "#FF9800": // Medium - Orange
                return "#FFE0B2"; // Light orange
            case "#4CAF50": // Low - Green
                return "#C8E6C9"; // Light green
            default:
                return "#F5F5F5"; // Default light gray
        }
    }
}
