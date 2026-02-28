package com.rhsoft.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.OutputBinding;
import com.rhsoft.model.ReportMessage;
import com.rhsoft.model.ReportRequest;

/**
 * Unit test for Function class.
 */
public class FunctionHandlerTest extends BaseFunctionTest {
    /**
     * Unit test for HttpTriggerJava method.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testAccepted() throws Exception {

        ReportRequest reportRequest = new ReportRequest();
        reportRequest.setReportId(UUID.randomUUID());

        final Optional<ReportRequest> queryBody = Optional.of(reportRequest);
        doReturn(queryBody).when(req).getBody();

        OutputBinding<ReportMessage> outQueue = mock(OutputBinding.class);
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                ReportMessage message = invocation.getArgument(0);
                assertEquals(message.getReportId(), queryBody.get().getReportId().toString());
                return null;
            }
        }).when(outQueue).setValue(any());

        // Invoke
        final HttpResponseMessage ret = new FunctionHandler().run(req, outQueue, context);
        verify(outQueue, times(1)).setValue(any());

        // Verify
        assertEquals(ret.getStatus(), HttpStatus.ACCEPTED);
    }
}
