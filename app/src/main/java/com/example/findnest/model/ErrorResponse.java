package com.example.findnest.model;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ErrorResponse
{
    private String status;
    private int statusCode;
    private String message;
    private String errorCode;
    private Map<String, List<String>> errors;
}
