package com.rhsoft.model;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportRequest {

    private UUID executionId;
    private String name;
    private String email;
    private String reportType;
    
}
