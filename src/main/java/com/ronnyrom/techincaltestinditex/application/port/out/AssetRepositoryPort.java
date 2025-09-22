package com.ronnyrom.techincaltestinditex.application.port.out;

import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import com.ronnyrom.techincaltestinditex.domain.model.AssetFilter;

import java.util.List;

public interface AssetRepositoryPort {
    AssetDomain save(AssetDomain assetDomain);
    List<AssetDomain> getByFilter(AssetFilter filter);
}
