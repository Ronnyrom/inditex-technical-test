package com.ronnyrom.techincaltestinditex.adapter.out.r2dbc;

import com.ronnyrom.techincaltestinditex.adapter.out.r2dbc.entity.AssetEntity;
import com.ronnyrom.techincaltestinditex.adapter.out.r2dbc.mapper.AssetR2dbcMapper;
import com.ronnyrom.techincaltestinditex.application.port.out.ReactiveAssetRepositoryPort;
import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import com.ronnyrom.techincaltestinditex.domain.model.AssetFilter;
import com.ronnyrom.techincaltestinditex.domain.model.SortDirection;
import com.ronnyrom.techincaltestinditex.repository.SpringDataAssetR2dbcRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class AssetR2dbcRepositoryAdapter implements ReactiveAssetRepositoryPort {

    private final SpringDataAssetR2dbcRepository repo;
    private final DatabaseClient db;
    private final AssetR2dbcMapper mapper = new AssetR2dbcMapper();

    public AssetR2dbcRepositoryAdapter(SpringDataAssetR2dbcRepository repo, DatabaseClient db) {
        this.repo = repo;
        this.db = db;
    }

    @Override
    public Mono<AssetDomain> save(AssetDomain asset) {
        return repo.save(mapper.toEntity(asset)).map(mapper::toDomain);
    }

    @Override
    public Flux<AssetDomain> findByFilter(AssetFilter f) {
        StringBuilder sql = new StringBuilder("SELECT * FROM assets WHERE 1=1");
        if (f.getUploadDateStart() != null) sql.append(" AND upload_date >= :uds");
        if (f.getUploadDateEnd() != null) sql.append(" AND upload_date <= :ude");
        if (f.getFilename() != null && !f.getFilename().isBlank()) sql.append(" AND filename LIKE :fn");
        if (f.getFiletype() != null && !f.getFiletype().isBlank()) sql.append(" AND content_type = :ct");
        if (f.getSortDirection() != null) {
            sql.append(" ORDER BY upload_date ").append(f.getSortDirection() == SortDirection.DESC ? "DESC" : "ASC");
        }
        var spec = db.sql(sql.toString());
        if (f.getUploadDateStart() != null) spec = spec.bind("uds", f.getUploadDateStart());
        if (f.getUploadDateEnd() != null) spec = spec.bind("ude", f.getUploadDateEnd());
        if (f.getFilename() != null && !f.getFilename().isBlank()) spec = spec.bind("fn", "%" + f.getFilename() + "%");
        if (f.getFiletype() != null && !f.getFiletype().isBlank()) spec = spec.bind("ct", f.getFiletype());

        return spec.map((row, meta) -> {
                    AssetEntity e = new AssetEntity();
                    e.setId((Integer) row.get("id"));
                    e.setFilename((String) row.get("filename"));
                    e.setContentType((String) row.get("content_type"));
                    e.setUrl((String) row.get("url"));
                    e.setSize(row.get("size", Long.class) != null ? row.get("size", Long.class).intValue() : null);
                    e.setUploadDate(row.get("upload_date", java.time.LocalDateTime.class));
                    e.setStatus((String) row.get("status"));
                    return e;
                })
                .all()
                .map(mapper::toDomain);
    }
}