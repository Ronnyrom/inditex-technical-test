package com.ronnyrom.techincaltestinditex.adapter.in.web.mapper;

import com.ronnyrom.adapter.in.web.model.AssetFileUploadRequest;
import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class AssetUploadMapperTest {
    private final AssetFileUploadMapper mapper = new AssetFileUploadMapper();


    @Test
    void toDomain_maps_all_basic_fields() {
        AssetFileUploadRequest assetFileUploadRequest = generateAssetFileUploadRequest("hello.jpg", "test file".getBytes(StandardCharsets.UTF_8), "image/jpg");

        var domain = mapper.toDomain(assetFileUploadRequest);

        assertNotNull(domain, "El dominio no debe ser null");
        assertEquals("hello.jpg", domain.getFilename());
        assertEquals("image/jpg", domain.getContentType());
        assertEquals(assetFileUploadRequest.getEncodedFile().length, domain.getSize());
        assertArrayEquals(assetFileUploadRequest.getEncodedFile(), domain.getEncodedFile());
    }

    @Test
    void toDomain_handles_empty_file() {
        AssetFileUploadRequest assetFileUploadRequest = generateAssetFileUploadRequest("test empty file", new byte[0], "text/plain");

        var domain = mapper.toDomain(assetFileUploadRequest);

        assertNotNull(domain);
        assertEquals("test empty file", domain.getFilename());
        assertEquals("text/plain", domain.getContentType());
        assertEquals(0, domain.getSize());
        assertArrayEquals(assetFileUploadRequest.getEncodedFile(), domain.getEncodedFile());
    }
    @Test
    void toResponse_maps_id_to_string() {
        AssetDomain assetDomain = AssetDomain.builder().id(42).build();

        var resp = mapper.toResponse(assetDomain);

        assertNotNull(resp);
        assertEquals("42", resp.getId());
    }

    private AssetFileUploadRequest generateAssetFileUploadRequest(String filename, byte[] bytes, String contentType) {
        AssetFileUploadRequest assetFileUploadRequest = new AssetFileUploadRequest();
        assetFileUploadRequest.filename(filename);
        assetFileUploadRequest.encodedFile(bytes);
        assetFileUploadRequest.contentType(contentType);
        return assetFileUploadRequest;
    }

}