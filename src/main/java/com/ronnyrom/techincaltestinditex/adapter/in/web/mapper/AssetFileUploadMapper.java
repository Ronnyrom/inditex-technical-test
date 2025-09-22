package com.ronnyrom.techincaltestinditex.adapter.in.web.mapper;

import com.ronnyrom.adapter.in.web.model.AssetFileUploadRequest;
import com.ronnyrom.adapter.in.web.model.AssetFileUploadResponse;
import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import org.springframework.stereotype.Component;


@Component
public class AssetFileUploadMapper {

    public AssetDomain toDomain(AssetFileUploadRequest assetFileUploadRequest) {
        return AssetDomain.builder().
                filename(assetFileUploadRequest.getFilename())
                .encodedFile(assetFileUploadRequest.getEncodedFile())
                .contentType(assetFileUploadRequest.getContentType())
                .size(assetFileUploadRequest.getEncodedFile().length)
                .build();
    }

    public AssetFileUploadResponse toResponse(AssetDomain assetDomain) {
        return new AssetFileUploadResponse().id(assetDomain.getId().toString());
    }
}