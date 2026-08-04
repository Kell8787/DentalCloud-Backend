package com.dentalcloud.dentalcloudbackend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {
    private int status;
    private String code;
    private String message;

    @Builder.Default
    private Map<String, String> fieldErrors = Collections.emptyMap();

    private String traceId;
    private Instant timestamp;
}
