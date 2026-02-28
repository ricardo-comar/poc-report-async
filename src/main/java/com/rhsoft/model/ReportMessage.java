package com.rhsoft.model;

import lombok.Builder;
import lombok.Value;

@Value @Builder
public class ReportMessage {
    
    private String executionId;
    private String reportId;
    private ReportRequest request;
}
