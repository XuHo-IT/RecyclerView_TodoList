package com.example.todo_app_xuho_it;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.todo_app_xuho_it.Adapter.TaskAdapter;
import com.example.todo_app_xuho_it.DAO.TaskDAO;
import com.example.todo_app_xuho_it.Model.Task;
import com.example.todo_app_xuho_it.Service.TaskNotificationService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskActionListener {
    // UI Components
    private EditText edSearch;
    private ListView lvTask;
    private TextView tvTaskCounter, tvFilterAll, tvFilterHigh, tvFilterMedium, tvFilterLow;
    private Button fabAdd;
    private AlertDialog taskDialog;
    
    // Data and Services
    private TaskDAO taskDAO;
    private TaskAdapter taskAdapter;
    private TaskNotificationService notificationService;
    private Calendar selectedDate = Calendar.getInstance();
    private Task selectedTask = null;
    private List<Task> allTasks = new ArrayList<>();
    private List<Task> filteredTasks = new ArrayList<>();
    private String currentFilter = "All";
    
    // Constants
    private static final int NOTIFICATION_PERMISSION_CODE = 1001;
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Check if user is logged in
        if (!isUserLoggedIn()) {
            navigateToLogin();
            return;
        }
        
        // Clear temporary login state if it exists (for non-remember me logins)
        clearTemporaryLoginState();
        
        // Get username from intent or SharedPreferences
        String username = getIntent().getStringExtra("username");
        if (username == null) {
            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            username = sharedPreferences.getString(KEY_USERNAME, "User");
        }
        
        // Set action bar title with welcome message
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Welcome, " + username + "!");
        }
        
        // Initialize components
        taskDAO = new TaskDAO(this);
        notificationService = new TaskNotificationService(this);
        
        // Request notification permission
        requestNotificationPermission();
        
        initGUI();
        setupEventListeners();
        loadTasks();
        updateTaskCounter();
        checkTaskAlerts();
    }

    private void requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.POST_NOTIFICATIONS}, 
                NOTIFICATION_PERMISSION_CODE);
        }
    }

    private void initGUI() {
        try {
            // Views
            edSearch = findViewById(R.id.edSearch);
            lvTask = findViewById(R.id.lvTask);
            tvTaskCounter = findViewById(R.id.tvTaskCounter);
            fabAdd = findViewById(R.id.fabAdd);
            
            // Filter tabs
            tvFilterAll = findViewById(R.id.tvFilterAll);
            tvFilterHigh = findViewById(R.id.tvFilterHigh);
            tvFilterMedium = findViewById(R.id.tvFilterMedium);
            tvFilterLow = findViewById(R.id.tvFilterLow);
            
            // Initialize adapter
            taskAdapter = new TaskAdapter(this, filteredTasks);
            taskAdapter.setOnTaskActionListener(this);
            lvTask.setAdapter(taskAdapter);
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing GUI: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupEventListeners() {
        // Floating action button
        fabAdd.setOnClickListener(v -> showTaskDialog(null));
        
        // Logout button
        Button btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> handleLogout());
        }
        
        // Search functionality
        edSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                filterTasks();
            }
        });
        
        // Priority filter tabs
        tvFilterAll.setOnClickListener(v -> setPriorityFilter("All"));
        tvFilterHigh.setOnClickListener(v -> setPriorityFilter("HIGH"));
        tvFilterMedium.setOnClickListener(v -> setPriorityFilter("MEDIUM"));
        tvFilterLow.setOnClickListener(v -> setPriorityFilter("LOW"));
    }

    private void showTaskDialog(Task taskToEdit) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_task_form, null);
        
        // Get dialog views
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        EditText edDialogTitle = dialogView.findViewById(R.id.edDialogTitle);
        EditText edDialogContent = dialogView.findViewById(R.id.edDialogContent);
        EditText edDialogDate = dialogView.findViewById(R.id.edDialogDate);
        Spinner spinnerDialogPriority = dialogView.findViewById(R.id.spinnerDialogPriority);
        Button btnDialogSave = dialogView.findViewById(R.id.btnDialogSave);
        Button btnDialogCancel = dialogView.findViewById(R.id.btnDialogCancel);
        
        // Setup priority spinner
        String[] priorities = {"HIGH", "MEDIUM", "LOW"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priorities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDialogPriority.setAdapter(adapter);
        
        // Set dialog title and fill data if editing
        if (taskToEdit != null) {
            tvDialogTitle.setText("Edit Task");
            edDialogTitle.setText(taskToEdit.getTitle());
            edDialogContent.setText(taskToEdit.getContent());
            edDialogDate.setText(taskToEdit.getFormattedDate());
            
            // Set priority spinner
            String priorityString = taskToEdit.getPriorityString();
            for (int i = 0; i < priorities.length; i++) {
                if (priorities[i].equals(priorityString)) {
                    spinnerDialogPriority.setSelection(i);
                    break;
                }
            }
            
            if (taskToEdit.getDueDate() != null) {
                selectedDate.setTime(taskToEdit.getDueDate());
            }
        } else {
            tvDialogTitle.setText("Add New Task");
            selectedDate = Calendar.getInstance();
            updateDialogDateDisplay(edDialogDate);
            spinnerDialogPriority.setSelection(1); // Default to MEDIUM
        }
        
        // Date picker
        edDialogDate.setOnClickListener(v -> showDatePickerDialog(edDialogDate));
        
        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        taskDialog = builder.create();
        taskDialog.show();
        
        // Save button
        btnDialogSave.setOnClickListener(v -> {
            if (validateDialogInput(edDialogTitle, edDialogContent)) {
                String title = edDialogTitle.getText().toString().trim();
                String content = edDialogContent.getText().toString().trim();
                String date = edDialogDate.getText().toString().trim();
                String priorityString = spinnerDialogPriority.getSelectedItem().toString();
                
                Task.Priority priority = Task.Priority.valueOf(priorityString);
                Task task = new Task(0, title, content, date, priority);
                
                if (taskToEdit != null) {
                    // Update existing task
                    taskToEdit.setTitle(title);
                    taskToEdit.setContent(content);
                    taskToEdit.setDate(date);
                    taskToEdit.setPriority(priority);
                    taskToEdit.setDueDate(selectedDate.getTime());
                    
                    long result = taskDAO.updateTask(taskToEdit);
                    if (result > 0) {
                        Toast.makeText(this, "✅ Task updated successfully!", Toast.LENGTH_SHORT).show();
                        taskDialog.dismiss();
                        loadTasks();
                        updateTaskCounter();
                        checkTaskAlerts();
                    } else {
                        Toast.makeText(this, "❌ Failed to update task", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Add new task
                    long result = taskDAO.addTask(task);
                    if (result > 0) {
                        Toast.makeText(this, "✅ Task added successfully!", Toast.LENGTH_SHORT).show();
                        taskDialog.dismiss();
                        loadTasks();
                        updateTaskCounter();
                        checkTaskAlerts();
                    } else {
                        Toast.makeText(this, "❌ Failed to add task", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        
        // Cancel button
        btnDialogCancel.setOnClickListener(v -> taskDialog.dismiss());
    }

    private void showDatePickerDialog(EditText dateField) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDate.set(year, month, dayOfMonth);
                updateDialogDateDisplay(dateField);
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void updateDialogDateDisplay(EditText dateField) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateField.setText(sdf.format(selectedDate.getTime()));
    }

    private boolean validateDialogInput(EditText titleField, EditText contentField) {
        boolean isValid = true;
        
        if (TextUtils.isEmpty(titleField.getText())) {
            titleField.setError("Title is required");
            isValid = false;
        }
        
        if (TextUtils.isEmpty(contentField.getText())) {
            contentField.setError("Content is required");
            isValid = false;
        }
        
        return isValid;
    }


    private void setPriorityFilter(String filter) {
        currentFilter = filter;
        updateFilterTabs();
        filterTasks();
    }

    private void updateFilterTabs() {
        // Reset all tabs
        tvFilterAll.setBackgroundResource(R.drawable.filter_unselected_background);
        tvFilterHigh.setBackgroundResource(R.drawable.filter_unselected_background);
        tvFilterMedium.setBackgroundResource(R.drawable.filter_unselected_background);
        tvFilterLow.setBackgroundResource(R.drawable.filter_unselected_background);
        
        // Set selected tab
        switch (currentFilter) {
            case "All":
                tvFilterAll.setBackgroundResource(R.drawable.filter_selected_background);
                break;
            case "HIGH":
                tvFilterHigh.setBackgroundResource(R.drawable.filter_selected_background);
                break;
            case "MEDIUM":
                tvFilterMedium.setBackgroundResource(R.drawable.filter_selected_background);
                break;
            case "LOW":
                tvFilterLow.setBackgroundResource(R.drawable.filter_selected_background);
                break;
        }
    }

    private void filterTasks() {
        filteredTasks.clear();
        
        String searchText = edSearch.getText().toString().toLowerCase();
        
        for (Task task : allTasks) {
            boolean matchesSearch = searchText.isEmpty() || 
                task.getTitle().toLowerCase().contains(searchText) ||
                task.getContent().toLowerCase().contains(searchText);
            
            boolean matchesFilter = currentFilter.equals("All") || 
                task.getPriorityString().equals(currentFilter);
            
            if (matchesSearch && matchesFilter) {
                filteredTasks.add(task);
            }
        }
        
        taskAdapter.updateTaskList(filteredTasks);
    }

    private void loadTasks() {
        allTasks = taskDAO.getAllTasks();
        filterTasks();
    }

    private void updateTaskCounter() {
        int completed = 0;
        int incomplete = 0;
        
        for (Task task : allTasks) {
            if (task.isCompleted()) {
                completed++;
            } else {
                incomplete++;
            }
        }
        
        tvTaskCounter.setText(completed + " completed / " + incomplete + " incomplete");
    }

    private void checkTaskAlerts() {
        notificationService.checkAndNotifyTasks(allTasks);
    }

    // TaskAdapter.OnTaskActionListener implementation
    @Override
    public void onTaskComplete(Task task) {
        task.setCompleted(!task.isCompleted());
        long result = taskDAO.updateTask(task);
        
        if (result > 0) {
            String status = task.isCompleted() ? "completed" : "reopened";
            Toast.makeText(this, "✅ Task " + status + "!", Toast.LENGTH_SHORT).show();
            loadTasks();
            updateTaskCounter();
        }
    }

    @Override
    public void onTaskEdit(Task task) {
        showTaskDialog(task);
    }

    @Override
    public void onTaskDelete(Task task) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Delete", (dialog, which) -> {
                long result = taskDAO.delTask(task.getId());
                
                if (result > 0) {
                    Toast.makeText(this, "✅ Task deleted successfully!", Toast.LENGTH_SHORT).show();
                    loadTasks();
                    updateTaskCounter();
                } else {
                    Toast.makeText(this, "❌ Failed to delete task", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
        updateTaskCounter();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            handleLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private boolean isUserLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void clearTemporaryLoginState() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isTemporary = sharedPreferences.getBoolean("is_temporary_login", false);
        if (isTemporary) {
            // Clear the temporary login state
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
        }
    }
    
    private void handleLogout() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout", (dialog, which) -> {
                // Clear login state
                SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                navigateToLogin();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
}