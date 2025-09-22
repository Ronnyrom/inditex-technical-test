package com.ronnyrom.techincaltestinditex.adapter.in.web.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AssetUploadValidator {
    private final Validator validator;

    public AssetUploadValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    public void validate(AssetFileUploadRequest request) {
        Set<ConstraintViolation<AssetFileUploadRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(violations.iterator().next().getMessage());
        }
        if (request.getFilename().contains("..") || request.getFilename().contains("/") || request.getFilename().contains("\\")) {
            throw new IllegalArgumentException("Nombre de archivo no válido");
        }

    }
    @Getter
    public static class AssetFileUploadRequest {
        @NotBlank(message = "Filename cannot be null or empty")
        private final String filename;
        @NotBlank(message = "EncodedFile cannot be null or empty")
        private final String encodedFile;
        @NotBlank(message = "ContentType cannot be null or empty")
        private final String contentType;

        public AssetFileUploadRequest(String filename, String encodedFile, String contentType) {
            this.filename = filename;
            this.encodedFile = encodedFile;
            this.contentType = contentType;
        }
    }
}
