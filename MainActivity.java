
package com.example.studentenrollment;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private EditText etStudentName;
    private EditText etStudentId;
    private CheckBox cbEnrolled;

    private Button btnSave;
    private Button btnUpdate;
    private Button btnDelete;
    private Button btnClear;

    private ListView lvStudents;

    private DatabaseHelper databaseHelper;
    private ArrayList<DatabaseHelper.Student> studentList;
    private ArrayAdapter<DatabaseHelper.Student> adapter;

    // -1 means no student is selected.
    private long selectedDbId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views.
        etStudentName = findViewById(R.id.etStudentName);
        etStudentId = findViewById(R.id.etStudentId);
        cbEnrolled = findViewById(R.id.cbEnrolled);

        btnSave = findViewById(R.id.btnSave);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        btnClear = findViewById(R.id.btnClear);

        lvStudents = findViewById(R.id.lvStudents);

        // Initialize SQLite database.
        databaseHelper = new DatabaseHelper(this);

        studentList = new ArrayList<>();

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                studentList
        );

        lvStudents.setAdapter(adapter);

        // Add student.
        btnSave.setOnClickListener(v -> addStudent());

        // Update selected student.
        btnUpdate.setOnClickListener(v -> updateStudent());

        // Delete selected student.
        btnDelete.setOnClickListener(v -> confirmDelete());

        // Clear form and selection.
        btnClear.setOnClickListener(v -> clearForm());

        // Select a student from the list.
        lvStudents.setOnItemClickListener((parent, view, position, id) -> {
            DatabaseHelper.Student student =
                    studentList.get(position);

            selectedDbId = student.id;

            etStudentName.setText(student.name);
            etStudentId.setText(student.studentId);
            cbEnrolled.setChecked(student.enrolled);

            Toast.makeText(
                    MainActivity.this,
                    "Student selected",
                    Toast.LENGTH_SHORT
            ).show();
        });

        loadStudents();
    }

    // Read input and validate it.
    private boolean validateInput() {
        String name = etStudentName.getText()
                .toString().trim();

        String studentId = etStudentId.getText()
                .toString().trim();

        if (name.isEmpty()) {
            etStudentName.setError("Student name is required");
            etStudentName.requestFocus();
            return false;
        }

        if (studentId.isEmpty()) {
            etStudentId.setError("Student ID is required");
            etStudentId.requestFocus();
            return false;
        }

        return true;
    }

    // CREATE: add a new student.
    private void addStudent() {
        if (!validateInput()) {
            return;
        }

        String name = etStudentName.getText()
                .toString().trim();

        String studentId = etStudentId.getText()
                .toString().trim();

        boolean enrolled = cbEnrolled.isChecked();

        // Check duplicate Student ID.
        if (databaseHelper.isStudentIdExists(studentId, -1)) {
            etStudentId.setError("Student ID already exists");
            etStudentId.requestFocus();

            Toast.makeText(
                    this,
                    "Student ID must be unique!",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        long result = databaseHelper.insertStudent(
                name,
                studentId,
                enrolled
        );

        if (result != -1) {
            Toast.makeText(
                    this,
                    "Student added successfully",
                    Toast.LENGTH_SHORT
            ).show();

            clearForm();
            loadStudents();
        } else {
            Toast.makeText(
                    this,
                    "Failed to add student. Check Student ID.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // UPDATE: modify the selected student.
    private void updateStudent() {
        if (selectedDbId == -1) {
            Toast.makeText(
                    this,
                    "Please select a student from the list first",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        if (!validateInput()) {
            return;
        }

        String name = etStudentName.getText()
                .toString().trim();

        String studentId = etStudentId.getText()
                .toString().trim();

        boolean enrolled = cbEnrolled.isChecked();

        // Allow the selected student to keep their own ID,
        // but reject an ID belonging to another student.
        if (databaseHelper.isStudentIdExists(
                studentId, selectedDbId)) {

            etStudentId.setError("Student ID already exists");
            etStudentId.requestFocus();

            Toast.makeText(
                    this,
                    "Another student already uses this ID",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        int result = databaseHelper.updateStudent(
                selectedDbId,
                name,
                studentId,
                enrolled
        );

        if (result > 0) {
            Toast.makeText(
                    this,
                    "Student updated successfully",
                    Toast.LENGTH_SHORT
            ).show();

            clearForm();
            loadStudents();
        } else {
            Toast.makeText(
                    this,
                    "Failed to update student",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // Ask for confirmation before deleting.
    private void confirmDelete() {
        if (selectedDbId == -1) {
            Toast.makeText(
                    this,
                    "Please select a student to delete",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Student")
                .setMessage(
                        "Are you sure you want to delete this student?"
                )
                .setPositiveButton("Delete", (dialog, which) -> {
                    int result = databaseHelper.deleteStudent(
                            selectedDbId
                    );

                    if (result > 0) {
                        Toast.makeText(
                                MainActivity.this,
                                "Student deleted successfully",
                                Toast.LENGTH_SHORT
                        ).show();

                        clearForm();
                        loadStudents();
                    } else {
                        Toast.makeText(
                                MainActivity.this,
                                "Failed to delete student",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // READ: load all students into the ListView.
    private void loadStudents() {
        ArrayList<DatabaseHelper.Student> latestStudents =
                databaseHelper.getAllStudents();

        studentList.clear();
        studentList.addAll(latestStudents);

        adapter.notifyDataSetChanged();
    }

    // Reset the form and remove selection.
    private void clearForm() {
        selectedDbId = -1;

        etStudentName.setText("");
        etStudentId.setText("");
        cbEnrolled.setChecked(false);

        etStudentName.setError(null);
        etStudentId.setError(null);

        lvStudents.clearChoices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
