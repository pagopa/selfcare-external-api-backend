package it.pagopa.selfcare.external_api.service;


import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.OnboardingResponse;
import it.pagopa.selfcare.external_api.client.MsCoreInstitutionApiClient;
import it.pagopa.selfcare.external_api.client.MsOnboardingTokenControllerApi;
import it.pagopa.selfcare.external_api.connector.FileStorageConnector;
import it.pagopa.selfcare.external_api.exception.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.mapper.InstitutionMapper;
import it.pagopa.selfcare.external_api.mapper.TokenMapper;
import it.pagopa.selfcare.external_api.model.document.ResourceResponse;
import it.pagopa.selfcare.external_api.model.onboarding.InstitutionOnboarding;
import it.pagopa.selfcare.external_api.model.token.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class ContractServiceImpl implements ContractService {
    public static final String TOKEN_FOR_S_AND_S_FOUND_BUT_CONTRACT_SIGNED_REFERENCE_IS_EMPTY = "Token for %s and %s found but contract signed reference is empty!";
    public static final String TOKEN_FOR_S_AND_S_NOT_FOUND = "Token for %s and %s not found!";
    private final FileStorageConnector fileStorageConnector;
    private final MsCoreInstitutionApiClient institutionApiClient;

    private final InstitutionMapper institutionMapper;
    private final MsOnboardingTokenControllerApi tokenControllerApi;
    private final TokenMapper tokenMapper;
    public ContractServiceImpl(FileStorageConnector fileStorageConnector,
                               MsCoreInstitutionApiClient institutionApiClient,
                               InstitutionMapper institutionMapper,
                               MsOnboardingTokenControllerApi tokenControllerApi,
                               TokenMapper tokenMapper) {
        this.institutionApiClient = institutionApiClient;
        this.institutionMapper = institutionMapper;
        this.tokenControllerApi = tokenControllerApi;
        this.tokenMapper = tokenMapper;
        log.trace("Initializing {}...", ContractServiceImpl.class.getSimpleName());
        this.fileStorageConnector = fileStorageConnector;
    }

    @Override
    public ResourceResponse getContractV2(String institutionId, String productId) {
        log.trace("getContract start");
        log.debug("getContract institutionId = {}, productId = {}", institutionId, productId);

        List<OnboardingResponse> onboardings = Objects.requireNonNull(institutionApiClient.
                        _getOnboardingsInstitutionUsingGET(institutionId, productId)
                        .getBody())
                .getOnboardings();

        InstitutionOnboarding institutionOnboarding =  onboardings.stream()
                .findFirst()
                .map(institutionMapper::toEntity)
                .orElseThrow(ResourceNotFoundException::new);

        List<Token> tokens =  Objects.requireNonNull(tokenControllerApi._v1TokensGet(institutionOnboarding.getTokenId()).getBody()).stream()
                .map(tokenMapper::toEntity)
                .toList();

        if(CollectionUtils.isEmpty(tokens))
            throw new ResourceNotFoundException(String.format(TOKEN_FOR_S_AND_S_NOT_FOUND, institutionId, productId));
        if(!StringUtils.hasText(tokens.get(0).getContractSigned()))
            throw new ResourceNotFoundException(String.format(TOKEN_FOR_S_AND_S_FOUND_BUT_CONTRACT_SIGNED_REFERENCE_IS_EMPTY, institutionId, productId));
        ResourceResponse file = fileStorageConnector.getFile(tokens.get(0).getContractSigned());
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getContract result = {}", file);
        log.trace("getContract end");
        return file;
    }
}
