package com.ronnyrom.techincaltestinditex.adapter.out.storage;

import com.ronnyrom.techincaltestinditex.application.port.out.ReactiveStoragePort;
import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class ReactiveThirdPartyStorageAdapter implements ReactiveStoragePort {

    @Override
    public Mono<String> uploadAsync(AssetDomain asset) {
        return Mono.fromCallable(() -> {
                    Thread.sleep(600);
                    String safeName = asset.getFilename() != null ? asset.getFilename().replaceAll("\\s+", "-") : "file";
                    return "https://internal.storage/assets/" + asset.getId() + "/" + safeName;
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
}