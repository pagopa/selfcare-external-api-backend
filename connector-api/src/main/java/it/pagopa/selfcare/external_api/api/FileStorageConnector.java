package it.pagopa.selfcare.external_api.api;

import it.pagopa.selfcare.external_api.model.documents.ResourceResponse;

public interface FileStorageConnector {

    ResourceResponse getFile(String fileName);

}
