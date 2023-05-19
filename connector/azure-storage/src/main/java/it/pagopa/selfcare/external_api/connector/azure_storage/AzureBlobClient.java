package it.pagopa.selfcare.external_api.connector.azure_storage;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobProperties;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import it.pagopa.selfcare.external_api.api.FileStorageConnector;
import it.pagopa.selfcare.external_api.connector.azure_storage.config.AzureStorageConfig;
import it.pagopa.selfcare.external_api.exceptions.AzureRestClientException;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.documents.ResourceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;

import static it.pagopa.selfcare.external_api.model.constant.GenericError.ERROR_DURING_DOWNLOAD_FILE;


@Slf4j
@Service
public class AzureBlobClient implements FileStorageConnector {

    private final CloudBlobClient blobClient;
    private final AzureStorageConfig azureStorageConfig;

    AzureBlobClient(AzureStorageConfig azureStorageConfig) throws URISyntaxException {
        log.trace("AzureBlobClient.AzureBlobClient");
        log.debug("storageConnectionString = {}", azureStorageConfig.getConnectionString());
        this.azureStorageConfig = azureStorageConfig;
        final CloudStorageAccount storageAccount = buildStorageAccount();
        this.blobClient = storageAccount.createCloudBlobClient();
    }

    private CloudStorageAccount buildStorageAccount() throws URISyntaxException {
        StorageCredentials storageCredentials = new StorageCredentialsAccountAndKey(azureStorageConfig.getAccountName(), azureStorageConfig.getAccountKey());
        return new CloudStorageAccount(storageCredentials,
                true,
                azureStorageConfig.getEndpointSuffix(),
                azureStorageConfig.getAccountName());
    }

    public ResourceResponse getFile(String fileName) {
        log.trace("getFile start");
        log.debug("getFile fileName = {}", fileName);
        try {
            ResourceResponse response = new ResourceResponse();
            final CloudBlobContainer blobContainer = blobClient.getContainerReference(azureStorageConfig.getContractsTemplateContainer());
            final CloudBlockBlob blob = blobContainer.getBlockBlobReference(fileName);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BlobProperties properties = blob.getProperties();
            blob.download(outputStream);
            log.info("END - getFile - path {}", fileName);
            response.setData(outputStream.toByteArray());
            response.setFileName(blob.getName());
            response.setMimetype(properties.getContentType());
            return response;
        } catch (StorageException e) {
            if(e.getHttpStatusCode() == 404){
                throw new ResourceNotFoundException(String.format(ERROR_DURING_DOWNLOAD_FILE.getMessage(), fileName),
                        ERROR_DURING_DOWNLOAD_FILE.getCode());
            }
            throw new AzureRestClientException(String.format(ERROR_DURING_DOWNLOAD_FILE.getMessage(), fileName),
                    ERROR_DURING_DOWNLOAD_FILE.getCode());
        }catch (URISyntaxException e){
            throw new AzureRestClientException(String.format(ERROR_DURING_DOWNLOAD_FILE.getMessage(), fileName),
                    ERROR_DURING_DOWNLOAD_FILE.getCode());
        }
    }

}
