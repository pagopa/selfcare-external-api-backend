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
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class ContractServiceImpl implements ContractService {
    public static final String TOKEN_FOR_S_AND_S_FOUND_BUT_CONTRACT_SIGNED_REFERENCE_IS_EMPTY = "Token for %s and %s found but contract signed reference is empty!";
    public static final String TOKEN_FOR_S_AND_S_NOT_FOUND = "Token for %s and %s not found!";
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
            throw new ResourceNotFoundException(String.format(TOKEN_FOR_S_AND_S_NOT_FOUND, institutionId, productId));
        if(!StringUtils.hasText(tokens.get(0).getContractSigned()))
            throw new ResourceNotFoundException(String.format(TOKEN_FOR_S_AND_S_FOUND_BUT_CONTRACT_SIGNED_REFERENCE_IS_EMPTY, institutionId, productId));
        ResourceResponse file = fileStorageConnector.getFile(tokens.get(0).getContractSigned());
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getContract result = {}", file);
        log.trace("getContract end");
        return file;
    }
}
