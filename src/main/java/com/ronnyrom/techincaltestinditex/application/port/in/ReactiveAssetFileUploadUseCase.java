package com.ronnyrom.techincaltestinditex.application.port.in;

import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import reactor.core.publisher.Mono;

public interface ReactiveAssetFileUploadUseCase {
    Mono<AssetDomain> uploadAssetFile(AssetDomain asset);

}
