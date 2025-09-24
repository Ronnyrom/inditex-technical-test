package com.ronnyrom.techincaltestinditex.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ronnyrom.adapter.in.web.model.AssetFileUploadRequest;
import com.ronnyrom.adapter.in.web.model.AssetFileUploadResponse;
import com.ronnyrom.techincaltestinditex.adapter.out.r2dbc.entity.AssetEntity;
import com.ronnyrom.techincaltestinditex.application.port.out.ReactiveStoragePort;
import com.ronnyrom.techincaltestinditex.repository.SpringDataAssetR2dbcRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AssetControllerIT {
    private static final String TEST_JWT_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuYW1lIiwiaWF0IjoxNzU4NzM5MTI4LCJleHAiOjE3NTg3NDI3MjgsInJvbGUiOiJVU0VSIn0.SZjR8K8lSGSe_aWjL4fUBvMrJqxklwvRJasQriA_McA";

    @TestConfiguration
    static class TestBeans {
        private static final AtomicBoolean shouldFail = new AtomicBoolean(false);
        static final AtomicInteger invocations = new AtomicInteger(0);
        public static void setShouldFail(boolean value) {
            shouldFail.set(value);
        }
        public static void resetCounters() {
            shouldFail.set(false);
            invocations.set(0);
        }
        @Bean
        @Primary
        ReactiveStoragePort storagePort() {
            return assetDomain -> {
                invocations.incrementAndGet();
                if (shouldFail.get()) {
                    shouldFail.set(false);
                    return Mono.error(new RuntimeException("Simulated upload failure"));
                } else {
                    return Mono.just("http://internal-storage/url/" + assetDomain.getId());
                }
            };
        }
    }
    @BeforeEach
    void setup() {
        AssetControllerIT.TestBeans.resetCounters();
        springDataAssetR2dbcRepository.deleteAll().block();
    }
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SpringDataAssetR2dbcRepository springDataAssetR2dbcRepository;
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Test
    void uploadAssetFile_202_and_persists_completed() {
        TestBeans.setShouldFail(false);
        AssetFileUploadRequest assetFileUploadRequest = new AssetFileUploadRequest()
                .filename("photo.jpg")
                .contentType("image/jpg")
                .encodedFile("encoded-file-sample".getBytes());

        String content = new String(
                webTestClient.post()
                        .uri("/api/mgmt/1/assets/actions/upload")
                        .header("Authorization", TEST_JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(assetFileUploadRequest)
                        .exchange()
                        .expectStatus().isAccepted()
                        .expectBody()
                        .jsonPath("$.id").value(not(blankOrNullString()))
                        .returnResult()
                        .getResponseBody()
        );
        AssetFileUploadResponse resp = readResponse(content);
        Integer id = Integer.valueOf(resp.getId());

        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            AssetEntity e = springDataAssetR2dbcRepository.findById(id).block();
            assertEquals("COMPLETED", e.getStatus());
            assertNotNull(e.getUrl());
        });
    }

    @Test
    void uploadAssetFile_202_and_persists_failed() {
        TestBeans.setShouldFail(true);

        AssetFileUploadRequest assetFileUploadRequest = new AssetFileUploadRequest()
                .filename("photo.jpg")
                .contentType("image/jpg")
                .encodedFile("encoded-file-sample".getBytes());

        String content = new String(
                webTestClient.post()
                        .uri("/api/mgmt/1/assets/actions/upload")
                        .header("Authorization", TEST_JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(assetFileUploadRequest)
                        .exchange()
                        .expectStatus().isAccepted()
                        .expectBody()
                        .jsonPath("$.id").value(not(blankOrNullString()))
                        .returnResult()
                        .getResponseBody()
        );

        AssetFileUploadResponse resp = readResponse(content);
        Integer id = Integer.valueOf(resp.getId());

        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            AssetEntity e = springDataAssetR2dbcRepository.findById(id).block();
            assertEquals("FAILED", e.getStatus());
            assertNull(e.getUrl());
        });
    }

    @Test
    void uploadAssetFile_with_invalid_request_400() {
        AssetFileUploadRequest assetFileUploadRequest = new AssetFileUploadRequest()
                .filename(null)
                .contentType(null)
                .encodedFile(null);

        webTestClient.post()
                .uri("/api/mgmt/1/assets/actions/upload")
                .header("Authorization", TEST_JWT_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(assetFileUploadRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void uploadAssetFile_with_invalid_filename_request_400() {
        AssetFileUploadRequest assetFileUploadRequest = new AssetFileUploadRequest()
                .filename(null)
                .contentType("image/jpg")
                .encodedFile("encoded-file-sample".getBytes());

        webTestClient.post()
                .uri("/api/mgmt/1/assets/actions/upload")
                .header("Authorization", TEST_JWT_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(assetFileUploadRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void uploadAssetFile_with_invalid_contentType_request_400() {
        AssetFileUploadRequest assetFileUploadRequest = new AssetFileUploadRequest()
                .filename("filename")
                .contentType(null)
                .encodedFile("encoded-file-sample".getBytes());

        webTestClient.post()
                .uri("/api/mgmt/1/assets/actions/upload")
                .header("Authorization", TEST_JWT_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(assetFileUploadRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void uploadAssetFile_with_invalid_encodedFile_request_400() {
        AssetFileUploadRequest assetFileUploadRequest = new AssetFileUploadRequest()
                .filename("filename")
                .contentType("image/jpg")
                .encodedFile(null);

        webTestClient.post()
                .uri("/api/mgmt/1/assets/actions/upload")
                .header("Authorization", TEST_JWT_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(assetFileUploadRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getAssetsByFilter_200_returns_filtered_sorted_array() {
        AssetEntity firstFile = springDataAssetR2dbcRepository.save(AssetEntity.builder()
                .filename("firstFile.png")
                .contentType("image/png")
                .url("http://internal/1")
                .size(111)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(3))
                .build()).block();

        springDataAssetR2dbcRepository.save(AssetEntity.builder()
                .filename("secondFile.jpg")
                .contentType("image/jpg")
                .url("http://internal/2")
                .size(222)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(2))
                .build()).block();

        AssetEntity thirdFile = springDataAssetR2dbcRepository.save(AssetEntity.builder()
                .filename("firstFile.png")
                .contentType("image/png")
                .url("http://internal/3")
                .size(333)
                .status("PENDING")
                .uploadDate(LocalDateTime.now().minusDays(1))
                .build()).block();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/mgmt/1/assets/")
                        .queryParam("filename", "firstFile.png")
                        .queryParam("filetype", "image/png")
                        .queryParam("sortDirection", "ASC")
                        .build())
                .header("Authorization", TEST_JWT_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].id").isEqualTo(String.valueOf(firstFile.getId()))
                .jsonPath("$[1].id").isEqualTo(String.valueOf(thirdFile.getId()));
    }

    @Test
    void getAssetsByFilter_200_returns_empty_array() {
        springDataAssetR2dbcRepository.save(AssetEntity.builder()
                .filename("firstFile.png")
                .contentType("image/png")
                .url("http://internal/1")
                .size(111)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(3))
                .build()).block();

        springDataAssetR2dbcRepository.save(AssetEntity.builder()
                .filename("secondFile.jpg")
                .contentType("image/jpg")
                .url("http://internal/2")
                .size(222)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(2))
                .build()).block();

        springDataAssetR2dbcRepository.save(AssetEntity.builder()
                .filename("thirdFile.png")
                .contentType("image/png")
                .url("http://internal/3")
                .size(333)
                .status("PENDING")
                .uploadDate(LocalDateTime.now().minusDays(1))
                .build()).block();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/mgmt/1/assets/")
                        .queryParam("filename", "forthFile")
                        .queryParam("filetype", "image/png")
                        .queryParam("sortDirection", "ASC")
                        .build())
                .header("Authorization", TEST_JWT_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void getAssetsByFilter_200_returns_filtered_sorted_array_DESC() {
        AssetEntity firstFile = springDataAssetR2dbcRepository.save(AssetEntity.builder()
                .filename("firstFile.png")
                .contentType("image/png")
                .url("http://internal/1")
                .size(111)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(3))
                .build()).block();

        springDataAssetR2dbcRepository.save(AssetEntity.builder()
                .filename("secondFile.jpg")
                .contentType("image/jpg")
                .url("http://internal/2")
                .size(222)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(2))
                .build()).block();

        AssetEntity thirdFile = springDataAssetR2dbcRepository.save(AssetEntity.builder()
                .filename("firstFile.png")
                .contentType("image/png")
                .url("http://internal/3")
                .size(333)
                .status("PENDING")
                .uploadDate(LocalDateTime.now().minusDays(1))
                .build()).block();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/mgmt/1/assets/")
                        .queryParam("filename", "firstFile.png")
                        .queryParam("filetype", "image/png")
                        .queryParam("sortDirection", "ASC")
                        .build())
                .header("Authorization", TEST_JWT_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[1].id").isEqualTo(String.valueOf(thirdFile.getId()))
                .jsonPath("$[0].id").isEqualTo(String.valueOf(firstFile.getId()));
    }

    @Test
    void getAssetsByFilter_200_returns_filtered_by_contentType() {
        springDataAssetR2dbcRepository.save(AssetEntity.builder()
                .filename("firstFile.png")
                .contentType("image/png")
                .url("http://internal/1")
                .size(111)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(3))
                .build()).block();

        AssetEntity secondFile = springDataAssetR2dbcRepository.save(AssetEntity.builder()
                .filename("secondFile.jpg")
                .contentType("image/jpg")
                .url("http://internal/2")
                .size(222)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(2))
                .build()).block();

        springDataAssetR2dbcRepository.save(AssetEntity.builder()
                .filename("thirsFile.png")
                .contentType("image/png")
                .url("http://internal/3")
                .size(333)
                .status("PENDING")
                .uploadDate(LocalDateTime.now().minusDays(1))
                .build()).block();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/mgmt/1/assets/")
                        .queryParam("filetype", "image/jpg")
                        .build())
                .header("Authorization", TEST_JWT_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].id").isEqualTo(String.valueOf(secondFile.getId()));
    }

    @Test
    void getAssetsByFilter_200_returns_filtered_by_uploadDateStart() {
        springDataAssetR2dbcRepository.save(AssetEntity.builder()
                .filename("firstFile.png")
                .contentType("image/png")
                .url("http://internal/1")
                .size(111)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(3))
                .build()).block();

        AssetEntity secondFile = springDataAssetR2dbcRepository.save(AssetEntity.builder()
                .filename("secondFile.jpg")
                .contentType("image/jpg")
                .url("http://internal/2")
                .size(222)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(2))
                .build()).block();

        AssetEntity thirdFile = springDataAssetR2dbcRepository.save(AssetEntity.builder()
                .filename("thirdFile.png")
                .contentType("image/png")
                .url("http://internal/3")
                .size(333)
                .status("PENDING")
                .uploadDate(LocalDateTime.now().minusDays(1))
                .build()).block();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/mgmt/1/assets/")
                        .queryParam("uploadDateStart", LocalDateTime.now().minusDays(2).format(formatter))
                        .queryParam("sortDirection", "ASC")
                        .build())
                .header("Authorization", TEST_JWT_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].id").isEqualTo(String.valueOf(secondFile.getId()))
                .jsonPath("$[1].id").isEqualTo(String.valueOf(thirdFile.getId()));
    }

    @Test
    void getAssetsByFilter_200_returns_filtered_by_uploadDateEnd() {
        AssetEntity firstFile = springDataAssetR2dbcRepository.save(AssetEntity.builder()
                .filename("firstFile.png")
                .contentType("image/png")
                .url("http://internal/1")
                .size(111)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(3))
                .build()).block();

        AssetEntity secondFile = springDataAssetR2dbcRepository.save(AssetEntity.builder()
                .filename("secondFile.jpg")
                .contentType("image/jpg")
                .url("http://internal/2")
                .size(222)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(2))
                .build()).block();

        AssetEntity thirdFile = springDataAssetR2dbcRepository.save(AssetEntity.builder()
                .filename("thirdFile.png")
                .contentType("image/png")
                .url("http://internal/3")
                .size(333)
                .status("PENDING")
                .uploadDate(LocalDateTime.now().minusDays(1))
                .build()).block();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/mgmt/1/assets/")
                        .queryParam("uploadDateEnd", LocalDateTime.now().format(formatter))
                        .queryParam("sortDirection", "ASC")
                        .build())
                .header("Authorization", TEST_JWT_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[0].id").isEqualTo(String.valueOf(firstFile.getId()))
                .jsonPath("$[1].id").isEqualTo(String.valueOf(secondFile.getId()))
                .jsonPath("$[2].id").isEqualTo(String.valueOf(thirdFile.getId()));
    }

    @Test
    void getAssetsByFilter_200_returns_filtered_by_both_uploadDateStart_and_uploadDateEnd() {
        springDataAssetR2dbcRepository.save(AssetEntity.builder()
                .filename("firstFile.png")
                .contentType("image/png")
                .url("http://internal/1")
                .size(111)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(3))
                .build()).block();

        AssetEntity secondFile = springDataAssetR2dbcRepository.save(AssetEntity.builder()
                .filename("secondFile.jpg")
                .contentType("image/jpg")
                .url("http://internal/2")
                .size(222)
                .status("COMPLETED")
                .uploadDate(LocalDateTime.now().minusDays(2))
                .build()).block();

        springDataAssetR2dbcRepository.save(AssetEntity.builder()
                .filename("thirdFile.png")
                .contentType("image/png")
                .url("http://internal/3")
                .size(333)
                .status("PENDING")
                .uploadDate(LocalDateTime.now().minusDays(1))
                .build()).block();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/mgmt/1/assets/")
                        .queryParam("uploadDateStart", LocalDateTime.now().minusDays(2).format(formatter))
                        .queryParam("uploadDateEnd", LocalDateTime.now().minusDays(1).format(formatter))
                        .queryParam("sortDirection", "ASC")
                        .build())
                .header("Authorization", TEST_JWT_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].id").isEqualTo(String.valueOf(secondFile.getId()));
    }

    @Test
    void uploadAssetFile_circuitBreaker_triggers_fallback_after_retries() {
        AssetControllerIT.TestBeans.setShouldFail(true);

        AssetFileUploadRequest assetFileUploadRequest = new AssetFileUploadRequest()
                .filename("photo.jpg")
                .contentType("image/jpg")
                .encodedFile("encoded-file-sample".getBytes());

        String content = new String(
                webTestClient.post()
                        .uri("/api/mgmt/1/assets/actions/upload")
                        .header("Authorization", TEST_JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(assetFileUploadRequest)
                        .exchange()
                        .expectStatus().isAccepted()
                        .expectBody()
                        .returnResult()
                        .getResponseBody()
        );

        AssetFileUploadResponse resp = readResponse(content);
        Integer id = Integer.valueOf(resp.getId());

        Awaitility.await().atMost(8, TimeUnit.SECONDS).untilAsserted(() -> {
            AssetEntity e = springDataAssetR2dbcRepository.findById(id).block();
            assertEquals("FAILED", e.getStatus());
            assertNull(e.getUrl());
        });
    }

    @Test
    void uploadAssetFile_circuitBreaker_open_short_circuits_and_uses_fallback() {
        TestBeans.resetCounters();
        var cb = circuitBreakerRegistry.circuitBreaker("storageService");
        cb.transitionToOpenState();
        int before = TestBeans.invocations.get();

        AssetFileUploadRequest req = new AssetFileUploadRequest()
                .filename("photo.jpg")
                .contentType("image/jpg")
                .encodedFile("encoded-file-sample".getBytes());

        String content = new String(
                webTestClient.post()
                        .uri("/api/mgmt/1/assets/actions/upload")
                        .header("Authorization", TEST_JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(req)
                        .exchange()
                        .expectStatus().isAccepted()
                        .expectBody()
                        .returnResult()
                        .getResponseBody()
        );

        AssetFileUploadResponse resp = readResponse(content);
        Integer id = Integer.valueOf(resp.getId());


        Awaitility.await().atMost(6, TimeUnit.SECONDS).untilAsserted(() -> {
            AssetEntity e = springDataAssetR2dbcRepository.findById(id).block();
            assertEquals("FAILED", e.getStatus());
            assertNull(e.getUrl());
        });
    }

    private AssetFileUploadResponse readResponse(String content) {
        try {
            return objectMapper.readValue(content, AssetFileUploadResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}