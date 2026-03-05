package com.rhsoft.function;

import java.util.Optional;
import com.google.inject.Inject;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import com.rhsoft.service.ReportGeneratorService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
public class FunctionCleaner {

    @Inject
    ReportGeneratorService reportService;

    @FunctionName("Report-Cleaner")
    public void run(
                @TimerTrigger(name = "timer", schedule = "0 0 3 * * *") String timerInfo, 
            final ExecutionContext context) {

        context.getLogger().info("Removing tagged blobs");

        Optional<Integer> deletedCount = reportService.deleteRemovedReports(context);
        deletedCount.ifPresent(count -> context.getLogger().info("Deleted reports: " + count));

    }
}
