package com.ronnyrom.techincaltestinditex.adapter.in.web.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AssetUploadValidatorTest {
    private final AssetUploadValidator validator = new AssetUploadValidator();

    @Test
    void validate_accepts_valid_request() {
        var request = new AssetUploadValidator.AssetFileUploadRequest(
                "image.jpg",
                "iVBORw0KGgoAAAANSUhEUgAAAAUA" +
                        "AAAFCAYAAACNbyblAAAAHElEQVQI12P4" +
                        "//8/w38GIAXDIBKE0DHxgljNBAAO" +
                        "9TXL0Y4OHwAAAABJRU5ErkJggg==",
                "image/jpg"
        );
        assertDoesNotThrow(() -> validator.validate(request));
    }
    @Test
    void validate_empty_request() {
        var request = new AssetUploadValidator.AssetFileUploadRequest(
                "",
                "",
                ""
        );
        assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
    }
    @Test
    void validate_empty_filename_request() {
        var request = new AssetUploadValidator.AssetFileUploadRequest(
                "",
                "iVBORw0KGgoAAAANSUhEUgAAAAUA" +
                        "AAAFCAYAAACNbyblAAAAHElEQVQI12P4" +
                        "//8/w38GIAXDIBKE0DHxgljNBAAO" +
                        "9TXL0Y4OHwAAAABJRU5ErkJggg==",
                "image/jpg"
        );
        assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
    }
    @Test
    void validate_empty_encodedFile_request() {
        var request = new AssetUploadValidator.AssetFileUploadRequest(
                "image.jpg",
                "",
                "image/jpg"
        );
        assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
    }
    @Test
    void validate_empty_contentType_request() {
        var request = new AssetUploadValidator.AssetFileUploadRequest(
                "image.jpg",
                "iVBORw0KGgoAAAANSUhEUgAAAAUA" +
                        "AAAFCAYAAACNbyblAAAAHElEQVQI12P4" +
                        "//8/w38GIAXDIBKE0DHxgljNBAAO" +
                        "9TXL0Y4OHwAAAABJRU5ErkJggg==",
                ""
        );
        assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
    }
}
