package com.decisionhub.dto;

import com.decisionhub.entity.ReportFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReportRequest(
    @NotBlank(message = "Report title is required")
    String title,

    @NotNull(message = "Report format is required")
    ReportFormat format,

    @NotNull(message = "Decision ID is required")
    UUID decisionId
) {}
