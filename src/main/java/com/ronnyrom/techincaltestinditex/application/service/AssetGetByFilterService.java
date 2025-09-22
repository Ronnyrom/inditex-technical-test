package com.ronnyrom.techincaltestinditex.application.service;

import com.ronnyrom.techincaltestinditex.application.port.in.AssetGetByFilterUseCase;
import com.ronnyrom.techincaltestinditex.application.port.out.AssetRepositoryPort;
import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import com.ronnyrom.techincaltestinditex.domain.model.AssetFilter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssetGetByFilterService implements AssetGetByFilterUseCase {
    private final AssetRepositoryPort assetRepositoryPort;

    public AssetGetByFilterService(AssetRepositoryPort assetRepositoryPort) {
        this.assetRepositoryPort = assetRepositoryPort;
    }
    @Override
    public List<AssetDomain> getByFilter(AssetFilter filter) {
        return assetRepositoryPort.getByFilter(filter);
    }
}
