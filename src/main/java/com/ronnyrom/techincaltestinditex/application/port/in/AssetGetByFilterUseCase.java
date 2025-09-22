package com.ronnyrom.techincaltestinditex.application.port.in;

import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import com.ronnyrom.techincaltestinditex.domain.model.AssetFilter;

import java.util.List;

public interface AssetGetByFilterUseCase {
    List<AssetDomain> getByFilter(AssetFilter filter);
}
