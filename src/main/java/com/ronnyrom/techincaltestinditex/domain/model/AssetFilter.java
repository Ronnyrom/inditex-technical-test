package com.ronnyrom.techincaltestinditex.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class AssetFilter {
    private LocalDateTime uploadDateStart;
    private LocalDateTime uploadDateEnd;
    private String filename;
    private String filetype;
    private SortDirection sortDirection;
}
