package com.ronnyrom.techincaltestinditex.application.service;

import com.ronnyrom.techincaltestinditex.application.port.in.AssetFileUploadUseCase;
import com.ronnyrom.techincaltestinditex.application.port.out.StoragePort;
import com.ronnyrom.techincaltestinditex.application.port.out.AssetRepositoryPort;
import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import com.ronnyrom.techincaltestinditex.domain.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

@Service
public class AssetFileUploadService implements AssetFileUploadUseCase {

    private static final Logger log = LoggerFactory.getLogger(AssetFileUploadService.class);

    private final AssetRepositoryPort assetRepositoryPort;
    private final StoragePort storagePort;
    private final Executor virtualThreadExecutor;
    private final AssetFileUploadService self;

    public AssetFileUploadService(AssetRepositoryPort assetRepositoryPort, StoragePort storagePort,
                                  Executor virtualThreadExecutor,
                                  @Lazy AssetFileUploadService self) {
        this.assetRepositoryPort = assetRepositoryPort;
        this.storagePort = storagePort;
        this.virtualThreadExecutor = virtualThreadExecutor;
        this.self = (self != null) ? self : this;
    }

    @Override
    public AssetDomain uploadAssetFile(AssetDomain assetDomain) {
        try {
            assetDomain.setStatus(Status.PENDING);
            AssetDomain saved = assetRepositoryPort.save(assetDomain);
            self.uploadAsyncWithResilience(saved);
            return saved;
        } catch (Exception e) {
            log.error("Error in uploadAssetFile", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error saving the asset");
        }
    }

    @CircuitBreaker(name = "storageService", fallbackMethod = "storageFallback")
    @Retry(name = "storageService")
    public CompletionStage<Void> uploadAsyncWithResilience(AssetDomain saved) {
        return storagePort.uploadAsync(saved)
                .thenApplyAsync(url  -> {
                    if (url == null || url.isBlank()) {
                        throw new IllegalStateException("URL from storage is empty");
                    }
                    log.debug("Processing completed asset upload storage with url {}", url);
                    saved.setStatus(Status.COMPLETED);
                    saved.setUrl(url);
                    assetRepositoryPort.save(saved);
                    return null;
                }, virtualThreadExecutor);
    }

    private CompletionStage<Void> storageFallback(AssetDomain assetDomain, Throwable t) {
        log.error("Storage service fallback triggered for asset {}", assetDomain.getId(), t);
        assetDomain.setStatus(Status.FAILED);
        assetDomain.setUrl(null);
        assetRepositoryPort.save(assetDomain);
        return CompletableFuture.completedFuture(null);

    }
}
