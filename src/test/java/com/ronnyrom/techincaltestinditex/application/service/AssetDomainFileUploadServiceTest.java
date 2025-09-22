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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AssetDomainFileUploadServiceTest {

    @Mock
    private AssetRepositoryPort assetRepositoryPort;

    @Mock
    private StoragePort storagePort;

    private AssetFileUploadService assetFileUploadService;

    @Captor
    private ArgumentCaptor<AssetDomain> assetCaptor;

    @BeforeEach
    void setUp() {
        assetFileUploadService = new AssetFileUploadService(assetRepositoryPort, storagePort, assetFileUploadService);
    }

    @Test
    void uploadAssetFile_shouldCompleteSuccessfully() throws InterruptedException {

        //given
        AssetDomain assetDomain = generateAsset(null, null);
        AssetDomain pendingAssetDomain = generateAsset(Status.PENDING, 1);

        when(assetRepositoryPort.save(any(AssetDomain.class))).thenReturn(pendingAssetDomain);
        when(storagePort.uploadAsync(any(AssetDomain.class)))
                .thenReturn(CompletableFuture.supplyAsync(() -> "http://storage.com/asset-1",
                        CompletableFuture.delayedExecutor(250, TimeUnit.MILLISECONDS)));

        //When
        AssetDomain result = assetFileUploadService.uploadAssetFile(assetDomain);
        //then
        assertEquals(Status.PENDING, result.getStatus());
        assertEquals(1, result.getId());
        Thread.sleep(300);

        verify(assetRepositoryPort, times(2)).save(assetCaptor.capture());
        List<AssetDomain> assetDomains = assetCaptor.getAllValues();


        AssetDomain firstSave = assetDomains.getFirst();
        assertEquals(Status.PENDING, firstSave.getStatus());
        assertNull(firstSave.getId());

        AssetDomain secondSave = assetDomains.get(1);
        assertEquals(Status.COMPLETED, secondSave.getStatus());
        assertEquals(1, secondSave.getId());
        assertEquals("http://storage.com/asset-1", secondSave.getUrl());

    }

    @Test
    void uploadAssetFile_shouldSetPendingAndSaveOnce(){
        //given
        AssetDomain assetDomain = generateAsset(null, null);
        AssetDomain pendingAssetDomain = generateAsset(Status.PENDING, 1);
        //when
        when(assetRepositoryPort.save(any(AssetDomain.class))).thenReturn(pendingAssetDomain);
        when(storagePort.uploadAsync(any(AssetDomain.class))).thenReturn(CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("Storage error");
        }, CompletableFuture.delayedExecutor(100, TimeUnit.MILLISECONDS)))
        ;
        //then
        AssetDomain result = assetFileUploadService.uploadAssetFile(assetDomain);
        assertEquals(Status.PENDING, result.getStatus());
        assertEquals(1, result.getId());

        verify(assetRepositoryPort, times(1)).save(assetCaptor.capture());

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
            assertEquals("500 INTERNAL_SERVER_ERROR \"Error interno al guardar el asset\"", e.getMessage());
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
