package com.ronnyrom.techincaltestinditex.adapter.out.persistence.mapper;

import com.ronnyrom.techincaltestinditex.adapter.out.persistence.entity.AssetEntity;
import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import com.ronnyrom.techincaltestinditex.domain.model.Status;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AssetMapperTest {

    private final AssetMapper mapper = new AssetMapper();

    @Test
    void toAssetEntity_maps_fields_and_sets_uploadDate_and_status_string() {
        AssetDomain d = AssetDomain.builder()
                .id(10)
                .filename("a.jpg")
                .contentType("image/jpg")
                .url("http://internal-storage/10")
                .size(123)
                .status(Status.COMPLETED)
                .build();

        AssetEntity e = mapper.toAssetEntity(d);

        assertEquals(d.getId(), e.getId());
        assertEquals(d.getFilename(), e.getFilename());
        assertEquals(d.getContentType(), e.getContentType());
        assertEquals(d.getUrl(), e.getUrl());
        assertEquals(d.getSize(), e.getSize());
        assertEquals("COMPLETED", e.getStatus());
        assertNotNull(e.getUploadDate());
    }

    @Test
    void toAsset_maps_all_fields_and_parses_status() {
        LocalDateTime now = LocalDateTime.now();
        AssetEntity e = AssetEntity.builder()
                .id(5)
                .filename("b.png")
                .contentType("image/png")
                .url("http://internal-storage/5")
                .size(456)
                .status("FAILED")
                .uploadDate(now)
                .build();

        AssetDomain d = mapper.toAsset(e);

        assertEquals(e.getId(), d.getId());
        assertEquals(e.getFilename(), d.getFilename());
        assertEquals(e.getContentType(), d.getContentType());
        assertEquals(e.getUrl(), d.getUrl());
        assertEquals(e.getSize(), d.getSize());
        assertEquals(Status.FAILED, d.getStatus());
        assertEquals(now, d.getUploadDate());
    }
}
