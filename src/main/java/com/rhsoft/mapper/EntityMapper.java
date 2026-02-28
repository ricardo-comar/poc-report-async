package com.rhsoft.mapper;

import java.util.Optional;

import com.azure.data.tables.models.TableEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rhsoft.ApplicationConstants;
import com.rhsoft.model.ReportEntity;
import com.rhsoft.model.ReportRequest;
import com.rhsoft.model.ReportStatus;

import lombok.AllArgsConstructor;

@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class EntityMapper {

    private static final EntityMapper INSTANCE;
    private static final ObjectMapper MAPPER;

    static {
        INSTANCE = new EntityMapper();
        MAPPER = new ObjectMapper();
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static EntityMapper getInstance() {
        return INSTANCE;
    }

    public TableEntity toTableEntity(ReportEntity report) {
        if (report == null) {
            return null;
        }

        TableEntity entity = new TableEntity(ApplicationConstants.PARTITION_KEY, report.getExecutionId());
        entity.addProperty("request", Optional.of(report.getRequest()).map(req -> {
            try {
                return MAPPER.writeValueAsString(req);
            } catch (Exception e) {
                throw new RuntimeException("Error serializing report request", e);
            }
        }).orElse(null));
        entity.addProperty("status", report.getStatus().name());
        entity.addProperty("filePath", report.getFilePath());
        entity.addProperty("fileSize", report.getFileSize());
        entity.addProperty("requestedAt",
                Optional.ofNullable(report.getRequestedAt()).map(r -> r.toString()).orElse(null));
        entity.addProperty("completedAt",
                Optional.ofNullable(report.getCompletedAt()).map(r -> r.toString()).orElse(null));
        return entity;

    }

    public ReportEntity toReportEntity(TableEntity entity) {
        if (entity == null) {
            return null;
        }

        return ReportEntity.builder()
                .executionId(entity.getRowKey())
                .request(
                        Optional.ofNullable(entity.getProperty("request")).map(req -> {
                            try {
                                return MAPPER.readValue(req.toString(), ReportRequest.class);
                            } catch (Exception e) {
                                throw new RuntimeException("Error deserializing report request", e);
                            }
                        }).orElse(null))
                .status(ReportStatus.valueOf(entity.getProperty("status").toString()))
                .filePath((String) entity.getProperty("filePath"))
                .fileSize((Long) entity.getProperty("fileSize"))
                .requestedAt(Optional.ofNullable(entity.getProperty("requestedAt"))
                        .map(r -> java.time.LocalDateTime.parse(r.toString())).orElse(null))
                .completedAt(Optional.ofNullable(entity.getProperty("completedAt"))
                        .map(r -> java.time.LocalDateTime.parse(r.toString())).orElse(null))
                .build();
    }
}
