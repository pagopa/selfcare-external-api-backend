package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.model.documents.ResourceResponse;

public interface ContractService {

    ResourceResponse getContract(String institutionId, String productId);

    ResourceResponse getContractV2(String institutionId, String productId);
}
