package com.rhsoft.function;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.rhsoft.model.ReportMessage;
import com.rhsoft.service.ReportGeneratorService;


/**
 * Unit test for Function class.
 */
public class FunctionGeneratorTest extends BaseFunctionTest {

    private ReportMessage queueMessage;
    private ReportGeneratorService mockService;
    private FunctionGenerator functionGenerator;

    public void beforeEachTest() {
        queueMessage = ReportMessage.builder().executionId(UUID.randomUUID().toString()).build();

        mockService = mock(ReportGeneratorService.class);
        doNothing().when(mockService).generateReport(eq(queueMessage), eq(context));

        functionGenerator = new FunctionGenerator(mockService);
    }

    @Test
    public void testSuccess() throws Exception {

        functionGenerator.run(queueMessage, context);
        verify(mockService, times(1)).generateReport(eq(queueMessage), eq(context));

    }

}
