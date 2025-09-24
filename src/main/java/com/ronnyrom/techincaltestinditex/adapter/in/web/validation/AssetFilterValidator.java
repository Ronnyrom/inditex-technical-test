package com.ronnyrom.techincaltestinditex.adapter.in.web.validation;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Set;

@Component
public class AssetFilterValidator {

    private static final Set<String> SORT_ALLOWED = Set.of("ASC", "DESC");

    public record AssetFilterParams(String uploadDateStart, String uploadDateEnd,
                                    String filename, String filetype, String sortDirection) {}

    public void validate(AssetFilterParams params) {
        if (params == null) return;

        if (params.uploadDateStart != null && !params.uploadDateStart.isBlank()) {
            parseIso(params.uploadDateStart, "uploadDateStart");
        }
        if (params.uploadDateEnd != null && !params.uploadDateEnd.isBlank()) {
            parseIso(params.uploadDateEnd, "uploadDateEnd");
        }
        if (params.sortDirection != null && !params.sortDirection.isBlank()
                && !SORT_ALLOWED.contains(params.sortDirection)) {
            throw new IllegalArgumentException("sortDirection must be one of: ASC, DESC");
        }
    }

    private void parseIso(String value, String field) {
        try {
            LocalDateTime.parse(value);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(field + " must be ISO-8601 LocalDateTime (e.g. 2024-05-01T10:15:30)");
        }
    }
}