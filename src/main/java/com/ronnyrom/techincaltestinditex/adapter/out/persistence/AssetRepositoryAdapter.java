package com.ronnyrom.techincaltestinditex.adapter.out.persistence;

import com.ronnyrom.techincaltestinditex.adapter.out.persistence.entity.AssetEntity;
import com.ronnyrom.techincaltestinditex.adapter.out.persistence.mapper.AssetMapper;
import com.ronnyrom.techincaltestinditex.application.port.out.AssetRepositoryPort;
import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import com.ronnyrom.techincaltestinditex.domain.model.AssetFilter;
import com.ronnyrom.techincaltestinditex.domain.model.SortDirection;
import com.ronnyrom.techincaltestinditex.repository.UploadRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class AssetRepositoryAdapter implements AssetRepositoryPort {

    private static final String UPLOAD_DATE = "uploadDate";
    private static final String FILENAME = "filename";
    private static final String CONTENT_TYPE = "contentType";

    private final UploadRepository uploadRepository;
    private final AssetMapper assetMapper;

    public AssetRepositoryAdapter(UploadRepository uploadRepository, AssetMapper assetMapper) {
        this.uploadRepository = uploadRepository;
        this.assetMapper = assetMapper;
    }

    @Override
    public AssetDomain save(AssetDomain assetDomain) {
        return assetMapper.toAsset(uploadRepository.save(assetMapper.toAssetEntity(assetDomain)));
    }

    @Override
    public List<AssetDomain> getByFilter(AssetFilter filter) {
        Specification<AssetEntity> spec = buildSpec(filter);
        Sort sort = buildSort(filter);
        List<AssetEntity> entities = uploadRepository.findAll(spec, sort);
        return entities.stream().map(assetMapper::toAsset).collect(Collectors.toList());
    }

    private Specification<AssetEntity> buildSpec(AssetFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter != null) {
                if (filter.getUploadDateStart() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get(UPLOAD_DATE), filter.getUploadDateStart()));
                }
                if (filter.getUploadDateEnd() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get(UPLOAD_DATE), filter.getUploadDateEnd()));
                }
                if (filter.getFilename() != null && !filter.getFilename().isBlank()) {
                    predicates.add(cb.like(cb.lower(root.get(FILENAME)), "%" + filter.getFilename().toLowerCase() + "%"));
                }
                if (filter.getFiletype() != null && !filter.getFiletype().isBlank()) {
                    predicates.add(cb.equal(cb.lower(root.get(CONTENT_TYPE)), filter.getFiletype().toLowerCase()));
                }
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));

        };
    }

    private Sort buildSort(AssetFilter filter) {
        if (filter == null || filter.getSortDirection() == null) {
            return Sort.by(Sort.Direction.DESC, UPLOAD_DATE);
        }

        Sort.Direction direction = (filter.getSortDirection() == SortDirection.ASC)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return Sort.by(direction, UPLOAD_DATE);
    }
}
