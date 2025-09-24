package com.ronnyrom.techincaltestinditex.application.service;


import com.ronnyrom.techincaltestinditex.application.port.out.ReactiveAssetRepositoryPort;
import com.ronnyrom.techincaltestinditex.application.port.out.ReactiveStoragePort;
import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import com.ronnyrom.techincaltestinditex.domain.model.Status;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ResilientStorageService {
    private static final Logger log = LoggerFactory.getLogger(ResilientStorageService.class);

    private final ReactiveStoragePort storage;
    private final ReactiveAssetRepositoryPort repo;

    public ResilientStorageService(ReactiveStoragePort storage, ReactiveAssetRepositoryPort repo) {
        this.storage = storage;
        this.repo = repo;
    }

    @CircuitBreaker(name = "storageService", fallbackMethod = "storageFallback")
    public Mono<Void> uploadAndPersist(AssetDomain asset) {
        return storage.uploadAsync(asset)
                .flatMap(url -> {
                    if (url == null || url.isBlank()) {
                        return Mono.error(new IllegalStateException("URL from storage is empty"));
                    }
                    asset.setStatus(Status.COMPLETED);
                    asset.setUrl(url);
                    return repo.save(asset).then();
                });
    }

    private Mono<Void> storageFallback(AssetDomain asset, Throwable t) {
        log.error("Storage service fallback for asset {}", asset.getId(), t);
        asset.setStatus(Status.FAILED);
        asset.setUrl(null);
        return repo.save(asset).then();
    }
}