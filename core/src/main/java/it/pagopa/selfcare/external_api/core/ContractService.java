package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.model.documents.ResourceResponse;

public interface ContractService {

    ResourceResponse getContractV2(String institutionId, String productId);
}
