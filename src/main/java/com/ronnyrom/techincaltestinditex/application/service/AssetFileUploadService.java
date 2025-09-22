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

@Service
public class AssetFileUploadService implements AssetFileUploadUseCase {

    private static final Logger log = LoggerFactory.getLogger(AssetFileUploadService.class);

    private final AssetRepositoryPort assetRepositoryPort;
    private final StoragePort storagePort;

    // Proxy del propio bean para evitar auto-invocación
    private final AssetFileUploadService self;

    public AssetFileUploadService(AssetRepositoryPort assetRepositoryPort, StoragePort storagePort,
                                  @Lazy AssetFileUploadService self) {
        this.assetRepositoryPort = assetRepositoryPort;
        this.storagePort = storagePort;
        this.self = (self != null) ? self : this;
    }

    @Override

    public AssetDomain uploadAssetFile(AssetDomain assetDomain) {
        try {
            assetDomain.setStatus(Status.PENDING);
            AssetDomain saved = assetRepositoryPort.save(assetDomain);
            //uploadAsyncWithResilience(saved);
            self.uploadAsyncWithResilience(saved);
//            storagePort.uploadAsync(saved).whenComplete((url, throwable) -> {
//                try {
//                    if (throwable == null && url != null && !url.isEmpty()) {
//                        log.debug("Processing completed assed upload storage with url {}", url);
//                        saved.setStatus(Status.COMPLETED);
//                        saved.setUrl(url);
//                    } else {
//                        log.error("Error processing upload storage with url {}", url, throwable);
//                        saved.setStatus(Status.FAILED);
//                    }
//                    assetRepositoryPort.save(saved);
//
//                } catch (Exception e) {
//                    log.error("Error processing upload storage with id {}", saved.getId());
//                }
//            });

            return saved;
        } catch (Exception e) {
            log.error("Error en uploadAssetFile", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al guardar el asset");
        }
    }

    @CircuitBreaker(name = "storageService", fallbackMethod = "storageFallback")
    @Retry(name = "storageService")
    public CompletionStage<Void> uploadAsyncWithResilience(AssetDomain saved) {
        return storagePort.uploadAsync(saved)
                .thenApply(url -> {
                    if (url == null || url.isBlank()) {
                        throw new IllegalStateException("URL devuelta por storage vacía");
                    }
                    log.debug("Processing completed asset upload storage with url {}", url);
                    saved.setStatus(Status.COMPLETED);
                    saved.setUrl(url);
                    assetRepositoryPort.save(saved);
                    return null;
                });
        // No usar whenComplete/exceptionally: dejar propagar el fallo para Retry/CircuitBreaker
    }
    // Fallback: misma firma (parámetros + Throwable) y mismo tipo de retorno
    @SuppressWarnings("unused")
    private CompletionStage<Void> storageFallback(AssetDomain assetDomain, Throwable t) {
        log.error("Storage service fallback triggered for asset {}", assetDomain.getId(), t);
        assetDomain.setStatus(Status.FAILED);
        assetDomain.setUrl(null);
        assetRepositoryPort.save(assetDomain);
        return CompletableFuture.completedFuture(null);

    }
}
