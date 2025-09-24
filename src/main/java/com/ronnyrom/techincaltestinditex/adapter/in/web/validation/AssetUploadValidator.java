package com.ronnyrom.techincaltestinditex.adapter.in.web.validation;

import org.springframework.stereotype.Component;

@Component
public class AssetUploadValidator {

    public record AssetFileUploadParams(String filename, String encodedFile, String contentType) {}

    public void validate(AssetFileUploadParams params) {
        if (params == null) throw new IllegalArgumentException("Request cannot be null");

        if (params.filename() == null || params.filename().isBlank())
            throw new IllegalArgumentException("Filename cannot be null or empty");
        if (params.encodedFile() == null || params.encodedFile().isBlank())
            throw new IllegalArgumentException("EncodedFile cannot be null or empty");
        if (params.contentType() == null || params.contentType().isBlank())
            throw new IllegalArgumentException("ContentType cannot be null or empty");

        if (params.filename().contains("..") || params.filename().contains("/") || params.filename().contains("\\"))
            throw new IllegalArgumentException("Nombre de archivo no válido");
    }
}