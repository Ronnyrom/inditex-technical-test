package com.ronnyrom.techincaltestinditex.adapter.out.persistence.mapper;

import com.ronnyrom.techincaltestinditex.adapter.out.persistence.entity.AssetEntity;
import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import com.ronnyrom.techincaltestinditex.domain.model.Status;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AssetMapper {
    public AssetEntity toAssetEntity(AssetDomain assetDomain) {
        return AssetEntity.builder()
                .id(assetDomain.getId())
                .filename(assetDomain.getFilename())
                .contentType(assetDomain.getContentType())
                .url(assetDomain.getUrl())
                .size(assetDomain.getSize())
                .status(assetDomain.getStatus().name())
                .uploadDate(LocalDateTime.now())
                .build();
    }

    public AssetDomain toAsset(AssetEntity assetEntity) {
        return AssetDomain.builder()
                .id(assetEntity.getId())
                .filename(assetEntity.getFilename())
                .contentType(assetEntity.getContentType())
                .url(assetEntity.getUrl())
                .size(assetEntity.getSize())
                .status(Status.valueOf(assetEntity.getStatus()))
                .uploadDate(assetEntity.getUploadDate())
                .build();
    }
}
