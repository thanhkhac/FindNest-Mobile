package com.example.findnest.utils;

import android.database.Cursor;

import java.lang.reflect.Field;

public class CursorMapper
{
    public static <T> T mapCursorToObject(Cursor cursor, Class<T> clazz)
    {
        try
        {
            T obj = clazz.newInstance();

            for (Field field : clazz.getDeclaredFields())
            {
                field.setAccessible(true);
                String columnName = field.getName();

                int columnIndex = cursor.getColumnIndex(columnName);
                if (columnIndex == -1) continue;

                Class<?> fieldType = field.getType();

                if (fieldType == int.class || fieldType == Integer.class)
                {
                    field.set(obj, cursor.getInt(columnIndex));
                } else if (fieldType == String.class)
                {
                    field.set(obj, cursor.getString(columnIndex));
                } else if (fieldType == double.class || fieldType == Double.class)
                {
                    field.set(obj, cursor.getDouble(columnIndex));
                }
            }
            return obj;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
