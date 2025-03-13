package com.example.findnest.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper
{
    Context dbContext;
    public SQLiteDatabase db;
    static String dbName = "pe-database.db";
    static int dbVersion = 1;

    public DatabaseHelper(Context context)
    {
        super(context, dbName, null, dbVersion);
        dbContext = context;
    }

    public void DB2SDCard()
    {
        try
        {
            File file = dbContext.getDatabasePath(dbName);
            //Bỏ comment khi muốn xóa database hiện tại
            if (file.exists())
            {
                file.delete();
            }

            if (file.exists())
            {
                Toast.makeText(dbContext.getApplicationContext(), "file CSDL đã tồn tại!", Toast.LENGTH_LONG).show();
                this.close();
            } else
            {
                try
                {
                    this.getReadableDatabase();
                    InputStream in = dbContext.getAssets().open(dbName);
                    OutputStream out = new FileOutputStream(file);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0)
                    {
                        out.write(buf, 0, len);
                    }
                    out.close();
                    in.close();
                    Toast.makeText(dbContext.getApplicationContext(), "Tải database lên điện thoại thành công", Toast.LENGTH_LONG).show();
                } catch (Exception e)
                {
                    Toast.makeText(dbContext.getApplicationContext(), "Có lỗi xảy ra", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        } catch (Exception eio)
        {
            eio.printStackTrace();
            Toast.makeText(dbContext.getApplicationContext(), "Có lỗi xảy ra", Toast.LENGTH_LONG).show();
        }
    }

    public Cursor getCursor(String sql)
    {
        db = SQLiteDatabase.openDatabase(dbContext.getDatabasePath(dbName).getPath(), null, SQLiteDatabase.OPEN_READWRITE);
        Cursor c = db.rawQuery(sql, null);
        return c;
    }

    public void execsql(String sql)
    {
        db = SQLiteDatabase.openDatabase(dbContext.getDatabasePath(dbName).getPath(), null, SQLiteDatabase.OPEN_READWRITE);
        db.execSQL(sql);
        db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {

    }
}
