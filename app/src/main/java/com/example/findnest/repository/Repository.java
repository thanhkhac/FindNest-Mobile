package com.example.findnest.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.example.findnest.model.StudentEntity;
import com.example.findnest.utils.CursorMapper;
import com.example.findnest.utils.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class Repository
{
    private DatabaseHelper dbHelper;

    public Repository(Context context)
    {
        dbHelper = new DatabaseHelper(context);
    }


    public long createStudent(StudentEntity student)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "INSERT INTO Student (Id, Category, Name, Count, Price) VALUES (?, ?, ?, ?, ?)";
        SQLiteStatement stmt = db.compileStatement(sql);

        stmt.bindLong(1, student.getId());
        stmt.bindString(2, student.getCategory());
        stmt.bindString(3, student.getName());
        stmt.bindLong(4, student.getCount());
        stmt.bindLong(5, student.getPrice());

        long rowId = stmt.executeInsert();
        db.close();
        return rowId;
    }

    public int updateStudent(StudentEntity student)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "UPDATE Student SET Category = ?, Name = ?, Count = ?, Price = ? WHERE Id = ?";
        SQLiteStatement stmt = db.compileStatement(sql);

        stmt.bindString(1, student.getCategory());
        stmt.bindString(2, student.getName());
        stmt.bindLong(3, student.getCount());
        stmt.bindLong(4, student.getPrice());
        stmt.bindLong(5, student.getId());

        int rowsAffected = stmt.executeUpdateDelete();
        stmt.close();
        db.close();
        return rowsAffected;
    }

    public int deleteStudent(int id)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "DELETE FROM Student WHERE Id = ?";
        SQLiteStatement stmt = db.compileStatement(sql);
        stmt.bindLong(1, id);

        int rowsAffected = stmt.executeUpdateDelete();
        stmt.close();
        db.close();
        return rowsAffected;
    }

    public StudentEntity getStudentById(int id)
    {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT Id, Category, Name, Count, Price FROM Student WHERE Id = " + id;
        Cursor cursor = db.rawQuery(sql, null);

        StudentEntity student = null;
        if (cursor.moveToFirst())
        {
            student = cursorToStudent(cursor);
        }

        cursor.close();
        db.close();
        return student;
    }


    /// Cursor:
    /// Cursor hay con trỏ, dùng để duyệt qua từng kết quả truy vấn trả về
    /// - cursor.moveToFirst(): Di chuyển con trỏ tới hàng đầu tiên. Trả về true nếu có hàng đầu tiên.
    private List<StudentEntity> cursorToListStudent(Cursor cursor)
    {
        List<StudentEntity> students = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst())
        {
            do
            {
                var student = cursorToStudent(cursor);
                students.add(student);
            } while (cursor.moveToNext());
        }
        return students;
    }

    private StudentEntity cursorToStudent(Cursor cursor)
    {
        var student = CursorMapper.mapCursorToObject(cursor, StudentEntity.class);
        return student;
    }
}
