package com.example.findnest.model.response.media;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaDetailRes {
    private UUID id;
    private String path;
    private int order;
}
