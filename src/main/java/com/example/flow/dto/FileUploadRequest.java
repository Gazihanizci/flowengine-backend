package com.example.flow.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class FileUploadRequest {

    private MultipartFile file;

    private Long surecId;
    private Long adimId;
    private Long aksiyonId;
}