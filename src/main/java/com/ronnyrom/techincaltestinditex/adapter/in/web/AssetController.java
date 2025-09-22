package com.ronnyrom.techincaltestinditex.adapter.in.web;

import com.ronnyrom.adapter.in.web.AssetApi;
import com.ronnyrom.adapter.in.web.model.Asset;
import com.ronnyrom.adapter.in.web.model.AssetFileUploadRequest;
import com.ronnyrom.adapter.in.web.model.AssetFileUploadResponse;
import com.ronnyrom.techincaltestinditex.adapter.in.web.mapper.AssetFileUploadMapper;
import com.ronnyrom.techincaltestinditex.adapter.in.web.mapper.AssetGetByFilterMapper;
import com.ronnyrom.techincaltestinditex.adapter.in.web.validation.AssetFilterValidator;
import com.ronnyrom.techincaltestinditex.adapter.in.web.validation.AssetUploadValidator;
import com.ronnyrom.techincaltestinditex.application.port.in.AssetFileUploadUseCase;
import com.ronnyrom.techincaltestinditex.application.port.in.AssetGetByFilterUseCase;
import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
public class AssetController implements AssetApi {

    private final AssetFileUploadUseCase assetFileUploadUseCase;
    private final AssetFileUploadMapper assetFileUploadMapper;
    private final AssetGetByFilterMapper assetGetByFilterMapper;
    private final AssetGetByFilterUseCase assetGetByFilterUseCase;
    private final AssetFilterValidator assetFilterValidator;
    private final AssetUploadValidator assetUploadValidator;

    public AssetController(AssetFileUploadUseCase assetFileUploadUseCase, AssetFileUploadMapper assetFileUploadMapper, AssetGetByFilterUseCase assetGetByFilterUseCase, AssetGetByFilterMapper assetGetByFilterMapper, AssetFilterValidator assetFilterValidator, AssetUploadValidator assetUploadValidator) {
        this.assetFileUploadUseCase = assetFileUploadUseCase;
        this.assetFileUploadMapper = assetFileUploadMapper;
        this.assetGetByFilterUseCase = assetGetByFilterUseCase;
        this.assetGetByFilterMapper = assetGetByFilterMapper;
        this.assetFilterValidator = assetFilterValidator;
        this.assetUploadValidator = assetUploadValidator;
    }

    @Override
    public ResponseEntity<AssetFileUploadResponse> uploadAssetFile(@Valid @RequestBody AssetFileUploadRequest request) {
        AssetUploadValidator.AssetFileUploadRequest params = new AssetUploadValidator.AssetFileUploadRequest(
                request.getFilename(),
                request.getEncodedFile() != null ? Arrays.toString(request.getEncodedFile()) : null,
                request.getContentType()
        );
        assetUploadValidator.validate(params);

        AssetDomain asset = assetFileUploadUseCase.uploadAssetFile(assetFileUploadMapper.toDomain(request));
        return ResponseEntity.accepted().body(assetFileUploadMapper.toResponse(asset));
    }

    @Override
    public ResponseEntity<List<Asset>> getAssetsByFilter(
            @RequestParam(value = "uploadDateStart", required = false) String uploadDateStart,
            @RequestParam(value = "uploadDateEnd", required = false) String uploadDateEnd,
            @RequestParam(value = "filename", required = false) String filename,
            @RequestParam(value = "filetype", required = false) String filetype,
            @RequestParam(value = "sortDirection", required = false) String sortDirection) {
        AssetFilterValidator.AssetFilterParams params = new AssetFilterValidator.AssetFilterParams(
                uploadDateStart, uploadDateEnd, filename, filetype, sortDirection
        );
        assetFilterValidator.validate(params);

        List<AssetDomain> assets = assetGetByFilterUseCase.getByFilter(assetGetByFilterMapper.toAssetFilter(uploadDateStart, uploadDateEnd, filename, filetype, sortDirection));
        return ResponseEntity.ok().body(assetGetByFilterMapper.toResponse(assets));
    }
}
