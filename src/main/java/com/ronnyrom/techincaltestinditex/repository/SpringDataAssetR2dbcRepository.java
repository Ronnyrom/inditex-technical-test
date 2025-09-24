package com.ronnyrom.techincaltestinditex.repository;

import com.ronnyrom.techincaltestinditex.adapter.out.r2dbc.entity.AssetEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface SpringDataAssetR2dbcRepository extends ReactiveCrudRepository<AssetEntity, Integer> {
}
