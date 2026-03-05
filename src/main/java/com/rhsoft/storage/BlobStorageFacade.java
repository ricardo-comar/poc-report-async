package com.rhsoft.storage;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.google.inject.Singleton;
import com.microsoft.azure.functions.ExecutionContext;
import com.rhsoft.ApplicationConstants;
import com.rhsoft.ioc.AfterInjection;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Singleton
public class BlobStorageFacade {

    BlobContainerClient containerClient;

    @AfterInjection
    public void initClient() {

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(System.getenv(ApplicationConstants.AZURE_WEB_JOBS_STORAGE))
                .buildClient();

        blobServiceClient.createBlobContainerIfNotExists(ApplicationConstants.BLOB_CONTAINER_NAME);

        containerClient = blobServiceClient
                .getBlobContainerClient(ApplicationConstants.BLOB_CONTAINER_NAME);
    }

    public void uploadReport(String blobName, byte[] content, Map<String, String> metadataFields) {
        ByteArrayInputStream dataStream = new ByteArrayInputStream(content);
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.upload(dataStream, true);
        blobClient.setMetadata(metadataFields);
    }

    public Optional<String> generateAccessUrl(String blobName) {
        if (blobName == null || !containerClient.getBlobClient(blobName).exists()) {
            return Optional.empty();
        }

        BlobClient blobClient = containerClient.getBlobClient(blobName);
        String sasToken = blobClient
                .generateSas(new BlobServiceSasSignatureValues(OffsetDateTime.now().plusMinutes(10),
                        new BlobSasPermission().setReadPermission(true))
                        .setStartTime(OffsetDateTime.now()));

        return Optional.of(String.format("%s?%s", blobClient.getBlobUrl(), sasToken));
    }

    public Optional<List<String>> listBlobsToBeDeleted(final ExecutionContext context) {

            ListBlobsOptions options = new ListBlobsOptions()
                            .setPrefix(ApplicationConstants.BLOB_PREFIX)
                            .setDetails(new BlobListDetails().setRetrieveDeletedBlobs(false)
                                            .setRetrieveTags(true).setRetrieveMetadata(true));

            return Optional.of(containerClient
                            .listBlobsByHierarchy("/", options, Duration.ofSeconds(10)).stream()
                            .filter(blobItem -> "pending-logical-delete"
                                            .equals(blobItem.getTags().get("phase")))
                            .map(blobItem -> blobItem.getName()).toList());
    }
}
