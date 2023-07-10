package it.pagopa.selfcare.external_api.core;


import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.external_api.api.FileStorageConnector;
import it.pagopa.selfcare.external_api.api.MsCoreConnector;
import it.pagopa.selfcare.external_api.model.documents.ResourceResponse;
import it.pagopa.selfcare.external_api.model.token.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ContractServiceImpl implements ContractService {
    private final FileStorageConnector fileStorageConnector;
    private final MsCoreConnector msCoreConnector;

    public ContractServiceImpl(FileStorageConnector fileStorageConnector, MsCoreConnector msCoreConnector) {
        log.trace("Initializing {}...", ContractServiceImpl.class.getSimpleName());
        this.fileStorageConnector = fileStorageConnector;
        this.msCoreConnector = msCoreConnector;
    }

    @Override
    public ResourceResponse getContract(String institutionId, String productId) {
        log.trace("getContract start");
        log.debug("getContract institutionId = {}, productId = {}", institutionId, productId);
        Token token = msCoreConnector.getToken(institutionId, productId);
        ResourceResponse file = fileStorageConnector.getFile(token.getContractSigned());
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getContract result = {}", file);
        log.trace("getContract end");
        return file;
    }
}
