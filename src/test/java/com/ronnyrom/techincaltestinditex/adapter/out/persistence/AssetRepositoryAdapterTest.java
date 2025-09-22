package com.ronnyrom.techincaltestinditex.adapter.out.persistence;

import com.ronnyrom.techincaltestinditex.adapter.out.persistence.entity.AssetEntity;
import com.ronnyrom.techincaltestinditex.adapter.out.persistence.mapper.AssetMapper;
import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import com.ronnyrom.techincaltestinditex.domain.model.AssetFilter;
import com.ronnyrom.techincaltestinditex.domain.model.SortDirection;
import com.ronnyrom.techincaltestinditex.repository.UploadRepository;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class AssetRepositoryAdapterTest {
    private UploadRepository uploadRepository;
    private AssetMapper assetMapper;
    private AssetRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        uploadRepository = mock(UploadRepository.class);
        assetMapper = mock(AssetMapper.class);
        adapter = new AssetRepositoryAdapter(uploadRepository, assetMapper);
    }

    @Captor
    private ArgumentCaptor<Specification<AssetEntity>> specCaptor;

    private static AssetFilter generateFilter(SortDirection direction) {
        return AssetFilter.builder().
                uploadDateStart(LocalDateTime.now().minusDays(1)).
                uploadDateEnd(LocalDateTime.now()).
                filename("filename").
                filetype("filetype").
                sortDirection(direction).
                build();
    }

    @Test
    void save_delegates_to_mapper_and_repository() {
        AssetDomain domain = mock(AssetDomain.class);
        AssetEntity toSave = mock(AssetEntity.class);
        AssetEntity saved = mock(AssetEntity.class);
        AssetDomain mappedSaved = mock(AssetDomain.class);

        when(assetMapper.toAssetEntity(domain)).thenReturn(toSave);
        when(uploadRepository.save(toSave)).thenReturn(saved);
        when(assetMapper.toAsset(saved)).thenReturn(mappedSaved);

        AssetDomain result = adapter.save(domain);

        verify(assetMapper).toAssetEntity(domain);
        verify(uploadRepository).save(toSave);
        verify(assetMapper).toAsset(saved);
        assertSame(mappedSaved, result);
    }

    @Test
    void getByFilter_uses_default_desc_sort_when_direction_is_null() {
        AssetFilter filter = generateFilter(null);

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        when(uploadRepository.findAll(
                ArgumentMatchers.<Specification<AssetEntity>>any(),
                sortCaptor.capture()
        )).thenReturn(Collections.emptyList());

        adapter.getByFilter(filter);

        Sort.Order order = sortCaptor.getValue().getOrderFor("uploadDate");
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    void getByFilter_uses_asc_sort_when_requested() {
        AssetFilter filter = generateFilter(SortDirection.ASC);

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        when(uploadRepository.findAll(
                ArgumentMatchers.<Specification<AssetEntity>>any(),
                sortCaptor.capture()
        )).thenReturn(Collections.emptyList());

        adapter.getByFilter(filter);

        Sort.Order order = sortCaptor.getValue().getOrderFor("uploadDate");
        assertNotNull(order);
        assertEquals(Sort.Direction.ASC, order.getDirection());
    }

    @Test
    void getByFilter_uses_desc_sort_when_requested() {
        AssetFilter filter = generateFilter(SortDirection.DESC);

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        when(uploadRepository.findAll(
                ArgumentMatchers.<Specification<AssetEntity>>any(),
                sortCaptor.capture()
        )).thenReturn(Collections.emptyList());

        adapter.getByFilter(filter);

        Sort.Order order = sortCaptor.getValue().getOrderFor("uploadDate");
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    void getByFilter_calls_repository_once_with_spec_and_sort() {
        AssetFilter filter = generateFilter(SortDirection.DESC);

        when(uploadRepository.findAll(
                ArgumentMatchers.<Specification<AssetEntity>>any(),
                any(Sort.class)
        )).thenReturn(Collections.emptyList());

        adapter.getByFilter(filter);

        verify(uploadRepository, times(1))
                .findAll(ArgumentMatchers.<Specification<AssetEntity>>any(), any(Sort.class));
    }

    @Test
    void getByFilter_with_null_filter_uses_default_desc_sort() {
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        when(uploadRepository.findAll(
                ArgumentMatchers.<Specification<AssetEntity>>any(),
                sortCaptor.capture()
        )).thenReturn(Collections.emptyList());

        adapter.getByFilter(null);

        Sort.Order order = sortCaptor.getValue().getOrderFor("uploadDate");
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    void getByFilter_with_all_filter_values_are_null_uses_default_desc_sort() {
        AssetFilter filter = AssetFilter.builder().
                uploadDateStart(null).
                uploadDateEnd(null).
                filename(null).
                filetype(null).
                sortDirection(null).
                build();

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        when(uploadRepository.findAll(
                ArgumentMatchers.<Specification<AssetEntity>>any(),
                sortCaptor.capture()
        )).thenReturn(Collections.emptyList());

        adapter.getByFilter(filter);

        Sort.Order order = sortCaptor.getValue().getOrderFor("uploadDate");
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    void buildSpec_returns_conjunction_when_filter_is_null() {
        when(uploadRepository.findAll(specCaptor.capture(), any(Sort.class)))
                .thenReturn(Collections.emptyList());

        adapter.getByFilter(null);
        Specification<AssetEntity> spec = specCaptor.getValue();
        assertNotNull(spec);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        Root<AssetEntity> root = mock(Root.class);
        Predicate conjunction = mock(Predicate.class);
        when(cb.conjunction()).thenReturn(conjunction);

        Predicate result = spec.toPredicate(root, query, cb);

        assertSame(conjunction, result);
        verify(cb).conjunction();
        verify(root, never()).get(anyString());
    }

    @Test
    void buildSpec_applies_all_predicates_when_all_filter_fields_present() {
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        LocalDateTime end = LocalDateTime.now().minusDays(1);
        String filename = "FiLe-Name";
        String filetype = "IMAGE/JPEG";

        AssetFilter filter = AssetFilter.builder()
                .uploadDateStart(start)
                .uploadDateEnd(end)
                .filename(filename)
                .filetype(filetype)
                .sortDirection(SortDirection.ASC)
                .build();

        when(uploadRepository.findAll(specCaptor.capture(), any(Sort.class)))
                .thenReturn(Collections.emptyList());

        adapter.getByFilter(filter);
        Specification<AssetEntity> spec = specCaptor.getValue();
        assertNotNull(spec);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        Root<AssetEntity> root = mock(Root.class);

        Predicate pAnd = mock(Predicate.class);
        when(cb.and(any(Predicate[].class))).thenReturn(pAnd);

        Predicate result = spec.toPredicate(root, query, cb);

        assertSame(pAnd, result);

        verify(root, times(2)).get("uploadDate");
        verify(root).get("filename");
        verify(root).get("contentType");

        verify(cb).greaterThanOrEqualTo(ArgumentMatchers.<Expression<LocalDateTime>>any(), eq(start));
        verify(cb).lessThanOrEqualTo(ArgumentMatchers.<Expression<LocalDateTime>>any(), eq(end));
        verify(cb).like(ArgumentMatchers.<Expression<String>>any(), eq("%" + filename.toLowerCase() + "%"));
        verify(cb).equal(ArgumentMatchers.<Expression<String>>any(), eq(filetype.toLowerCase()));
        verify(cb).and(any(Predicate[].class));
    }

    @Test
    void buildSpec_returns_conjunction_when_all_filter_fields_blank_or_null() {
        AssetFilter filter = AssetFilter.builder()
                .uploadDateStart(null)
                .uploadDateEnd(null)
                .filename("   ")
                .filetype("  ")
                .sortDirection(null)
                .build();

        when(uploadRepository.findAll(specCaptor.capture(), any(Sort.class)))
                .thenReturn(Collections.emptyList());

        adapter.getByFilter(filter);
        Specification<AssetEntity> spec = specCaptor.getValue();
        assertNotNull(spec);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        Root<AssetEntity> root = mock(Root.class);
        Predicate conjunction = mock(Predicate.class);
        when(cb.conjunction()).thenReturn(conjunction);

        Predicate result = spec.toPredicate(root, query, cb);

        assertSame(conjunction, result);
        verify(cb).conjunction();
        verify(root, never()).get(anyString());
    }
}
