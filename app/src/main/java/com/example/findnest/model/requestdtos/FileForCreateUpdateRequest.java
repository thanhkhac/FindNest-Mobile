package com.example.findnest.model.requestdtos;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class FileForCreateUpdateRequest
{

    private UUID id;

    private byte[] fileContent;

    private int order = 0;

    private String name;

    private String path;
}