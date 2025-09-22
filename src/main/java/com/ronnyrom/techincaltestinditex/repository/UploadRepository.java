package com.ronnyrom.techincaltestinditex.repository;

import com.ronnyrom.techincaltestinditex.adapter.out.persistence.entity.AssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UploadRepository extends JpaRepository<AssetEntity, Integer>, JpaSpecificationExecutor<AssetEntity> {
}
