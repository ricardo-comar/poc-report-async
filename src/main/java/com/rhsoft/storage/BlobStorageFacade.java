package com.rhsoft.storage;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.util.Optional;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.rhsoft.ApplicationConstants;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class BlobStorageFacade {

    private BlobContainerClient containerClient;

    public BlobStorageFacade() {

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(System.getenv(ApplicationConstants.AZURE_WEB_JOBS_STORAGE))
                .buildClient();

        blobServiceClient.createBlobContainerIfNotExists(ApplicationConstants.BLOB_CONTAINER_NAME);

        containerClient = blobServiceClient
                .getBlobContainerClient(ApplicationConstants.BLOB_CONTAINER_NAME);
    }

    public void uploadReport(String blobName, byte[] content) {
        ByteArrayInputStream dataStream = new ByteArrayInputStream(content);
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.upload(dataStream, true);
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
}
