package com.ronnyrom.techincaltestinditex.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "assets")
@Builder
@Getter
@AllArgsConstructor
public class AssetEntity {
    public AssetEntity() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "filename", nullable = false, length = 255)
    private String filename;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "url", length = 500)
    private String url;

    @Column(name = "size", nullable = false)
    private Integer size;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;
}
