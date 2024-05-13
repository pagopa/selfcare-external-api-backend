package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.InstitutionResponse;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.OnboardingResponse;
import it.pagopa.selfcare.external_api.api.MsCoreConnector;
import it.pagopa.selfcare.external_api.connector.rest.client.MsCoreInstitutionApiClient;
import it.pagopa.selfcare.external_api.connector.rest.client.MsCoreRestClient;
import it.pagopa.selfcare.external_api.connector.rest.client.MsUserApiRestClient;
import it.pagopa.selfcare.external_api.connector.rest.mapper.InstitutionMapper;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.RelationshipInfo;
import it.pagopa.selfcare.external_api.connector.rest.model.pnpg.CreatePnPgInstitutionRequest;
import it.pagopa.selfcare.external_api.connector.rest.model.pnpg.InstitutionPnPgResponse;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.institutions.*;
import it.pagopa.selfcare.external_api.model.onboarding.InstitutionLocation;
import it.pagopa.selfcare.external_api.model.onboarding.InstitutionOnboarding;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionInfo;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingResponseData;
import it.pagopa.selfcare.external_api.model.pnpg.CreatePnPgInstitution;
import it.pagopa.selfcare.external_api.model.product.PartyProduct;
import it.pagopa.selfcare.external_api.model.relationship.Relationship;
import it.pagopa.selfcare.external_api.model.user.ProductInfo;
import it.pagopa.selfcare.external_api.model.user.RoleInfo;
import it.pagopa.selfcare.external_api.model.user.UserInfo;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.OnboardedProductResponse;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.UserDataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.validation.ValidationException;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
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

    protected static final String PRODUCT_ID_IS_REQUIRED = "A productId is required";
    protected static final String INSTITUTION_ID_IS_REQUIRED = "An institutionId is required ";
    protected static final String USER_ID_IS_REQUIRED = "A userId is required";
    protected static final String REQUIRED_INSTITUTION_ID_MESSAGE = "An Institution external id is required";


    private static final BinaryOperator<InstitutionInfo> MERGE_FUNCTION =
            (inst1, inst2) -> {
                if (inst1.getUserRole().compareTo(inst2.getUserRole()) < 0) {
                    inst1.getProductRoles().addAll(inst2.getProductRoles());
                    return inst1;
                } else {
                    inst2.getProductRoles().addAll(inst1.getProductRoles());
                    return inst2;
                }
            };

    static final Function<Relationship, UserInfo> RELATIONSHIP_INFO_TO_USER_INFO_FUNCTION = relationshipInfo -> {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(relationshipInfo.getFrom());
        userInfo.setStatus(relationshipInfo.getState().toString());
        userInfo.setRole(relationshipInfo.getRole().getSelfCareAuthority());
        userInfo.setPartyRole(relationshipInfo.getRole());
        ProductInfo productInfo = new ProductInfo();
        productInfo.setId(relationshipInfo.getProduct().getId());
        RoleInfo roleInfo = new RoleInfo();
        roleInfo.setRelationshipId(relationshipInfo.getId());
        roleInfo.setSelcRole(relationshipInfo.getRole().getSelfCareAuthority());
        roleInfo.setRole(relationshipInfo.getProduct().getRole());
        roleInfo.setStatus(relationshipInfo.getState().toString());
        ArrayList<RoleInfo> roleInfos = new ArrayList<>();
        roleInfos.add(roleInfo);
        productInfo.setRoleInfos(roleInfos);
        Map<String, ProductInfo> products = new HashMap<>();
        products.put(productInfo.getId(), productInfo);
        userInfo.setProducts(products);
        userInfo.setInstitutionId(relationshipInfo.getTo());
        return userInfo;
    };

    private static final BinaryOperator<UserInfo> USER_INFO_MERGE_FUNCTION = (userInfo1, userInfo2) -> {
        String id = userInfo2.getProducts().keySet().toArray()[0].toString();

        if (userInfo1.getProducts().containsKey(id)) {
            userInfo1.getProducts().get(id).getRoleInfos().addAll(userInfo2.getProducts().get(id).getRoleInfos());
        } else {
            userInfo1.getProducts().put(id, userInfo2.getProducts().get(id));
        }
        if (userInfo1.getStatus().equals(userInfo2.getStatus())) {
            if (userInfo1.getRole().compareTo(userInfo2.getRole()) > 0) {
                userInfo1.setRole(userInfo2.getRole());
            }
        } else {
            if ("ACTIVE".equals(userInfo2.getStatus())) {
                userInfo1.setRole(userInfo2.getRole());
                userInfo1.setStatus(userInfo2.getStatus());
            }
        }
        return userInfo1;
    };

    private static final Function<OnboardingResponseData, InstitutionInfo> ONBOARDING_DATA_TO_INSTITUTION_INFO_FUNCTION = onboardingData -> {
        InstitutionInfo institutionInfo = new InstitutionInfo();
        institutionInfo.setId(onboardingData.getId());
        institutionInfo.setExternalId(onboardingData.getExternalId());
        institutionInfo.setDescription(onboardingData.getDescription());
        institutionInfo.setStatus(onboardingData.getState().toString());
        institutionInfo.setTaxCode(onboardingData.getTaxCode());
        institutionInfo.setAddress(onboardingData.getAddress());
        institutionInfo.setDigitalAddress(onboardingData.getDigitalAddress());
        institutionInfo.setZipCode(onboardingData.getZipCode());
        institutionInfo.setBilling(onboardingData.getBilling());
        Set<String> productRole = new HashSet<>();
        productRole.add(onboardingData.getProductInfo().getRole());
        institutionInfo.setProductRoles(productRole);
        institutionInfo.setOrigin(onboardingData.getOrigin());
        institutionInfo.setOriginId(onboardingData.getOriginId());
        institutionInfo.setInstitutionType(onboardingData.getInstitutionType());
        institutionInfo.setUserRole(onboardingData.getRole());
        institutionInfo.setBusinessData(onboardingData.getBusinessData());
        institutionInfo.setSupportContact(onboardingData.getSupportContact());
        institutionInfo.setPaymentServiceProvider(onboardingData.getPaymentServiceProvider());
        institutionInfo.setDataProtectionOfficer(onboardingData.getDataProtectionOfficer());
        RootParent rootParent = new RootParent();
        rootParent.setId(onboardingData.getRootParentId());
        rootParent.setDescription(onboardingData.getParentDescription());
        institutionInfo.setRootParent(rootParent);
        institutionInfo.setSubunitCode(onboardingData.getSubunitCode());
        institutionInfo.setSubunitType(onboardingData.getSubunitType());
        institutionInfo.setAooParentCode(onboardingData.getAooParentCode());
        InstitutionLocation institutionLocation = new InstitutionLocation();
        if (onboardingData.getInstitutionLocation() != null) {
            institutionLocation.setCity(onboardingData.getInstitutionLocation().getCity());
            institutionLocation.setCountry(onboardingData.getInstitutionLocation().getCountry());
            institutionLocation.setCounty(onboardingData.getInstitutionLocation().getCounty());
        }
        institutionInfo.setInstitutionLocation(institutionLocation);

        return institutionInfo;
    };


    private static final Function<RelationshipInfo, PartyProduct> RELATIONSHIP_INFO_TO_PARTY_PRODUCT_FUNCTION = relationshipInfo -> {
        PartyProduct partyProduct = new PartyProduct();
        partyProduct.setId(relationshipInfo.getProduct().getId());
        partyProduct.setRole(relationshipInfo.getRole());
        return partyProduct;
    };

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
        if (responseEntity != null && responseEntity.getBody() != null && responseEntity.getBody().getOnboarding() != null) {
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
}