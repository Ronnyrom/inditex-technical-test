package com.ronnyrom.techincaltestinditex.adapter.out.r2dbc.entity;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("ASSETS")
public class AssetEntity {

    @Id
    private Integer id;

    private String filename;

    @Column("content_type")
    private String contentType;

    private String url;

    private Integer size;

    private String status;

    @Column("upload_date")
    private LocalDateTime uploadDate;
}