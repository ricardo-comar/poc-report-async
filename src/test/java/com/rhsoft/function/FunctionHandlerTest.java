package com.rhsoft.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.OutputBinding;
import com.rhsoft.model.ReportMessage;
import com.rhsoft.model.ReportProcessingStatus;
import com.rhsoft.model.ReportRequest;
import com.rhsoft.model.ReportStatus;
import com.rhsoft.service.ReportGeneratorService;

/**
 * Unit test for Function class.
 */
public class FunctionHandlerTest extends BaseFunctionTest {

    private ReportRequest reportRequest;
    private ReportProcessingStatus reportStatus;
    private OutputBinding<ReportMessage> outQueue;
    private ReportGeneratorService mockService;
    private FunctionHandler functionHandler;

    @SuppressWarnings("unchecked")
    public void beforeEachTest() {

        reportRequest = new ReportRequest();
        reportRequest.setReportId(UUID.randomUUID());

        reportStatus = ReportProcessingStatus.builder().executionId(UUID.randomUUID())
                .reportId(reportRequest.getReportId()).status(ReportStatus.PENDING)
                .requestedAt(LocalDateTime.now().toString()).build();

        mockService = mock(ReportGeneratorService.class);
        doReturn(reportStatus).when(mockService).requestReport(any(), eq(context));

        final Optional<ReportRequest> queryBody = Optional.of(reportRequest);
        doReturn(queryBody).when(req).getBody();

        outQueue = mock(OutputBinding.class);
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                ReportMessage message = invocation.getArgument(0);
                assertEquals(message.getReportId(), queryBody.get().getReportId().toString());
                return null;
            }
        }).when(outQueue).setValue(any());

        functionHandler = new FunctionHandler(mockService);
    }

    @Test
    public void testAccepted() throws Exception {

        final HttpResponseMessage ret = functionHandler.run(req, outQueue, context);
        verify(outQueue, times(1)).setValue(any());

        assertEquals(ret.getStatus(), HttpStatus.ACCEPTED);
        assertEquals(ret.getBody(), reportStatus);
    }

    @Test
    public void testInvalidBody() throws Exception {

        reportRequest.setReportId(null);

        final HttpResponseMessage ret = functionHandler.run(req, outQueue, context);
        verify(outQueue, times(0)).setValue(any());

        assertEquals(ret.getStatus(), HttpStatus.BAD_REQUEST);

        doReturn(Optional.empty()).when(req).getBody();

        final HttpResponseMessage ret2 = functionHandler.run(req, outQueue, context);
        verify(outQueue, times(0)).setValue(any());

        assertEquals(ret2.getStatus(), HttpStatus.BAD_REQUEST);
    }
}
