package com.rhsoft.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.microsoft.azure.functions.ExecutionContext;
import com.rhsoft.model.ReportEntity;
import com.rhsoft.model.ReportMessage;
import com.rhsoft.model.ReportProcessingStatus;
import com.rhsoft.model.ReportRequest;
import com.rhsoft.model.ReportStatus;
import com.rhsoft.storage.BlobStorageFacade;
import com.rhsoft.storage.TableStorageFacade;


public class ReportGeneratorServiceTest {

    @Mock
    private BlobStorageFacade blobStorage;

    @Mock
    private TableStorageFacade tableStorage;

    @Mock
    private ExecutionContext context;

    private ReportGeneratorService service;

    private ReportMessage message;

    private ReportRequest reportRequest = new ReportRequest();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new ReportGeneratorService(blobStorage, tableStorage, 10L);

        doReturn(Logger.getGlobal()).when(context).getLogger();

        reportRequest.setReportId(UUID.randomUUID());
        reportRequest.setReportType("SALES");

        message = ReportMessage.builder().executionId(UUID.randomUUID().toString()).build();
    }

    @Test
    public void testRequestReport() {

        ReportProcessingStatus result = service.requestReport(reportRequest, context);

        assertNotNull(result);
        assertNotNull(result.getExecutionId());
        verify(tableStorage, times(1)).upsertReport(any(ReportEntity.class));
    }

    @Test
    public void testGenerateReportNotFound() {
        when(tableStorage.getReport(anyString())).thenReturn(Optional.empty());

        service.generateReport(message, context);

        verify(tableStorage, times(0)).upsertReport(any());
        verify(blobStorage, times(0)).uploadReport(anyString(), any());
    }

    @Test
    public void testGenerateReportInvalidType() {
        reportRequest.setReportType(null);
        ReportEntity entity = ReportEntity.builder().executionId(message.getExecutionId())
                .request(reportRequest).status(ReportStatus.PENDING).build();

        when(tableStorage.getReport(entity.getExecutionId())).thenReturn(Optional.of(entity));

        service.generateReport(message, context);

        assertEquals(ReportStatus.INVALID, entity.getStatus());
        verify(tableStorage, times(2)).upsertReport(any());
        verify(blobStorage, times(0)).uploadReport(anyString(), any());
    }

    @Test
    public void testGenerateReport() {
        ReportEntity entity = ReportEntity.builder().executionId(message.getExecutionId())
                .request(reportRequest).status(ReportStatus.PENDING).build();

        when(tableStorage.getReport(entity.getExecutionId())).thenReturn(Optional.of(entity));

        service.generateReport(message, context);

        assertEquals(ReportStatus.COMPLETED, entity.getStatus());
        verify(tableStorage, times(2)).upsertReport(any());
        verify(blobStorage, times(1)).uploadReport(anyString(), any());
    }

    @Test
    public void testGetReportStatus() {
        String executionId = UUID.randomUUID().toString();
        ReportEntity entity = ReportEntity.builder().executionId(executionId)
                .reportId(UUID.randomUUID().toString()).status(ReportStatus.COMPLETED)
                .fileSize(1024L).filePath("generated/report.pdf").build();

        when(tableStorage.getReport(executionId)).thenReturn(Optional.of(entity));
        when(blobStorage.generateAccessUrl("generated/report.pdf"))
                .thenReturn(Optional.of("http://url"));

        Optional<ReportProcessingStatus> result = service.getReportStatus(executionId, context);

        assertTrue(result.isPresent());
        assertEquals(ReportStatus.COMPLETED, result.get().getStatus());
        assertEquals("http://url", result.get().getFilePath());
        verify(blobStorage, times(1)).generateAccessUrl("generated/report.pdf");
    }

    @Test
    public void testGetReportStatusNotFound() {
        String executionId = UUID.randomUUID().toString();

        when(tableStorage.getReport(executionId)).thenReturn(Optional.empty());

        Optional<ReportProcessingStatus> result = service.getReportStatus(executionId, context);

        assertFalse(result.isPresent());
    }
}
