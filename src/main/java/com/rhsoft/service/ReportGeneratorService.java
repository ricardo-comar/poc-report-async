package com.rhsoft.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.microsoft.azure.functions.ExecutionContext;
import com.rhsoft.model.ReportEntity;
import com.rhsoft.model.ReportMessage;
import com.rhsoft.model.ReportProcessingStatus;
import com.rhsoft.model.ReportRequest;
import com.rhsoft.model.ReportStatus;
import com.rhsoft.storage.BlobStorageFacade;
import com.rhsoft.storage.TableStorageFacade;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ReportGeneratorService {

    BlobStorageFacade blobStorage = new BlobStorageFacade();
    TableStorageFacade tableStorage = new TableStorageFacade();

    public ReportProcessingStatus requestReport(ReportRequest request, final ExecutionContext context) {
        UUID executionId = UUID.randomUUID();
        ReportEntity entity = ReportEntity.builder()
                .executionId(executionId.toString())
                .reportId(request.getReportId().toString())
                .request(request)
                .status(ReportStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();

        tableStorage.upsertReport(entity);

        context.getLogger().info("Report request received with executionId: " + executionId);

        return ReportProcessingStatus.builder()
                .executionId(UUID.fromString(entity.getExecutionId()))
                .reportId(UUID.fromString(entity.getReportId()))
                .status(entity.getStatus())
                .requestedAt(entity.getRequestedAt().toString())
                .build();
    }

    public void generateReport(ReportMessage message, final ExecutionContext context) {

        Optional<ReportEntity> recordOpt = tableStorage.getReport(message.getExecutionId());
        if (recordOpt.isEmpty()) {
            context.getLogger().severe("No report record found for executionId: " + message.getExecutionId());
            return;
        }

        ReportEntity record = recordOpt.get();

        context.getLogger().info("Generating report for executionId: " + record.getExecutionId());
        record.setStatus(ReportStatus.PROCESSING);
        tableStorage.upsertReport(record);

        if (record.getRequest().getReportType() == null) {
            context.getLogger().severe("Invalid report type for executionId: " + record.getExecutionId());
            record.setStatus(ReportStatus.INVALID);
        } else {

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }

            try {
                byte[] reportContent = this.getClass().getClassLoader().getResourceAsStream("sample.pdf")
                        .readAllBytes();
                String filePath = "generated/report-" + record.getExecutionId() + ".pdf";
                blobStorage.uploadReport(filePath, reportContent);
                record.setFilePath(filePath);
                record.setFileSize((long) reportContent.length);
                context.getLogger().info("Report generation completed for executionId: " + record.getExecutionId());

                record.setStatus(ReportStatus.COMPLETED);
            } catch (Exception e) {
                context.getLogger().severe(
                        "Error generating report for executionId: " + record.getExecutionId() + " - " + e.getMessage());
                record.setStatus(ReportStatus.FAILED);
            }
        }

        record.setCompletedAt(LocalDateTime.now());
        tableStorage.upsertReport(record);

    }

    public Optional<ReportProcessingStatus> getReportStatus(String executionId, final ExecutionContext context) {

        return Optional.of(tableStorage.getReport(executionId).map(record -> ReportProcessingStatus.builder()
                .executionId(Optional.ofNullable(record.getExecutionId()).map(UUID::fromString).orElse(null))
                .reportId(Optional.ofNullable(record.getReportId()).map(UUID::fromString).orElse(null))
                .status(record.getStatus())
                .filePath(blobStorage.generateAccessUrl(record.getFilePath()).orElse(null))
                .fileSize(record.getFileSize())
                .build())
            .orElse(null));
    }

}
