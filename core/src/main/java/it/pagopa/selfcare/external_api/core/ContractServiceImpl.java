package it.pagopa.selfcare.external_api.core;


import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.external_api.api.FileStorageConnector;
import it.pagopa.selfcare.external_api.api.MsCoreConnector;
import it.pagopa.selfcare.external_api.api.OnboardingMsConnector;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.documents.ResourceResponse;
import it.pagopa.selfcare.external_api.model.onboarding.InstitutionOnboarding;
import it.pagopa.selfcare.external_api.model.token.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class ContractServiceImpl implements ContractService {
    private final FileStorageConnector fileStorageConnector;
    private final MsCoreConnector msCoreConnector;
    private final OnboardingMsConnector onboardingMsConnector;

    public ContractServiceImpl(FileStorageConnector fileStorageConnector, MsCoreConnector msCoreConnector, OnboardingMsConnector onboardingMsConnector) {
        this.onboardingMsConnector = onboardingMsConnector;
        log.trace("Initializing {}...", ContractServiceImpl.class.getSimpleName());
        this.fileStorageConnector = fileStorageConnector;
        this.msCoreConnector = msCoreConnector;
    }

    @Override
    public ResourceResponse getContractV2(String institutionId, String productId) {
        log.trace("getContract start");
        log.debug("getContract institutionId = {}, productId = {}", institutionId, productId);

        InstitutionOnboarding institutionOnboarding = msCoreConnector.getInstitutionOnboardings(institutionId, productId);

        List<Token> tokens = onboardingMsConnector.getToken(institutionOnboarding.getTokenId());
        if(Objects.isNull(tokens) || tokens.isEmpty())
            throw new ResourceNotFoundException("Token not found!");
        ResourceResponse file = fileStorageConnector.getFile(tokens.get(0).getContractSigned());
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getContract result = {}", file);
        log.trace("getContract end");
        return file;
    }
}
