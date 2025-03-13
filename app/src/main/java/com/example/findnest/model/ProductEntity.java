package com.example.findnest.model;


import androidx.annotation.NonNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductEntity
{
    private int id;
    private String category;
    private String name;
    private int count;
    private int price;

    @Override
    public String toString()
    {
        return id + " - " + category + " - " + name + " - " + count + " - " + price;
    }
}
