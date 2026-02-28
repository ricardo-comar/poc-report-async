package com.rhsoft;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.rhsoft.model.ReportProcessingStatus;
import com.rhsoft.model.ReportRequest;
import com.rhsoft.model.ReportStatus;

/**
 * Azure Functions with HTTP Trigger.
 */ 
public class FunctionHandler {
    @FunctionName("Report-Handler")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", route = "report", methods = {
                    HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<ReportRequest>> request,
            final ExecutionContext context) {

        context.getLogger().info("Report Handler triggered");

        if (request.getBody().isEmpty() || request.getBody().get().getExecutionId() == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                            .body("Please provide report data in the request body").build();
        }

        return request.createResponseBuilder(HttpStatus.ACCEPTED)
                .body(ReportProcessingStatus.builder().reportId(UUID.randomUUID()).status(ReportStatus.PENDING)
                                        .requestedAt(LocalDateTime.now().toString()).build())
                .build();

    }
}
