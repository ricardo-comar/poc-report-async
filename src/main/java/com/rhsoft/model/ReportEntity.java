package com.rhsoft.model;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportEntity {

    private String executionId;
    private String reportId;
    private ReportStatus status;
    private ReportRequest request;

    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
    private String filePath;
    private Long fileSize;

}
