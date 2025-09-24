package com.ronnyrom.techincaltestinditex.application.port.out;

import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import reactor.core.publisher.Mono;

public interface ReactiveStoragePort {
    Mono<String> uploadAsync(AssetDomain asset);
}