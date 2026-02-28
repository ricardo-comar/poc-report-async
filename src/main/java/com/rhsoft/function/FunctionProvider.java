package com.rhsoft.function;

import java.util.Optional;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.rhsoft.model.ReportProcessingStatus;
import com.rhsoft.model.ReportRequest;
import com.rhsoft.service.ReportGeneratorService;

import lombok.NoArgsConstructor;

/**
 * Azure Functions with HTTP Trigger.
 */
@NoArgsConstructor 
public class FunctionProvider {

    private ReportGeneratorService reportGeneratorService = new ReportGeneratorService();

    FunctionProvider(ReportGeneratorService reportGeneratorService) {
        this.reportGeneratorService = reportGeneratorService;
    }

    @FunctionName("Report-Provider")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", route = "report/{executionId}", methods = {
                    HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<ReportRequest>> request,
            @BindingName("executionId") String executionId,
            final ExecutionContext context) {

        if (executionId == null || executionId.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Please provide executionId in the URL").build();
        }

        context.getLogger().info("Report Provider triggered");

        Optional<ReportProcessingStatus> reportStatus = reportGeneratorService.getReportStatus(executionId, context);

        HttpStatus status = reportStatus.map(
                report -> {
                    switch (report.getStatus()) {
                        case PENDING:
                        case PROCESSING:
                            return HttpStatus.OK;
                        case COMPLETED:
                            return HttpStatus.CREATED;
                        case INVALID:
                            return HttpStatus.BAD_REQUEST;
                        case FAILED:
                            return HttpStatus.INTERNAL_SERVER_ERROR;
                        default:
                            return HttpStatus.INTERNAL_SERVER_ERROR;
                    }

                }).orElse(HttpStatus.NOT_FOUND);

        return request.createResponseBuilder(status)
                .body(reportStatus.orElse(null)).build();

    }
}
