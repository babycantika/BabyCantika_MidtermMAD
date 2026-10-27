
package com.example.studentenrollment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "StudentDB.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "students";
    public static final String COL_ID = "_id";
    public static final String COL_NAME = "student_name";
    public static final String COL_STUDENT_ID = "student_id";
    public static final String COL_ENROLLED = "enrolled";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_STUDENT_ID + " TEXT NOT NULL UNIQUE, "
                + COL_ENROLLED + " INTEGER NOT NULL DEFAULT 0"
                + ")";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion,
                          int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean isStudentIdExists(String studentId,
                                     long excludeId) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_NAME,
                new String[]{COL_ID},
                COL_STUDENT_ID + " = ? AND " + COL_ID + " != ?",
                new String[]{
                        studentId,
                        String.valueOf(excludeId)
                },
                null, null, null
        );

        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    public long insertStudent(String name,
                              String studentId,
                              boolean enrolled) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_STUDENT_ID, studentId);
        values.put(COL_ENROLLED, enrolled ? 1 : 0);

        try {
            return db.insertOrThrow(TABLE_NAME, null, values);
        } catch (SQLiteConstraintException e) {
            return -1;
        }
    }

    public int updateStudent(long id,
                             String name,
                             String studentId,
                             boolean enrolled) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_STUDENT_ID, studentId);
        values.put(COL_ENROLLED, enrolled ? 1 : 0);

        try {
            return db.update(
                    TABLE_NAME,
                    values,
                    COL_ID + " = ?",
                    new String[]{String.valueOf(id)}
            );
        } catch (SQLiteConstraintException e) {
            return 0;
        }
    }

    public int deleteStudent(long id) {
        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                TABLE_NAME,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    public ArrayList<Student> getAllStudents() {
        ArrayList<Student> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_NAME,
                new String[]{
                        COL_ID,
                        COL_NAME,
                        COL_STUDENT_ID,
                        COL_ENROLLED
                },
                null, null, null, null,
                COL_ID + " DESC"
        );

        try {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(
                        cursor.getColumnIndexOrThrow(COL_ID));

                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_NAME));

                String studentId = cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_STUDENT_ID));

                boolean enrolled = cursor.getInt(
                        cursor.getColumnIndexOrThrow(COL_ENROLLED)) == 1;

                list.add(new Student(id, name, studentId, enrolled));
            }
        } finally {
            cursor.close();
        }

        return list;
    }

    public static class Student {
        public long id;
        public String name;
        public String studentId;
        public boolean enrolled;

        public Student(long id, String name,
                       String studentId, boolean enrolled) {
            this.id = id;
            this.name = name;
            this.studentId = studentId;
            this.enrolled = enrolled;
        }

        @Override
        public String toString() {
            return name
                    + "\nStudent ID: " + studentId
                    + "\nStatus: "
                    + (enrolled ? "Enrolled" : "Not Enrolled");
        }
    }
}
