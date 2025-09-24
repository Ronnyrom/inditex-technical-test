package com.ronnyrom.techincaltestinditex.adapter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ronnyrom.adapter.in.web.model.Asset;
import com.ronnyrom.adapter.in.web.model.AssetFileUploadRequest;
import com.ronnyrom.adapter.in.web.model.AssetFileUploadResponse;
import com.ronnyrom.techincaltestinditex.adapter.in.web.AssetController;
import com.ronnyrom.techincaltestinditex.adapter.in.web.mapper.AssetFileUploadMapper;
import com.ronnyrom.techincaltestinditex.adapter.in.web.mapper.AssetGetByFilterMapper;
import com.ronnyrom.techincaltestinditex.adapter.in.web.validation.AssetFilterValidator;
import com.ronnyrom.techincaltestinditex.adapter.in.web.validation.AssetUploadValidator;
import com.ronnyrom.techincaltestinditex.application.port.in.AssetFileUploadUseCase;
import com.ronnyrom.techincaltestinditex.application.port.in.AssetGetByFilterUseCase;
import com.ronnyrom.techincaltestinditex.domain.model.AssetDomain;
import com.ronnyrom.techincaltestinditex.domain.model.Status;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(AssetController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AssetControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class MockConfig {
        @Bean
        AssetFileUploadUseCase assetFileUploadUseCase() {
            return Mockito.mock(AssetFileUploadUseCase.class);
        }

        @Bean
        AssetGetByFilterUseCase assetGetByFilterUseCase() {
            return Mockito.mock(AssetGetByFilterUseCase.class);
        }

        @Bean
        AssetFileUploadMapper assetFileUploadMapper() {
            return Mockito.mock(AssetFileUploadMapper.class);
        }

        @Bean
        AssetGetByFilterMapper assetGetByFilterMapper() {
            return Mockito.mock(AssetGetByFilterMapper.class);
        }

        @Bean
        AssetFilterValidator assetFilterValidator() {
            return Mockito.mock(AssetFilterValidator.class);
        }

        @Bean
        AssetUploadValidator assetUploadValidator() {
            return Mockito.mock(AssetUploadValidator.class);
        } // <-- nuevo bean

    }

    @Autowired
    private AssetFileUploadUseCase assetFileUploadUseCase;
    @Autowired
    private AssetFileUploadMapper assetFileUploadMapper;
    @Autowired
    private AssetGetByFilterMapper assetGetByFilterMapper;
    @Autowired
    private AssetGetByFilterUseCase assetGetByFilterUseCase;
    @Autowired
    private AssetFilterValidator validator;
    @Autowired
    private AssetUploadValidator assetUploadValidator;

    @AfterEach
    void tearDown() {
        reset(assetFileUploadUseCase, assetGetByFilterUseCase, assetFileUploadMapper, assetGetByFilterMapper, validator, assetUploadValidator);
    }

    @Test
    void uploadAssetFile_returnsAcceptedAndBody() throws Exception {
        AssetFileUploadRequest request = new AssetFileUploadRequest()
                .filename("filename.jpg")
                .encodedFile(Base64.getDecoder().decode("dGVzdC1jb250ZW50LWJhc2U2NA=="))
                .contentType("image/jpg");

        AssetDomain toUpload = generateAsset(null, null, null);
        AssetDomain saved = generateAsset(Status.PENDING, 1, "http://cdn/url");

        AssetFileUploadResponse response = new AssetFileUploadResponse()
                .id("1");

        when(assetFileUploadMapper.toDomain(request)).thenReturn(toUpload);
        when(assetFileUploadUseCase.uploadAssetFile(toUpload)).thenReturn(saved);
        when(assetFileUploadMapper.toResponse(saved)).thenReturn(response);

        mockMvc.perform(post("/api/mgmt/1/assets/actions/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAssetsByFilter_returnsAssetList() throws Exception {
        String url = "/api/mgmt/1/assets/";

        String uploadDateStart = "2024-01-01T00:00:00";
        String uploadDateEnd = "2024-12-31T00:00:00";
        String filename = "file";
        String filetype = "image/jpg";
        String sortDirection = "ASC";

        List<AssetDomain> assets = List.of(
                generateAsset(Status.PENDING, 1, "http://internalStorage/1"),
                generateAsset(Status.COMPLETED, 2, "http://internalStorage/2")
        );

        List<Asset> assetsDto = List.of(
                new Asset().id("1"),
                new Asset().id("2")
        );

        when(assetGetByFilterUseCase.getByFilter(any())).thenReturn(assets);
        when(assetGetByFilterMapper.toResponse(anyList())).thenReturn(assetsDto);

        mockMvc.perform(get(url)
                        .param("uploadDateStart", uploadDateStart)
                        .param("uploadDateEnd", uploadDateEnd)
                        .param("filename", filename)
                        .param("filetype", filetype)
                        .param("sortDirection", sortDirection)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[1].id").value("2"));
    }

    @Test
    void getAssetsByFilter_returns400_when_validator_fails() throws Exception {
        doThrow(new IllegalArgumentException("sortDirection debe ser ASC o DESC"))
                .when(validator).validate(any(AssetFilterValidator.AssetFilterParams.class));

        mockMvc.perform(get("/api/mgmt/1/assets/")
                        .param("sortDirection", "UP"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("sortDirection debe ser ASC o DESC"));

        verify(validator, times(1)).validate(any(AssetFilterValidator.AssetFilterParams.class));
        verifyNoInteractions(assetFileUploadUseCase);
    }

    @Test
    void uploadAssetFile_returns500_when_usecase_throws() throws Exception {
        AssetFileUploadRequest req = new AssetFileUploadRequest()
                .filename("a.jpg").encodedFile("x".getBytes()).contentType("image/jpg");

        when(assetFileUploadMapper.toDomain(any())).thenReturn(AssetDomain.builder().build());
        when(assetFileUploadUseCase.uploadAssetFile(any()))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Internal error saving the asset"));

        mockMvc.perform(post("/api/mgmt/1/assets/actions/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }

    private AssetDomain generateAsset(Status status, Integer id, String url) {
        byte[] encodedFile = "test-content-base64".getBytes();
        return AssetDomain.builder()
                .id(id)
                .filename("filename.jpg")
                .encodedFile(encodedFile)
                .url(url)
                .contentType("image/jpg")
                .size(378)
                .status(status)
                .build();
    }
}

