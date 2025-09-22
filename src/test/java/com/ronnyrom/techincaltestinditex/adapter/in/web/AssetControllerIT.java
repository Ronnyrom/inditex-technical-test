package com.ronnyrom.techincaltestinditex.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ronnyrom.adapter.in.web.model.AssetFileUploadRequest;
import com.ronnyrom.adapter.in.web.model.AssetFileUploadResponse;
import com.ronnyrom.techincaltestinditex.adapter.out.persistence.entity.AssetEntity;
import com.ronnyrom.techincaltestinditex.application.port.out.StoragePort;
import com.ronnyrom.techincaltestinditex.repository.UploadRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AssetControllerIT {

    @TestConfiguration
    static class TestBeans {
        private static final AtomicBoolean shouldFail = new AtomicBoolean(false);

        public static void setShouldFail(boolean value) {
            shouldFail.set(value);
        }

        @Bean
        @Primary
        StoragePort storagePort() {
            return assetDomain -> {
                if (shouldFail.get()) {
                    shouldFail.set(false);
                    CompletableFuture<String> future = new CompletableFuture<>();
                    future.completeExceptionally(new RuntimeException("Simulated upload failure"));
                    return future;
                } else {
                    return CompletableFuture.completedFuture("http://internal-storage/url/" + assetDomain.getId());
                }
            };
        }
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UploadRepository uploadRepository;

    @Test
    void uploadAssetFile_202_and_persists_completed() throws Exception {
        TestBeans.setShouldFail(false);
        AssetFileUploadRequest assetFileUploadRequest = new AssetFileUploadRequest()
                .filename("photo.jpg")
                .contentType("image/jpg")
                .encodedFile("encoded-file-sample".getBytes());

        String content = mockMvc.perform(post("/api/mgmt/1/assets/actions/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assetFileUploadRequest)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", not(blankOrNullString())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        AssetFileUploadResponse resp = objectMapper.readValue(content, AssetFileUploadResponse.class);
        Integer id = Integer.valueOf(resp.getId());

        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            AssetEntity e = uploadRepository.findById(id).orElseThrow();
            assertEquals("COMPLETED", e.getStatus());
            assertNotNull(e.getUrl());
        });
    }

    @Test
    void uploadAssetFile_202_and_persists_failed() throws Exception {
        TestBeans.setShouldFail(true);

        AssetFileUploadRequest assetFileUploadRequest = new AssetFileUploadRequest()
                .filename("photo.jpg")
                .contentType("image/jpg")
                .encodedFile("encoded-file-sample".getBytes());

        String content = mockMvc.perform(post("/api/mgmt/1/assets/actions/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assetFileUploadRequest)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", not(blankOrNullString())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        AssetFileUploadResponse resp = objectMapper.readValue(content, AssetFileUploadResponse.class);
        Integer id = Integer.valueOf(resp.getId());

        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            AssetEntity e = uploadRepository.findById(id).orElseThrow();
            assertEquals("FAILED", e.getStatus());
            assertNull(e.getUrl());
        });
    }

    @Test
    void uploadAssetFile_with_invalid_request_400() throws Exception {
        AssetFileUploadRequest assetFileUploadRequest = new AssetFileUploadRequest()
                .filename(null)
                .contentType(null)
                .encodedFile(null);

        mockMvc.perform(post("/api/mgmt/1/assets/actions/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assetFileUploadRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadAssetFile_with_invalid_filename_request_400() throws Exception {
        AssetFileUploadRequest assetFileUploadRequest = new AssetFileUploadRequest()
                .filename(null)
                .contentType("image/jpg")
                .encodedFile("encoded-file-sample".getBytes());

        mockMvc.perform(post("/api/mgmt/1/assets/actions/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assetFileUploadRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadAssetFile_with_invalid_contentType_request_400() throws Exception {
        AssetFileUploadRequest assetFileUploadRequest = new AssetFileUploadRequest()
                .filename("filename")
                .contentType(null)
                .encodedFile("encoded-file-sample".getBytes());

        mockMvc.perform(post("/api/mgmt/1/assets/actions/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assetFileUploadRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadAssetFile_with_invalid_encodedFile_request_400() throws Exception {
        AssetFileUploadRequest assetFileUploadRequest = new AssetFileUploadRequest()
                .filename("filename")
                .contentType("image/jpg")
                .encodedFile(null);

        mockMvc.perform(post("/api/mgmt/1/assets/actions/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assetFileUploadRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAssetsByFilter_200_returns_filtered_sorted_array() throws Exception {
        AssetEntity firstFile = uploadRepository.save(AssetEntity.builder()
                .filename("firstFile.png")
                .contentType("image/png")
                .url("http://internal/1")
                .size(111)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(3))
                .build());

        uploadRepository.save(AssetEntity.builder()
                .filename("secondFile.jpg")
                .contentType("image/jpg")
                .url("http://internal/2")
                .size(222)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(2))
                .build());

        AssetEntity thirdFile = uploadRepository.save(AssetEntity.builder()
                .filename("firstFile.png")
                .contentType("image/png")
                .url("http://internal/3")
                .size(333)
                .status("PENDING")
                .uploadDate(LocalDateTime.now().minusDays(1))
                .build());

        mockMvc.perform(get("/api/mgmt/1/assets/") // si falla la barra final, usa "/api/mgmt/1/assets"
                        .param("filename", "firstFile.png")
                        .param("filetype", "image/png")
                        .param("sortDirection", "ASC")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].id", is(String.valueOf(firstFile.getId()))))
                .andExpect(jsonPath("$[1].id", is(String.valueOf(thirdFile.getId()))));
    }

    @Test
    void getAssetsByFilter_200_returns_empty_array() throws Exception {
        uploadRepository.save(AssetEntity.builder()
                .filename("firstFile.png")
                .contentType("image/png")
                .url("http://internal/1")
                .size(111)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(3))
                .build());

        uploadRepository.save(AssetEntity.builder()
                .filename("secondFile.jpg")
                .contentType("image/jpg")
                .url("http://internal/2")
                .size(222)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(2))
                .build());

        uploadRepository.save(AssetEntity.builder()
                .filename("thirdFile.png")
                .contentType("image/png")
                .url("http://internal/3")
                .size(333)
                .status("PENDING")
                .uploadDate(LocalDateTime.now().minusDays(1))
                .build());

        mockMvc.perform(get("/api/mgmt/1/assets/")
                        .param("filename", "forthFile")
                        .param("filetype", "image/png")
                        .param("sortDirection", "ASC")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$.length()", is(0)));
    }
    @Test
    void getAssetsByFilter_200_returns_filtered_sorted_array_DESC() throws Exception {
        AssetEntity firstFile = uploadRepository.save(AssetEntity.builder()
                .filename("firstFile.png")
                .contentType("image/png")
                .url("http://internal/1")
                .size(111)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(3))
                .build());

        uploadRepository.save(AssetEntity.builder()
                .filename("secondFile.jpg")
                .contentType("image/jpg")
                .url("http://internal/2")
                .size(222)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(2))
                .build());

        AssetEntity thirdFile = uploadRepository.save(AssetEntity.builder()
                .filename("firstFile.png")
                .contentType("image/png")
                .url("http://internal/3")
                .size(333)
                .status("PENDING")
                .uploadDate(LocalDateTime.now().minusDays(1))
                .build());

        mockMvc.perform(get("/api/mgmt/1/assets/") // si falla la barra final, usa "/api/mgmt/1/assets"
                        .param("filename", "firstFile.png")
                        .param("filetype", "image/png")
                        .param("sortDirection", "ASC")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[1].id", is(String.valueOf(thirdFile.getId()))))
                .andExpect(jsonPath("$[0].id", is(String.valueOf(firstFile.getId()))));
    }
    @Test
    void getAssetsByFilter_200_returns_filtered_by_contentType() throws Exception {
        uploadRepository.save(AssetEntity.builder()
                .filename("firstFile.png")
                .contentType("image/png")
                .url("http://internal/1")
                .size(111)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(3))
                .build());

        AssetEntity secondFile =uploadRepository.save(AssetEntity.builder()
                .filename("secondFile.jpg")
                .contentType("image/jpg")
                .url("http://internal/2")
                .size(222)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(2))
                .build());

        uploadRepository.save(AssetEntity.builder()
                .filename("thirsFile.png")
                .contentType("image/png")
                .url("http://internal/3")
                .size(333)
                .status("PENDING")
                .uploadDate(LocalDateTime.now().minusDays(1))
                .build());

        mockMvc.perform(get("/api/mgmt/1/assets/") // si falla la barra final, usa "/api/mgmt/1/assets"
                        .param("filetype", "image/jpg")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(String.valueOf(secondFile.getId()))));
    }
    @Test
    void getAssetsByFilter_200_returns_filtered_by_uploadDateStart() throws Exception {
        uploadRepository.save(AssetEntity.builder()
                .filename("firstFile.png")
                .contentType("image/png")
                .url("http://internal/1")
                .size(111)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(3))
                .build());

        AssetEntity secondFile =  uploadRepository.save(AssetEntity.builder()
                .filename("secondFile.jpg")
                .contentType("image/jpg")
                .url("http://internal/2")
                .size(222)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(2))
                .build());

        AssetEntity thirdFile = uploadRepository.save(AssetEntity.builder()
                .filename("thirdFile.png")
                .contentType("image/png")
                .url("http://internal/3")
                .size(333)
                .status("PENDING")
                .uploadDate(LocalDateTime.now().minusDays(1))
                .build());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        mockMvc.perform(get("/api/mgmt/1/assets/")
                        .param("uploadDateStart",LocalDateTime.now().minusDays(2).format(formatter))
                        .param("sortDirection", "ASC")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].id", is(String.valueOf(secondFile.getId()))))
                .andExpect(jsonPath("$[1].id", is(String.valueOf(thirdFile.getId()))));
    }
    @Test
    void getAssetsByFilter_200_returns_filtered_by_uploadDateEnd() throws Exception {
        AssetEntity firstFile = uploadRepository.save(AssetEntity.builder()
                .filename("firstFile.png")
                .contentType("image/png")
                .url("http://internal/1")
                .size(111)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(3))
                .build());

        AssetEntity secondFile =  uploadRepository.save(AssetEntity.builder()
                .filename("secondFile.jpg")
                .contentType("image/jpg")
                .url("http://internal/2")
                .size(222)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(2))
                .build());

        AssetEntity thirdFile = uploadRepository.save(AssetEntity.builder()
                .filename("thirdFile.png")
                .contentType("image/png")
                .url("http://internal/3")
                .size(333)
                .status("PENDING")
                .uploadDate(LocalDateTime.now().minusDays(1))
                .build());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        mockMvc.perform(get("/api/mgmt/1/assets/")
                        .param("uploadDateEnd",LocalDateTime.now().format(formatter))
                        .param("sortDirection", "ASC")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$.length()", is(3)))
                .andExpect(jsonPath("$[0].id", is(String.valueOf(firstFile.getId()))))
                .andExpect(jsonPath("$[1].id", is(String.valueOf(secondFile.getId()))))
                .andExpect(jsonPath("$[2].id", is(String.valueOf(thirdFile.getId()))));
    }

    @Test
    void getAssetsByFilter_200_returns_filtered_by_both_uploadDateStart_and_uploadDateEnd() throws Exception {
        uploadRepository.save(AssetEntity.builder()
                .filename("firstFile.png")
                .contentType("image/png")
                .url("http://internal/1")
                .size(111)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(3))
                .build());

        AssetEntity secondFile =  uploadRepository.save(AssetEntity.builder()
                .filename("secondFile.jpg")
                .contentType("image/jpg")
                .url("http://internal/2")
                .size(222)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(2))
                .build());

        uploadRepository.save(AssetEntity.builder()
                .filename("thirdFile.png")
                .contentType("image/png")
                .url("http://internal/3")
                .size(333)
                .status("PENDING")
                .uploadDate(LocalDateTime.now().minusDays(1))
                .build());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        mockMvc.perform(get("/api/mgmt/1/assets/")
                        .param("uploadDateStart",LocalDateTime.now().minusDays(2).format(formatter))
                        .param("uploadDateEnd",LocalDateTime.now().minusDays(1).format(formatter))
                        .param("sortDirection", "ASC")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(String.valueOf(secondFile.getId()))));
    }

    @Test
    void uploadAssetFile_circuitBreaker_triggers_fallback_after_retries() throws Exception {
        AssetControllerIT.TestBeans.setShouldFail(true);

        AssetFileUploadRequest assetFileUploadRequest = new AssetFileUploadRequest()
                .filename("photo.jpg")
                .contentType("image/jpg")
                .encodedFile("encoded-file-sample".getBytes());

        String content = mockMvc.perform(post("/api/mgmt/1/assets/actions/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assetFileUploadRequest)))
                .andExpect(status().isAccepted())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AssetFileUploadResponse resp = objectMapper.readValue(content, AssetFileUploadResponse.class);
        Integer id = Integer.valueOf(resp.getId());

        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            AssetEntity e = uploadRepository.findById(id).orElseThrow();
            assertEquals("FAILED", e.getStatus());
            assertNull(e.getUrl());
        });
    }

}
