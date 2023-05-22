package it.pagopa.selfcare.external_api.core;


import it.pagopa.selfcare.external_api.api.FileStorageConnector;
import it.pagopa.selfcare.external_api.api.MsCoreConnector;
import it.pagopa.selfcare.external_api.model.documents.ResourceResponse;
import it.pagopa.selfcare.external_api.model.token.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ContractServiceImpl implements ContractService {
    private final FileStorageConnector fileStorageConnector;
    private final MsCoreConnector msCoreConnector;

    @Autowired
    public ContractServiceImpl(FileStorageConnector fileStorageConnector, MsCoreConnector msCoreConnector) {
        this.fileStorageConnector = fileStorageConnector;
        this.msCoreConnector = msCoreConnector;
    }

    @Override
    public ResourceResponse getContract(String institutionId, String productId) {
        Token token = msCoreConnector.getToken(institutionId, productId);
        ResourceResponse file = fileStorageConnector.getFile(token.getContractSigned());
        return file;
    }
}
