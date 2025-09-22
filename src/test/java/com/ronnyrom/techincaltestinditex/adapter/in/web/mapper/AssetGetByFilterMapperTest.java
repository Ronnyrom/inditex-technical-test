package com.ronnyrom.techincaltestinditex.adapter.in.web.mapper;

import com.ronnyrom.adapter.in.web.model.Asset;
import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import com.ronnyrom.techincaltestinditex.domain.model.AssetFilter;
import com.ronnyrom.techincaltestinditex.domain.model.SortDirection;
import com.ronnyrom.techincaltestinditex.domain.model.Status;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AssetGetByFilterMapperTest {
    private final AssetGetByFilterMapper mapper = new AssetGetByFilterMapper();

    @Test
    void toAssetFilter_parsesAllFields() {
        String start = "2024-05-01T10:15:30";
        String end = "2024-06-01T12:00:00";
        String filename = "photo";
        String filetype = "image/jpg";
        String sort = "ASC";
        AssetFilter createdAsset = AssetFilter.builder().
                uploadDateStart(LocalDateTime.parse(start)).
                uploadDateEnd(LocalDateTime.parse(end)).
                filename(filename).
                filetype(filetype).
                sortDirection(SortDirection.ASC).
                build();
        AssetFilter filter = mapper.toAssetFilter(start, end, filename, filetype, sort);

        assertNotNull(filter);
        assertEquals(LocalDateTime.parse(start), filter.getUploadDateStart());
        assertEquals(LocalDateTime.parse(end), filter.getUploadDateEnd());
        assertEquals(filename, filter.getFilename());
        assertEquals(filetype, filter.getFiletype());
        assertEquals(SortDirection.ASC, filter.getSortDirection());
        assertEquals(createdAsset, filter);
    }

    @Test
    void toAssetFilter_handlesNullsAndBlanksForDates() {
        AssetFilter filter = mapper.toAssetFilter("   ", null, null, null, null);

        assertNotNull(filter);
        assertNull(filter.getUploadDateStart());
        assertNull(filter.getUploadDateEnd());
        assertNull(filter.getFilename());
        assertNull(filter.getFiletype());
        assertNull(filter.getSortDirection());
    }

    @Test
    void toResponse_mapsDomainListToApiList() {
        LocalDateTime now = LocalDateTime.now();

        List<AssetDomain> assetsDomain = List.of(
                AssetDomain.builder()
                        .id(1)
                        .filename("a.jpg")
                        .contentType("image/jpg")
                        .url("http://internal-storage/1")
                        .size(123)
                        .uploadDate(now)
                        .encodedFile(new byte[0])
                        .status(Status.PENDING)
                        .build(),
                AssetDomain.builder()
                        .id(2)
                        .filename("b.png")
                        .contentType("image/png")
                        .url("http://internal-storage/2")
                        .size(456)
                        .uploadDate(now.plusDays(1))
                        .encodedFile(new byte[0])
                        .status(Status.COMPLETED)
                        .build()
        );

        List<Asset> assetsResponse = mapper.toResponse(assetsDomain);

        assertNotNull(assetsResponse);
        assertEquals(2, assetsResponse.size());
        assertEquals("1", assetsResponse.getFirst().getId());
        assertEquals("a.jpg", assetsResponse.getFirst().getFilename());
        assertEquals("image/jpg", assetsResponse.getFirst().getContentType());
        assertEquals("http://internal-storage/1", assetsResponse.getFirst().getUrl());
        assertEquals(123, assetsResponse.getFirst().getSize());
        assertEquals(now.toString(), assetsResponse.getFirst().getUploadDate());

        assertEquals("2", assetsResponse.get(1).getId());
        assertEquals("b.png", assetsResponse.get(1).getFilename());
        assertEquals("image/png", assetsResponse.get(1).getContentType());
        assertEquals("http://internal-storage/2", assetsResponse.get(1).getUrl());
        assertEquals(456, assetsResponse.get(1).getSize());
        assertEquals(now.plusDays(1).toString(), assetsResponse.get(1).getUploadDate());
    }
}
