package com.rhsoft.function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Optional;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.rhsoft.model.ReportRequest;

public abstract class BaseFunctionTest {

    HttpRequestMessage<Optional<ReportRequest>> req;
    ExecutionContext context;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void beforeEach() {
        req = mock(HttpRequestMessage.class);

        doAnswer(new Answer<HttpResponseMessage.Builder>() {
            @Override
            public HttpResponseMessage.Builder answer(InvocationOnMock invocation) {
                HttpStatus status = (HttpStatus) invocation.getArguments()[0];
                return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(status);
            }
        }).when(req).createResponseBuilder(any(HttpStatus.class));

        context = mock(ExecutionContext.class);
        doReturn(Logger.getGlobal()).when(context).getLogger();
    }

}
