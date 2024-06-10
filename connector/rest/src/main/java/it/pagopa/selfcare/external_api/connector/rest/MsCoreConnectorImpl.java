package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.CreatePgInstitutionRequest;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.InstitutionResponse;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.OnboardingResponse;
import it.pagopa.selfcare.external_api.api.MsCoreConnector;
import it.pagopa.selfcare.external_api.connector.rest.client.MsCoreInstitutionApiClient;
import it.pagopa.selfcare.external_api.connector.rest.client.MsCoreRestClient;
import it.pagopa.selfcare.external_api.connector.rest.client.MsUserApiRestClient;
import it.pagopa.selfcare.external_api.connector.rest.mapper.InstitutionMapper;
import it.pagopa.selfcare.external_api.connector.rest.model.pnpg.CreatePnPgInstitutionRequest;
import it.pagopa.selfcare.external_api.connector.rest.model.pnpg.InstitutionPnPgResponse;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.institutions.GeographicTaxonomy;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.institutions.SearchMode;
import it.pagopa.selfcare.external_api.model.onboarding.InstitutionOnboarding;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionInfo;
import it.pagopa.selfcare.external_api.model.pnpg.CreatePnPgInstitution;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.OnboardedProductResponse;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.UserDataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.validation.ValidationException;
import java.util.*;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.external_api.model.user.RelationshipState.ACTIVE;

@Service
@Slf4j
@RequiredArgsConstructor
public class MsCoreConnectorImpl implements MsCoreConnector {

    private final MsCoreRestClient restClient;
    private final MsCoreInstitutionApiClient institutionApiClient;
    private final InstitutionMapper institutionMapper;
    private final MsCoreRestClient msCoreRestClient;
    private final MsUserApiRestClient msUserApiRestClient;

    protected static final String INSTITUTION_ID_IS_REQUIRED = "An institutionId is required ";
    protected static final String USER_ID_IS_REQUIRED = "A userId is required";
    protected static final String REQUIRED_INSTITUTION_ID_MESSAGE = "An Institution external id is required";

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
    public Institution getInstitutionByExternalId(String externalInstitutionId) {
        log.trace("getInstitution start");
        Assert.hasText(externalInstitutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        Institution result = msCoreRestClient.getInstitutionByExternalId(externalInstitutionId);
        log.debug("getInstitution result = {}", result);
        log.trace("getInstitution end");
        return result;
    }

    @Override
    public List<GeographicTaxonomy> getGeographicTaxonomyList(String institutionId) {
        log.trace("getGeographicTaxonomyList start");
        Assert.hasText(institutionId, INSTITUTION_ID_IS_REQUIRED);
        Institution institution = msCoreRestClient.getInstitution(institutionId);
        List<GeographicTaxonomy> result;
        if (institution.getGeographicTaxonomies() == null) {
            throw new ValidationException(String.format("The institution %s does not have geographic taxonomies.", institutionId));
        } else {
            result = institution.getGeographicTaxonomies();
        }
        log.debug("getGeographicTaxonomyList result = {}", result);
        log.trace("getGeographicTaxonomyList end");
        return result;
    }

    @Override
    public Collection<Institution> getInstitutionsByGeoTaxonomies(String geoTaxIds, SearchMode searchMode) {
        log.trace("getInstitutionByGeoTaxonomy start");
        Collection<Institution> institutions = msCoreRestClient.getInstitutionsByGeoTaxonomies(geoTaxIds, searchMode).getItems();
        if (institutions == null) {
            throw new ResourceNotFoundException(String.format("No institutions where found for given taxIds = %s", geoTaxIds));
        }
        log.debug("getInstitutionByGeoTaxonomy result = {}", institutions);
        log.trace("getInstitutionByGeoTaxonomy end");
        return institutions;
    }

    @Override
    public List<String> getInstitutionUserProductsV2(String institutionId, String userId) {
        log.trace("getInstitutionUserProducts start");
        Assert.hasText(institutionId, INSTITUTION_ID_IS_REQUIRED);
        Assert.hasText(userId, USER_ID_IS_REQUIRED);
        Set<String> products = new HashSet<>();
        ResponseEntity<List<UserDataResponse>> response = msUserApiRestClient._usersUserIdInstitutionInstitutionIdGet(institutionId, userId, userId, null, null, null, List.of(ACTIVE.name()));
        if (Objects.nonNull(response) && Objects.nonNull(response.getBody()) && !response.getBody().isEmpty()) {
            //There is only a document for the couple institutionId/userId
            products = response.getBody().get(0).getProducts().stream()
                    .map(OnboardedProductResponse::getProductId)
                    .collect(Collectors.toSet());
        }
        log.debug("getInstitutionUserProducts result = {}", products);
        log.trace("getInstitutionUserProducts start");
        return products.stream().toList();
    }

    @Override
    public List<OnboardedInstitutionInfo> getInstitutionDetails(String institutionId) {
        ResponseEntity<InstitutionResponse> responseEntity = institutionApiClient._retrieveInstitutionByIdUsingGET(institutionId);
        if (Objects.nonNull(responseEntity) && Objects.nonNull(responseEntity.getBody()) && Objects.nonNull(responseEntity.getBody().getOnboarding())) {
            return responseEntity.getBody().getOnboarding().stream().map(onboardedProductResponse -> {
                OnboardedInstitutionInfo onboardedInstitutionInfo = institutionMapper.toOnboardedInstitution(responseEntity.getBody());
                it.pagopa.selfcare.external_api.model.onboarding.ProductInfo productInfo = institutionMapper.toProductInfo(onboardedProductResponse);
                onboardedInstitutionInfo.setProductInfo(productInfo);
                onboardedInstitutionInfo.setState(productInfo.getStatus());
                return onboardedInstitutionInfo;
            }).toList();
        }
        return Collections.emptyList();
    }

    @Override
    public String createPgInstitution(String description, String taxId) {
        log.trace("createPgInstitution start");
        log.debug("createPgInstitution description = {},  taxId = {}", description, taxId);
        CreatePgInstitutionRequest createPgInstitutionRequest = new CreatePgInstitutionRequest(description, false, taxId);
        InstitutionResponse institutionResponse = Objects.requireNonNull(institutionApiClient.
                _createPgInstitutionUsingPOST(createPgInstitutionRequest)
                .getBody());

        log.trace("createPgInstitution end");

        return institutionResponse.getId();
    }
}