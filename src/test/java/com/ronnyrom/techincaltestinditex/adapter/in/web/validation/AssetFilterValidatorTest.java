package com.ronnyrom.techincaltestinditex.adapter.in.web.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AssetFilterValidatorTest {
    private final AssetFilterValidator validator = new AssetFilterValidator();

    @Test
    void validate_accepts_valid_params() {
        var params = new AssetFilterValidator.AssetFilterParams(
                "2024-01-01T00:00:00", "2024-01-02T00:00:00",
                "file", "image/jpg", "ASC");
        assertDoesNotThrow(() -> validator.validate(params));
    }

    @Test
    void validate_rejects_invalid_date_format() {
        var params = new AssetFilterValidator.AssetFilterParams(
                "1987-2-11", "2024-01-02T00:00:00",
                null, null, "DESC");
        var ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(params));
        assertEquals("uploadDateStart debe ser ISO-8601", ex.getMessage());
    }

    @Test
    void validate_rejects_start_not_before_end() {
        var params = new AssetFilterValidator.AssetFilterParams(
                "2024-01-02T00:00:00", "2024-01-01T00:00:00",
                null, null, "ASC");
        var ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(params));
        assertEquals("uploadDateStart debe ser anterior a uploadDateEnd", ex.getMessage());
    }

    @Test
    void validate_rejects_invalid_sort_direction() {
        var params = new AssetFilterValidator.AssetFilterParams(
                null, null, null, null, "UP");
        var ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(params));
        assertEquals("sortDirection debe ser ASC o DESC", ex.getMessage());
    }
}
