package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.InstitutionResponse;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.OnboardingResponse;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.UserProductsResponse;
import it.pagopa.selfcare.external_api.api.MsCoreConnector;
import it.pagopa.selfcare.external_api.connector.rest.client.MsCoreInstitutionApiClient;
import it.pagopa.selfcare.external_api.connector.rest.client.MsCoreRestClient;
import it.pagopa.selfcare.external_api.connector.rest.client.MsCoreUserApiClient;
import it.pagopa.selfcare.external_api.connector.rest.mapper.InstitutionMapper;
import it.pagopa.selfcare.external_api.connector.rest.mapper.UserProductMapper;
import it.pagopa.selfcare.external_api.connector.rest.model.pnpg.CreatePnPgInstitutionRequest;
import it.pagopa.selfcare.external_api.connector.rest.model.pnpg.InstitutionPnPgResponse;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.onboarding.InstitutionOnboarding;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionInfo;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingInfoResponse;
import it.pagopa.selfcare.external_api.model.onboarding.ProductInfo;
import it.pagopa.selfcare.external_api.model.pnpg.CreatePnPgInstitution;
import it.pagopa.selfcare.external_api.model.token.Token;
import it.pagopa.selfcare.external_api.model.token.TokenUser;
import it.pagopa.selfcare.external_api.model.user.RelationshipState;
import it.pagopa.selfcare.external_api.model.user.UserProducts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MsCoreConnectorImpl implements MsCoreConnector {

    private final MsCoreRestClient restClient;
    private final MsCoreInstitutionApiClient institutionApiClient;
    private final MsCoreUserApiClient userApiClient;
    private final InstitutionMapper institutionMapper;
    private final UserProductMapper userProductMapper;

    @Autowired
    public MsCoreConnectorImpl(MsCoreRestClient restClient,
                               MsCoreInstitutionApiClient institutionApiClient,
                               MsCoreUserApiClient userApiClient,
                               InstitutionMapper institutionMapper,
                               UserProductMapper userProductMapper) {
        this.restClient = restClient;
        this.institutionApiClient = institutionApiClient;
        this.institutionMapper = institutionMapper;
        this.userApiClient = userApiClient;
        this.userProductMapper = userProductMapper;
    }

    @Override
    public String createPnPgInstitution(CreatePnPgInstitution request) {
        log.trace("createPnPgInstitution start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "createPnPgInstitution request = {}", request);
        CreatePnPgInstitutionRequest institutionRequest = new CreatePnPgInstitutionRequest(request);
        InstitutionPnPgResponse pnPgInstitution = restClient.createPnPgInstitution(institutionRequest);
        log.debug("createPnPgInstitution result = {}", pnPgInstitution.getId());
        log.trace("createPnPgInstitution end");
        return pnPgInstitution.getId();
    }

    @Override
    public Token getToken(String institutionId, String productId) {
        log.trace("getToken start");
        log.debug("getToken institutionId = {}, productId = {}", institutionId, productId);
        Token token = restClient.getToken(institutionId, productId);
        log.debug("getToken result = {}", token);
        log.trace("getToken end");
        return token;
    }

    @Override
    public OnboardingInfoResponse getInstitutionProductsInfo(String userId) {
        log.trace("getInstitutionProductsInfo start");
        log.debug("getInstitutionProductsInfo userId = {}", userId);
        OnboardingInfoResponse onboardingInfo = restClient.getInstitutionProductsInfo(userId);
        log.debug("getInstitutionProductsInfo result = {}", onboardingInfo);
        log.trace("getInstitutionProductsInfo end");
        return onboardingInfo;
    }

    @Override
    public OnboardingInfoResponse getInstitutionProductsInfo(String userId, List<RelationshipState> userStatuses) {
        log.trace("getInstitutionProductsInfo start");
        log.debug("getInstitutionProductsInfo userId = {}", userId);
        OnboardingInfoResponse onboardingInfo = restClient.getInstitutionProductsInfo(userId, Objects.nonNull(userStatuses)
                ? userStatuses.stream().map(RelationshipState::name).toArray(String[]::new) : null);
        log.debug("getInstitutionProductsInfo result = {}", onboardingInfo);
        log.trace("getInstitutionProductsInfo end");
        return onboardingInfo;
    }

    @Override
    public InstitutionOnboarding getInstitutionOnboardings(String institutionId, String productId) {
        log.trace("getInstitutionOnboardings start");
        log.debug("getInstitutionOnboardings institutionId = {}, productId = {}", institutionId, productId);
        List<OnboardingResponse> onboardings = Objects.requireNonNull(institutionApiClient.
                        _getOnboardingsInstitutionUsingGET(institutionId, productId)
                        .getBody())
                .getOnboardings();

        log.trace("getInstitutionOnboardings end");

        return onboardings.stream()
                .findFirst()
                .map(institutionMapper::toEntity)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public List<UserProducts> getOnboarderUsers(List<TokenUser> users) {
        log.trace("getOnboarderUsers start");
        List<String> userIds = users.stream().map(TokenUser::getUserId).collect(Collectors.toList());
        List<UserProductsResponse> onboardedUsers = Objects.requireNonNull(userApiClient._getOnboardedUsersUsingGET(userIds)
                        .getBody())
                .getUsers();

        log.trace("getOnboarderUsers end");
        return onboardedUsers.stream().map(userProductMapper::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<OnboardedInstitutionInfo> getInstitutionDetails(String institutionId) {
        try {
            ResponseEntity<InstitutionResponse> responseEntity = institutionApiClient._retrieveInstitutionByIdUsingGET(institutionId);
            if (Objects.isNull(responseEntity) || Objects.isNull(responseEntity.getBody()) || Objects.isNull(responseEntity.getBody().getOnboarding())) {
                return Collections.emptyList();
            } else {
                return responseEntity.getBody().getOnboarding().stream().map(onboardedProductResponse -> {
                    OnboardedInstitutionInfo onboardedInstitutionInfo = institutionMapper.toOnboardedInstitution(responseEntity.getBody());
                    ProductInfo productInfo = institutionMapper.toProductInfo(onboardedProductResponse);
                    onboardedInstitutionInfo.setProductInfo(productInfo);
                    onboardedInstitutionInfo.setState(productInfo.getStatus());
                    return onboardedInstitutionInfo;
                }).toList();
            }
        } catch (Exception e) {
            log.error("Impossible to retrieve institution with ID: {}", institutionId, e);
            return Collections.emptyList();
        }
    }
}
