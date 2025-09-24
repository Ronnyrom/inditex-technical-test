package com.ronnyrom.techincaltestinditex.application.port.in;

import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import com.ronnyrom.techincaltestinditex.domain.model.AssetFilter;
import reactor.core.publisher.Flux;

public interface ReactiveAssetGetByFilterUseCase {
    Flux<AssetDomain> findByFilter(AssetFilter filter);

}
