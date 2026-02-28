package com.rhsoft.model;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ReportProcessingStatus {
    
    private UUID executionId;
    private UUID reportId;
    private ReportStatus status;

    private String requestedAt;
    private String completedAt;
}
