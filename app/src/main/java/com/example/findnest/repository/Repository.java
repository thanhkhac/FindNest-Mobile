package com.example.findnest.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.example.findnest.model.ProductEntity;
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


    public long create(ProductEntity product)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "INSERT INTO Products (Id, Category, Name, Count, Price) VALUES (?, ?, ?, ?, ?)";
        SQLiteStatement stmt = db.compileStatement(sql);

        stmt.bindLong(1, product.getId());
        stmt.bindString(2, product.getCategory());
        stmt.bindString(3, product.getName());
        stmt.bindLong(4, product.getCount());
        stmt.bindLong(5, product.getPrice());

        long rowId = stmt.executeInsert();
        db.close();
        return rowId;
    }

    public List<ProductEntity> getAll()
    {
        List<ProductEntity> products = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT Id, Category, Name, Count, Price FROM Products";

        try
        {
            Cursor cursor = db.rawQuery(sql, null);
            Log.d("DEBUG", "Cursor count: " + cursor.getCount());

            products = cursorToListProduct(cursor);
            cursor.close();
        } catch (Exception e)
        {
            Log.e("ERROR", "Lỗi khi truy vấn dữ liệu: " + e.getMessage(), e);
        } finally
        {
            db.close();
        }
        return products;
    }


    public int update(ProductEntity product)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "UPDATE Products SET Category = ?, Name = ?, Count = ?, Price = ? WHERE Id = ?";
        SQLiteStatement stmt = db.compileStatement(sql);

        stmt.bindString(1, product.getCategory());
        stmt.bindString(2, product.getName());
        stmt.bindLong(3, product.getCount());
        stmt.bindLong(4, product.getPrice());
        stmt.bindLong(5, product.getId());

        int rowsAffected = stmt.executeUpdateDelete();
        stmt.close();
        db.close();
        return rowsAffected;
    }

    public int delete(int id)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "DELETE FROM Products WHERE Id = ?";
        SQLiteStatement stmt = db.compileStatement(sql);
        stmt.bindLong(1, id);

        int rowsAffected = stmt.executeUpdateDelete();
        stmt.close();
        db.close();
        return rowsAffected;
    }

    public ProductEntity getById(int id)
    {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT Id, Category, Name, Count, Price FROM Products WHERE Id = " + id;
        Cursor cursor = db.rawQuery(sql, null);

        ProductEntity product = null;
        if (cursor.moveToFirst())
        {
            product = cursorToProduct(cursor);
        }

        cursor.close();
        db.close();
        return product;
    }

    public List<ProductEntity> searchByCategory(String category) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM Products WHERE Category LIKE '%" + category + "%'";
        Cursor cursor = db.rawQuery(sql, null);

        List<ProductEntity> products = cursorToListProduct(cursor);
        cursor.close();
        db.close();
        return products;
    }

    public List<ProductEntity> searchByName(String name) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM Products WHERE Name LIKE '%" + name + "%'";
        Cursor cursor = db.rawQuery(sql, null);

        List<ProductEntity> products = cursorToListProduct(cursor);
        cursor.close();
        db.close();
        return products;
    }

    public List<ProductEntity> searchByCount(int count) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM Products WHERE Count = " + count;
        Cursor cursor = db.rawQuery(sql, null);

        List<ProductEntity> products = cursorToListProduct(cursor);
        cursor.close();
        db.close();
        return products;
    }

    public List<ProductEntity> searchByField(String field, String keyword) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = null;

        switch (field) {
            case "Category":
                sql = "SELECT * FROM Products WHERE Category LIKE '%" + keyword + "%'";
                break;
            case "Name":
                sql = "SELECT * FROM Products WHERE Name LIKE '%" + keyword + "%'";
                break;
            case "Count":
                sql = "SELECT * FROM Products WHERE Count = " + keyword;
                break;
            default:
                return new ArrayList<>();
        }

        Cursor cursor = db.rawQuery(sql, null);
        List<ProductEntity> products = cursorToListProduct(cursor);
        cursor.close();
        db.close();
        return products;
    }

    public List<ProductEntity> searchByPriceRange(int minPrice, int maxPrice) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM Products WHERE Price BETWEEN " + minPrice + " AND " + maxPrice;
        Cursor cursor = db.rawQuery(sql, null);

        List<ProductEntity> products = cursorToListProduct(cursor);
        cursor.close();
        db.close();
        return products;
    }



    /// Cursor:
    /// Cursor hay con trỏ, dùng để duyệt qua từng kết quả truy vấn trả về
    /// - cursor.moveToFirst(): Di chuyển con trỏ tới hàng đầu tiên. Trả về true nếu có hàng đầu tiên.
    private List<ProductEntity> cursorToListProduct(Cursor cursor)
    {
        List<ProductEntity> products = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst())
        {
            do
            {
                var product = cursorToProduct(cursor);
                products.add(product);
                Log.d("INFO", "Id " + products.toString());
            } while (cursor.moveToNext());
        }
        return products;
    }

    private ProductEntity cursorToProduct(Cursor cursor)
    {
        try
        {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("Id"));
            String category = cursor.getString(cursor.getColumnIndexOrThrow("Category"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("Name"));
            int count = cursor.getInt(cursor.getColumnIndexOrThrow("Count"));
            int price = cursor.getInt(cursor.getColumnIndexOrThrow("Price"));
            Log.d("INFO", "Id " + String.valueOf(id));
            Log.d("INFO", "Category " + String.valueOf(category));
            Log.d("INFO", "Name " + String.valueOf(name));
            Log.d("INFO", "Count " + String.valueOf(count));
            Log.d("INFO", "Price " + String.valueOf(price));
            return ProductEntity.builder()
                    .id(id)
                    .category(category)
                    .name(name)
                    .count(count)
                    .price(price)
                    .build();
        } catch (Exception e)
        {
            Log.e("ERROR", "Lỗi khi truy vấn dữ liệu: " + e.getMessage(), e);
        }
        return null;
    }
}
