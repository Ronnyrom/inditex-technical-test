package com.ronnyrom.techincaltestinditex.application.port.out;

import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import com.ronnyrom.techincaltestinditex.domain.model.AssetFilter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveAssetRepositoryPort {
    Mono<AssetDomain> save(AssetDomain asset);
    Flux<AssetDomain> findByFilter(AssetFilter filter);
}