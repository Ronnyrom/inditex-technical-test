package com.ronnyrom.techincaltestinditex.adapter.in.web.mapper;

import com.ronnyrom.adapter.in.web.model.Asset;
import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import com.ronnyrom.techincaltestinditex.domain.model.AssetFilter;
import com.ronnyrom.techincaltestinditex.domain.model.SortDirection;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class AssetGetByFilterMapper {
    public AssetFilter toAssetFilter(String uploadDateStart, String uploadDateEnd, String filename, String filetype, String sortDirection) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return AssetFilter.builder()
                .uploadDateStart(uploadDateStart != null && !uploadDateStart.isBlank() ? LocalDateTime.parse(uploadDateStart, formatter) : null)
                .uploadDateEnd(uploadDateEnd != null && !uploadDateEnd.isBlank() ? LocalDateTime.parse(uploadDateEnd, formatter) : null)
                .filename(filename)
                .filetype(filetype)
                .sortDirection(sortDirection != null ? SortDirection.valueOf(sortDirection) : null)
                .build();
    }
    public List<Asset> toResponse(List<AssetDomain> assetDomains) {
        return assetDomains.stream().map(assetDomain -> new Asset()
                .id(assetDomain.getId().toString())
                .filename(assetDomain.getFilename())
                .contentType(assetDomain.getContentType())
                .url(assetDomain.getUrl())
                .size(assetDomain.getSize())
                .uploadDate(assetDomain.getUploadDate().toString())
        ).toList();
    }
}