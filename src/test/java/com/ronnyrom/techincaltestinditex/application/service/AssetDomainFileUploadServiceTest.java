package com.ronnyrom.techincaltestinditex.application.service;

import com.ronnyrom.techincaltestinditex.application.port.out.StoragePort;
import com.ronnyrom.techincaltestinditex.application.port.out.AssetRepositoryPort;
import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import com.ronnyrom.techincaltestinditex.domain.model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AssetDomainFileUploadServiceTest {

    @Mock
    private AssetRepositoryPort assetRepositoryPort;

    @Mock
    private StoragePort storagePort;

    private AssetFileUploadService assetFileUploadService;

    private Executor virtualThreadExecutor;

    @BeforeEach
    void setUp() {
        virtualThreadExecutor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor();
        assetFileUploadService = new AssetFileUploadService(assetRepositoryPort, storagePort, virtualThreadExecutor, assetFileUploadService);
    }

    @Test
    void uploadAssetFile_shouldCompleteSuccessfully() throws InterruptedException {
        // given
        AssetDomain assetDomain = generateAsset(null, null);
        AssetDomain pendingAssetDomain = generateAsset(Status.PENDING, 1);
        // when
        when(assetRepositoryPort.save(any(AssetDomain.class))).thenReturn(pendingAssetDomain);
        when(storagePort.uploadAsync(any(AssetDomain.class)))
                .thenReturn(CompletableFuture.completedFuture("http://storage.com/asset-1"));

        AssetDomain result = assetFileUploadService.uploadAssetFile(assetDomain);
        Thread.sleep(200);

        // then
        assertEquals(Status.COMPLETED, result.getStatus());
        assertEquals(1, result.getId());
        verify(assetRepositoryPort, times(2)).save(any(AssetDomain.class));
        verify(storagePort, times(1)).uploadAsync(any(AssetDomain.class));
    }

    @Test
    void uploadAssetFile_shouldReturnInternalServerErrorWhenRepositoryFails() {
        //given
        AssetDomain assetDomain = generateAsset(null, null);
        //when
        when(assetRepositoryPort.save(any(AssetDomain.class))).thenThrow(new RuntimeException("Database error"));

        try {
            assetFileUploadService.uploadAssetFile(assetDomain);
        } catch (Exception e) {
            assertEquals("500 INTERNAL_SERVER_ERROR \"Internal error saving the asset\"", e.getMessage());
        }
        verify(assetRepositoryPort, times(1)).save(any(AssetDomain.class));
        verifyNoInteractions(storagePort);
    }

    private AssetDomain generateAsset(Status status, Integer id) {
        byte[] encodedFile = "test-content-base64".getBytes();
        return AssetDomain.builder()
                .id(id)
                .filename("filename.jpg")
                .encodedFile(encodedFile)
                .url(null)
                .contentType("image/jpg")
                .size(378)
                .status(status)
                .build();
    }
}
