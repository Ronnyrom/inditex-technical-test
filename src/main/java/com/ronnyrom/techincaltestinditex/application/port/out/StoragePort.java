package com.ronnyrom.techincaltestinditex.application.port.out;

import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;

import java.util.concurrent.CompletableFuture;

public interface StoragePort {
    CompletableFuture<String> uploadAsync(AssetDomain assetDomain);

}
