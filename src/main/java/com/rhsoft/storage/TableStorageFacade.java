package com.rhsoft.storage;

import java.util.Optional;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableServiceClient;
import com.azure.data.tables.TableServiceClientBuilder;
import com.azure.data.tables.models.TableEntity;
import com.rhsoft.ApplicationConstants;
import com.rhsoft.mapper.EntityMapper;
import com.rhsoft.model.ReportEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class TableStorageFacade {

    private static final EntityMapper mapper = EntityMapper.getInstance();

    private TableClient tableClient;

    public TableStorageFacade() {

        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
                .connectionString(System.getenv(ApplicationConstants.AZURE_WEB_JOBS_STORAGE))
                .buildClient();

        tableServiceClient.createTableIfNotExists(ApplicationConstants.TABLE_NAME);

        this.tableClient = tableServiceClient.getTableClient(ApplicationConstants.TABLE_NAME);

    }

    public void upsertReport(ReportEntity report) {
        TableEntity entity = mapper.toTableEntity(report);
        tableClient.upsertEntity(entity);
    }

    public Optional<ReportEntity> getReport(String executionId) {
        try {
            TableEntity entity = tableClient.getEntity(ApplicationConstants.PARTITION_KEY, executionId);
            return Optional.ofNullable(mapper.toReportEntity(entity));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
