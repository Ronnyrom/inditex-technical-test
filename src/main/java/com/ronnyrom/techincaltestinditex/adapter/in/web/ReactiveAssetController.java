package com.ronnyrom.techincaltestinditex.adapter.in.web;

import com.ronnyrom.adapter.in.web.AssetApi;
import com.ronnyrom.adapter.in.web.model.Asset;
import com.ronnyrom.adapter.in.web.model.AssetFileUploadRequest;
import com.ronnyrom.adapter.in.web.model.AssetFileUploadResponse;
import com.ronnyrom.techincaltestinditex.adapter.in.web.mapper.AssetFileUploadMapper;
import com.ronnyrom.techincaltestinditex.adapter.in.web.mapper.AssetGetByFilterMapper;
import com.ronnyrom.techincaltestinditex.adapter.in.web.validation.AssetFilterValidator;
import com.ronnyrom.techincaltestinditex.adapter.in.web.validation.AssetUploadValidator;
import com.ronnyrom.techincaltestinditex.application.port.in.ReactiveAssetFileUploadUseCase;
import com.ronnyrom.techincaltestinditex.application.port.in.ReactiveAssetGetByFilterUseCase;
import com.ronnyrom.techincaltestinditex.domain.model.AssetFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class ReactiveAssetController implements AssetApi {

    private final ReactiveAssetFileUploadUseCase uploadUseCase;
    private final ReactiveAssetGetByFilterUseCase reactiveAssetGetByFilterUseCase;
    private final AssetFileUploadMapper uploadMapper;
    private final AssetGetByFilterMapper filterMapper;
    private final AssetFilterValidator filterValidator;
    private final AssetUploadValidator uploadValidator;

    public ReactiveAssetController(ReactiveAssetFileUploadUseCase uploadUseCase,
                                   ReactiveAssetGetByFilterUseCase reactiveAssetGetByFilterUseCase,
                                   AssetFileUploadMapper uploadMapper,
                                   AssetGetByFilterMapper filterMapper,
                                   AssetFilterValidator filterValidator,
                                   AssetUploadValidator uploadValidator) {
        this.uploadUseCase = uploadUseCase;
        this.reactiveAssetGetByFilterUseCase = reactiveAssetGetByFilterUseCase;
        this.uploadMapper = uploadMapper;
        this.filterMapper = filterMapper;
        this.filterValidator = filterValidator;
        this.uploadValidator = uploadValidator;
    }

    @Override
    public Mono<ResponseEntity<Flux<Asset>>> getAssetsByFilter(String uploadDateStart,
                                                               String uploadDateEnd,
                                                               String filename,
                                                               String filetype,
                                                               String sortDirection,
                                                               ServerWebExchange exchange) {
        var params = new AssetFilterValidator.AssetFilterParams(
                uploadDateStart, uploadDateEnd, filename, filetype, sortDirection
        );
        filterValidator.validate(params);

        AssetFilter filter = filterMapper.toAssetFilter(uploadDateStart, uploadDateEnd, filename, filetype, sortDirection);

        Flux<Asset> body = reactiveAssetGetByFilterUseCase.findByFilter(filter)
                .map(filterMapper::toResponse);

        return Mono.just(ResponseEntity.ok(body));
    }

    @Override
    public Mono<ResponseEntity<AssetFileUploadResponse>> uploadAssetFile(Mono<AssetFileUploadRequest> assetFileUploadRequest,
                                                                         ServerWebExchange exchange) {
        return assetFileUploadRequest
                .flatMap(req -> {
                    try {
                        uploadValidator.validate(
                                new AssetUploadValidator.AssetFileUploadParams(
                                        req.getFilename(),
                                        req.getEncodedFile() != null
                                                ? new String(req.getEncodedFile(), java.nio.charset.StandardCharsets.UTF_8)
                                                : "",
                                        req.getContentType()
                                )
                        );
                        return Mono.just(req);
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                })
                .map(uploadMapper::toDomain)
                .flatMap(uploadUseCase::uploadAssetFile)
                .map(uploadMapper::toResponse)
                .map(resp -> ResponseEntity.status(HttpStatus.ACCEPTED).body(resp));
    }
}