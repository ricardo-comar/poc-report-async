package com.rhsoft.function;

import java.util.Optional;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.OutputBinding;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.microsoft.azure.functions.annotation.QueueOutput;
import com.rhsoft.ApplicationConstants;
import com.rhsoft.model.ReportMessage;
import com.rhsoft.model.ReportProcessingStatus;
import com.rhsoft.model.ReportRequest;
import com.rhsoft.service.ReportGeneratorService;

import lombok.NoArgsConstructor;

/**
 * Azure Functions with HTTP Trigger.
 */
@NoArgsConstructor
public class FunctionHandler {
        private ReportGeneratorService reportGeneratorService = new ReportGeneratorService();

        FunctionHandler(ReportGeneratorService reportGeneratorService) {
                this.reportGeneratorService = reportGeneratorService;
        }

    @FunctionName("Report-Handler")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", route = "report", methods = {
                    HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<ReportRequest>> request,
                    @QueueOutput(name = "outQueueMsg", connection = ApplicationConstants.AZURE_WEB_JOBS_STORAGE, queueName = ApplicationConstants.QUEUE_NAME) OutputBinding<ReportMessage> outQueue,
                                    final ExecutionContext context) {

        context.getLogger().info("Report Handler triggered");

        if (request.getBody().isEmpty() || request.getBody().get().getReportId() == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                            .body("Please provide report data in the request body").build();
        }
        ReportRequest reportRequest = request.getBody().get();

        ReportProcessingStatus reportStatus = reportGeneratorService.requestReport(reportRequest, context);

        outQueue.setValue(ReportMessage.builder().reportId(reportStatus.getReportId().toString())
                        .executionId(reportStatus.getExecutionId().toString())
                        .request(reportRequest).build());

        return request.createResponseBuilder(HttpStatus.ACCEPTED)
                        .body(reportStatus)
                .build();

    }
}
