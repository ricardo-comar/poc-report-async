package com.rhsoft.function;

import com.google.inject.Inject;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.QueueTrigger;
import com.rhsoft.ApplicationConstants;
import com.rhsoft.model.ReportMessage;
import com.rhsoft.service.ReportGeneratorService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
public class FunctionGenerator {

    @Inject
    ReportGeneratorService reportGeneratorService;

    @FunctionName("Report-Generator")
    public void run(
            @QueueTrigger(name = "queueMessage",
                    connection = ApplicationConstants.AZURE_WEB_JOBS_STORAGE,
                            queueName = ApplicationConstants.QUEUE_NAME) ReportMessage queueMessage,
            final ExecutionContext context) {
        // Process the message from the queue
        context.getLogger().info("Processing message: " + queueMessage);

        reportGeneratorService.generateReport(queueMessage, context);

        context.getLogger().info(
                "Message processing completed for executionId: " + queueMessage.getExecutionId());
    }
}
