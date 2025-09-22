package com.ronnyrom.techincaltestinditex.adapter.out.storage;

import com.ronnyrom.techincaltestinditex.application.port.out.StoragePort;
import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class StorageClientAdapter implements StoragePort {
    @Override
    @Async("asyncExecutor")
    public CompletableFuture<String> uploadAsync(AssetDomain assetDomain) {
        try {
            // Simula llamada externa
            Thread.sleep(10000L);
            return CompletableFuture.completedFuture("http://internal_storage/"+ assetDomain.getId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.failedFuture(e);
        }
    }
}
