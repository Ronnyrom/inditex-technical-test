package com.ronnyrom.techincaltestinditex.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class AssetDomain {
    private Integer id;
    private String filename;
    private byte[] encodedFile;
    private String contentType;
    private String url;
    private Integer size;
    private LocalDateTime uploadDate;
    private Status status;
}
