package com.example.findnest.model.responsedtos;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MediaResponse
{
    private UUID id;
    private String path;
    private int order;
}
