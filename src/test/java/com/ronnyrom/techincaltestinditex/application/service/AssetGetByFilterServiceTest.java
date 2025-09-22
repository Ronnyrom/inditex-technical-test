package com.ronnyrom.techincaltestinditex.application.service;

import com.ronnyrom.techincaltestinditex.application.port.out.AssetRepositoryPort;
import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import com.ronnyrom.techincaltestinditex.domain.model.AssetFilter;
import com.ronnyrom.techincaltestinditex.domain.model.SortDirection;
import com.ronnyrom.techincaltestinditex.domain.model.Status;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class AssetGetByFilterServiceTest {

    @Test
    void getByFilter_callsRepositoryAndReturnsResult() {
        AssetRepositoryPort repo = mock(AssetRepositoryPort.class);
        AssetGetByFilterService service = new AssetGetByFilterService(repo);
        AssetFilter filter = generateFilter();
        List<AssetDomain> expected =generateListAsset();
        when(repo.getByFilter(filter)).thenReturn(expected);

        List<AssetDomain> result = service.getByFilter(filter);

        assertEquals(expected, result);
        verify(repo).getByFilter(filter);
    }

    @Test
    void getByFilter_returnsEmptyListIfNoResults() {
        AssetRepositoryPort repo = mock(AssetRepositoryPort.class);
        AssetGetByFilterService service = new AssetGetByFilterService(repo);
        AssetFilter filter = generateFilter();
        when(repo.getByFilter(filter)).thenReturn(List.of());

        List<AssetDomain> result = service.getByFilter(filter);

        assertTrue(result.isEmpty());
    }

    private AssetFilter generateFilter() {
        return AssetFilter.builder()
                .uploadDateStart(LocalDateTime.parse("2024-09-17T00:00:00"))
                .uploadDateEnd(LocalDateTime.parse("2026-09-17T00:00:00"))
                .filename("IMG.png")
                .filetype("image/jpeg")
                .sortDirection(SortDirection.ASC).build();
    }
    private List<AssetDomain> generateListAsset() {
        byte[] encodedFile = "test-content-base64".getBytes();
        return List.of(AssetDomain.builder()
                .id(1)
                .filename("filename.jpg")
                .encodedFile(encodedFile)
                .url("url")
                .contentType("image/jpg")
                .size(378)
                .status(Status.COMPLETED)
                .build());
    }
}
