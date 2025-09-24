package com.ronnyrom.techincaltestinditex.adapter.out.r2dbc.mapper;

import com.ronnyrom.techincaltestinditex.adapter.out.r2dbc.entity.AssetEntity;
import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import com.ronnyrom.techincaltestinditex.domain.model.Status;

public class AssetR2dbcMapper {
    public AssetEntity toEntity(AssetDomain assetDomain) {
        return AssetEntity.builder()
                .id(assetDomain.getId())
                .filename(assetDomain.getFilename())
                .contentType(assetDomain.getContentType())
                .url(assetDomain.getUrl())
                .size(assetDomain.getSize())
                .uploadDate(assetDomain.getUploadDate())
                .status(assetDomain.getStatus() != null ? assetDomain.getStatus().name() : null)
                .build();
    }

    public AssetDomain toDomain(AssetEntity assetEntity) {
        return AssetDomain.builder()
                .id(assetEntity.getId())
                .filename(assetEntity.getFilename())
                .contentType(assetEntity.getContentType())
                .url(assetEntity.getUrl())
                .size(assetEntity.getSize())
                .uploadDate(assetEntity.getUploadDate())
                .status(assetEntity.getStatus() != null ? Status.valueOf(assetEntity.getStatus()) : null)
                .build();
    }
}