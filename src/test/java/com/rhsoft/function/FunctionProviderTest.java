package com.rhsoft.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.rhsoft.model.ReportProcessingStatus;
import com.rhsoft.model.ReportStatus;
import com.rhsoft.service.ReportGeneratorService;


/**
 * Unit test for Function class.
 */
public class FunctionProviderTest extends BaseFunctionTest {

    private UUID executionId;
    private ReportGeneratorService mockService;
    private ReportProcessingStatus reportStatus;
    private FunctionProvider functionProvider;

    public void beforeEachTest() {
        executionId = UUID.randomUUID();

        reportStatus = ReportProcessingStatus.builder().executionId(executionId)
                .status(ReportStatus.PENDING).requestedAt(LocalDateTime.now().toString()).build();

        mockService = mock(ReportGeneratorService.class);
        doReturn(Optional.of(reportStatus)).when(mockService)
                .getReportStatus(eq(executionId.toString()), eq(context));

        functionProvider = new FunctionProvider(mockService);
    }

    @Test
    public void testQueued() throws Exception {

        final HttpResponseMessage ret = functionProvider.run(req, executionId.toString(), context);

        assertEquals(ret.getStatus(), HttpStatus.OK);
    }

    @Test
    public void testProcessing() throws Exception {

        reportStatus.setStatus(ReportStatus.PROCESSING);
        final HttpResponseMessage ret = functionProvider.run(req, executionId.toString(), context);

        assertEquals(ret.getStatus(), HttpStatus.OK);
    }

    @Test
    public void testCompleted() throws Exception {

        reportStatus.setStatus(ReportStatus.COMPLETED);
        final HttpResponseMessage ret = functionProvider.run(req, executionId.toString(), context);

        assertEquals(ret.getStatus(), HttpStatus.CREATED);
    }

    @Test
    public void testInvalid() throws Exception {

        reportStatus.setStatus(ReportStatus.INVALID);
        final HttpResponseMessage ret = functionProvider.run(req, executionId.toString(), context);

        assertEquals(ret.getStatus(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void testFailed() throws Exception {

        reportStatus.setStatus(ReportStatus.FAILED);
        final HttpResponseMessage ret = functionProvider.run(req, executionId.toString(), context);

        assertEquals(ret.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testNotFound() throws Exception {

        doReturn(Optional.empty()).when(mockService).getReportStatus(eq(executionId.toString()),
                eq(context));
        final HttpResponseMessage ret = functionProvider.run(req, executionId.toString(), context);

        assertEquals(ret.getStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void testPayloadError() throws Exception {

        final HttpResponseMessage ret = functionProvider.run(req, "", context);
        assertEquals(ret.getStatus(), HttpStatus.NOT_FOUND);

        final HttpResponseMessage ret2 = functionProvider.run(req, null, context);
        assertEquals(ret2.getStatus(), HttpStatus.NOT_FOUND);
    }
}
