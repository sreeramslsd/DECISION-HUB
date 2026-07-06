package com.decisionhub.dto;

import com.decisionhub.entity.ReportFormat;

import java.time.Instant;
import java.util.UUID;

public record ReportResponse(
    UUID id,
    UUID userId,
    String title,
    String fileUrl,
    ReportFormat format,
    long sizeBytes,
    Instant createdAt
) {}
