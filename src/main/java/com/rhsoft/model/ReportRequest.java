package com.rhsoft.model;

import java.util.UUID;

import lombok.Data;

@Data
public class ReportRequest {

    private UUID reportId;
    private String name;
    private String email;
    private String reportType;
    
}
