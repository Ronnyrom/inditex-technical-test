package com.ronnyrom.techincaltestinditex.adapter.in.web.mapper;

import com.ronnyrom.adapter.in.web.model.Asset;
import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import com.ronnyrom.techincaltestinditex.domain.model.AssetFilter;
import com.ronnyrom.techincaltestinditex.domain.model.SortDirection;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class AssetGetByFilterMapper {

    public AssetFilter toAssetFilter(String uploadDateStart, String uploadDateEnd, String filename, String filetype, String sortDirection) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return AssetFilter.builder()
                .uploadDateStart(uploadDateStart != null && !uploadDateStart.isBlank() ? LocalDateTime.parse(uploadDateStart, formatter) : null)
                .uploadDateEnd(uploadDateEnd != null && !uploadDateEnd.isBlank() ? LocalDateTime.parse(uploadDateEnd, formatter) : null)
                .filename(filename)
                .filetype(filetype)
                .sortDirection(sortDirection != null && !sortDirection.isBlank() ? SortDirection.valueOf(sortDirection) : null)
                .build();
    }

    public Asset toResponse(AssetDomain assetDomain) {
        return new Asset()
                .id(assetDomain.getId() != null ? assetDomain.getId().toString() : null)
                .filename(assetDomain.getFilename())
                .contentType(assetDomain.getContentType())
                .url(assetDomain.getUrl())
                .size(assetDomain.getSize())
                .uploadDate(assetDomain.getUploadDate() != null ? assetDomain.getUploadDate().toString() : null);
    }
}