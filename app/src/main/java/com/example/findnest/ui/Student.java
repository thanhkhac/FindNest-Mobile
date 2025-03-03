package com.example.findnest.ui;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Student {
    private String studentID;
    private String studentName;
    private String email;
    private String className, MajorName;
    private byte[] photo; // Ảnh dưới dạng mảng byte

}

