package com.ronnyrom.techincaltestinditex.application.service;

import com.ronnyrom.techincaltestinditex.application.port.in.ReactiveAssetGetByFilterUseCase;
import com.ronnyrom.techincaltestinditex.application.port.out.ReactiveAssetRepositoryPort;
import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import com.ronnyrom.techincaltestinditex.domain.model.AssetFilter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ReactiveAssetGetByFilterService implements ReactiveAssetGetByFilterUseCase {

    private final ReactiveAssetRepositoryPort repo;

    public ReactiveAssetGetByFilterService(ReactiveAssetRepositoryPort repo) {
        this.repo = repo;
    }

    @Override
    public Flux<AssetDomain> findByFilter(AssetFilter filter) {
        return repo.findByFilter(filter);
    }
}