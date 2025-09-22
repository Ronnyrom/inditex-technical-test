package com.ronnyrom.techincaltestinditex.adapter.in.web.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Set;

@Component
public class AssetFilterValidator {

    private final Validator validator;

    public AssetFilterValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    public void validate(AssetFilterParams params) {
        Set<ConstraintViolation<AssetFilterParams>> violations = validator.validate(params);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(violations.iterator().next().getMessage());
        }
        if (params.uploadDateStart != null && params.uploadDateEnd != null) {
            try {
                LocalDateTime start = LocalDateTime.parse(params.uploadDateStart);
                LocalDateTime end = LocalDateTime.parse(params.uploadDateEnd);
                if (!start.isBefore(end)) {
                    throw new IllegalArgumentException("uploadDateStart debe ser anterior a uploadDateEnd");
                }
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Las fechas deben estar en formato ISO-8601");
            }
        }
    }

    public static class AssetFilterParams {
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2})?$", message = "uploadDateStart debe ser ISO-8601")
        private final String uploadDateStart;

        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2})?$", message = "uploadDateEnd debe ser ISO-8601")
        private final String uploadDateEnd;

        @Size(max = 255, message = "filename demasiado largo")
        private final String filename;

        @Size(max = 100, message = "filetype demasiado largo")
        private final String filetype;

        @Pattern(regexp = "ASC|DESC", message = "sortDirection debe ser ASC o DESC")
        private final String sortDirection;

        public AssetFilterParams(String uploadDateStart, String uploadDateEnd, String filename, String filetype, String sortDirection) {
            this.uploadDateStart = uploadDateStart;
            this.uploadDateEnd = uploadDateEnd;
            this.filename = filename;
            this.filetype = filetype;
            this.sortDirection = sortDirection;
        }
    }
}