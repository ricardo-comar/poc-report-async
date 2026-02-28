package com.rhsoft;

public interface ApplicationConstants {
    
    String AZURE_WEB_JOBS_STORAGE = "AzureWebJobsStorage";
    String BLOB_CONTAINER_NAME = "reports-generated";
    String QUEUE_NAME = "report-async-queue";
    String TABLE_NAME = "ReportExecutionStatus";
    String PARTITION_KEY = "DEMO_PDF";
}
