package com.ronnyrom.techincaltestinditex.application.service;

import com.ronnyrom.techincaltestinditex.application.port.in.ReactiveAssetFileUploadUseCase;
import com.ronnyrom.techincaltestinditex.application.port.out.ReactiveAssetRepositoryPort;
import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import com.ronnyrom.techincaltestinditex.domain.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class ReactiveAssetFileUploadService implements ReactiveAssetFileUploadUseCase {
    private static final Logger log = LoggerFactory.getLogger(ReactiveAssetFileUploadService.class);

    private final ReactiveAssetRepositoryPort repo;
    private final ResilientStorageService resilientStorage;

    public ReactiveAssetFileUploadService(ReactiveAssetRepositoryPort repo, ResilientStorageService resilientStorage) {
        this.repo = repo;
        this.resilientStorage = resilientStorage;
    }

    public Mono<AssetDomain> uploadAssetFile(AssetDomain asset) {
        asset.setStatus(Status.PENDING);
        if (asset.getUploadDate() == null) {
            asset.setUploadDate(LocalDateTime.now());
        }
        return repo.save(asset)
                .onErrorMap(e -> {
                    log.error("Error saving asset", e);
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error saving the asset");
                })
                .doOnSuccess(saved -> resilientStorage.uploadAndPersist(saved).subscribe(
                        v -> log.debug("Post-upload processed for asset {}", saved.getId()),
                        ex -> log.error("Post-upload failed for asset {}", saved.getId(), ex)
                ));
    }
}
