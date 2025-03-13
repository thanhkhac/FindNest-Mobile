package com.example.findnest.model;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentEntity
{
    private int id;
    private String category;
    private String name;
    private int count;
    private int price;
}
