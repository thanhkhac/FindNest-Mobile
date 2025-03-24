package com.example.findnest.model.requestdtos;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import okhttp3.MultipartBody;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PostForCreateRequest extends PostForManipulationRequest
{

    private MultipartBody.Part thumbnail;
}
