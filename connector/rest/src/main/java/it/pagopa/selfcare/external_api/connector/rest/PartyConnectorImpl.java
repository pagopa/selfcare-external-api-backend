package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.api.PartyConnector;
import it.pagopa.selfcare.external_api.connector.rest.client.PartyManagementRestClient;
import it.pagopa.selfcare.external_api.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.OnBoardingInfo;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.RelationshipInfo;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.RelationshipsResponse;
import it.pagopa.selfcare.external_api.connector.rest.model.onboarding.InstitutionSeed;
import it.pagopa.selfcare.external_api.connector.rest.model.onboarding.InstitutionUpdate;
import it.pagopa.selfcare.external_api.connector.rest.model.onboarding.OnboardingContract;
import it.pagopa.selfcare.external_api.connector.rest.model.onboarding.OnboardingImportInstitutionRequest;
import it.pagopa.selfcare.external_api.connector.rest.model.relationship.Relationship;
import it.pagopa.selfcare.external_api.connector.rest.model.relationship.Relationships;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.institutions.GeographicTaxonomy;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.external_api.model.institutions.SearchMode;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingData;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingImportData;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingResponseData;
import it.pagopa.selfcare.external_api.model.onboarding.User;
import it.pagopa.selfcare.external_api.model.product.PartyProduct;
import it.pagopa.selfcare.external_api.model.user.ProductInfo;
import it.pagopa.selfcare.external_api.model.user.RoleInfo;
import it.pagopa.selfcare.external_api.model.user.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class PartyConnectorImpl implements PartyConnector {

    protected static final String PRODUCT_ID_IS_REQUIRED = "A productId is required";
    protected static final String INSTITUTION_ID_IS_REQUIRED = "An institutionId is required ";
    protected static final String USER_ID_IS_REQUIRED = "A userId is required";
    protected static final String REQUIRED_INSTITUTION_ID_MESSAGE = "An Institution external id is required";

    private final PartyProcessRestClient partyProcessRestClient;
    private final PartyManagementRestClient partyManagementRestClient;

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
        return institutionInfo;
    };

    private static final Function<RelationshipInfo, PartyProduct> RELATIONSHIP_INFO_TO_PARTY_PRODUCT_FUNCTION = relationshipInfo -> {
        PartyProduct partyProduct = new PartyProduct();
        partyProduct.setId(relationshipInfo.getProduct().getId());
        partyProduct.setRole(relationshipInfo.getRole());
        return partyProduct;
    };


    @Autowired
    public PartyConnectorImpl(PartyProcessRestClient partyProcessRestClient, PartyManagementRestClient partyManagementRestClient) {
        this.partyProcessRestClient = partyProcessRestClient;
        this.partyManagementRestClient = partyManagementRestClient;
    }


    @Override
    public Collection<InstitutionInfo> getOnBoardedInstitutions(String productId) {
        log.trace("getOnBoardedInstitutions start");
        log.debug("getOnboardedInstitutions productId = {}", productId);
        Assert.hasText(productId, PRODUCT_ID_IS_REQUIRED);
        OnBoardingInfo onBoardingInfo = partyProcessRestClient.getOnBoardingInfo(null, EnumSet.of(ACTIVE));
        Collection<InstitutionInfo> result = parseOnBoardingInfo(onBoardingInfo, productId);
        log.debug("getOnBoardedInstitutions result = {}", result);
        log.trace("getOnBoardedInstitutions end");
        return result;
    }


    @Override
    public List<PartyProduct> getInstitutionUserProducts(String institutionId, String userId) {
        log.trace("getInstitutionUserProducts start");
        log.debug("getInstitutionUserProducts institutionId = {}, userId = {}", institutionId, userId);
        Assert.hasText(institutionId, INSTITUTION_ID_IS_REQUIRED);
        Assert.hasText(userId, USER_ID_IS_REQUIRED);
        List<PartyProduct> products = Collections.emptyList();
        RelationshipsResponse response = partyProcessRestClient.getUserInstitutionRelationships(institutionId, EnumSet.allOf(PartyRole.class), EnumSet.of(ACTIVE), null, null, userId);
        if (response != null) {
            products = response.stream()
                    .map(RELATIONSHIP_INFO_TO_PARTY_PRODUCT_FUNCTION)
                    .collect(Collectors.toList());
        }
        log.debug("getInstitutionUserProducts result = {}", products);
        log.trace("getInstitutionUserProducts start");
        return products;
    }


    private Collection<InstitutionInfo> parseOnBoardingInfo(OnBoardingInfo onBoardingInfo, String productId) {
        log.trace("parseOnBoardingInfo start");
        log.debug("parseOnBoardingInfo onBoardingInfo = {}", onBoardingInfo);
        Collection<InstitutionInfo> institutions = Collections.emptyList();
        if (onBoardingInfo != null && onBoardingInfo.getInstitutions() != null) {
            institutions = onBoardingInfo.getInstitutions().stream()
                    .filter(onboardingResponseData -> onboardingResponseData.getProductInfo().getId().equals(productId))
                    .map(ONBOARDING_DATA_TO_INSTITUTION_INFO_FUNCTION)
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(InstitutionInfo::getId, Function.identity(), MERGE_FUNCTION),
                            Map::values
                    ));
        }
        log.debug("parseOnBoardingInfo result = {}", institutions);
        log.trace("parseOnBoardingInfo end");
        return institutions;
    }


    @Override
    public Collection<UserInfo> getUsers(UserInfo.UserInfoFilter userInfoFilter) {
        log.trace("getUsers start");
        log.debug("getUsers userInfoFilter = {}", userInfoFilter);

        Collection<UserInfo> userInfos = Collections.emptyList();

        EnumSet<PartyRole> roles = null;
        if (userInfoFilter.getRole().isPresent()) {
            roles = Arrays.stream(PartyRole.values())
                    .filter(partyRole -> partyRole.getSelfCareAuthority().equals(userInfoFilter.getRole().get()))
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(PartyRole.class)));
        }

        Relationships relationships = partyManagementRestClient.getRelationships(
                userInfoFilter.getUserId().orElse(null),
                userInfoFilter.getInstitutionId().orElse(null),
                roles,
                userInfoFilter.getAllowedStates().orElse(null),
                userInfoFilter.getProductId().map(Set::of).orElse(null),
                userInfoFilter.getProductRoles().orElse(null)
        );

        if (relationships != null && relationships.getItems() != null) {
            userInfos = relationships.getItems().stream()
                    .collect(Collectors.toMap(Relationship::getFrom,
                            RELATIONSHIP_INFO_TO_USER_INFO_FUNCTION,
                            USER_INFO_MERGE_FUNCTION)).values();
        }
        log.debug("getUsers result = {}", userInfos);
        log.trace("getUsers end");
        return userInfos;
    }

    @Override
    public ResponseEntity<Void> verifyOnboarding(String externalInstitutionId, String productId) {
        log.trace("verifyOnboarding start");
        log.debug("verifyOnboarding externalInstitutionId = {}, productId = {}", externalInstitutionId, productId);
        Assert.hasText(externalInstitutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        Assert.hasText(productId, PRODUCT_ID_IS_REQUIRED);
        ResponseEntity<Void> responseEntity = partyProcessRestClient.verifyOnboarding(externalInstitutionId, productId);
        log.trace("verifyOnboarding end");
        return responseEntity;
    }

    @Override
    public Institution getInstitutionByExternalId(String externalInstitutionId) {
        log.trace("getInstitution start");
        log.debug("getInstitution externalInstitutionId = {}", externalInstitutionId);
        Assert.hasText(externalInstitutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        Institution result = partyProcessRestClient.getInstitutionByExternalId(externalInstitutionId);
        log.debug("getInstitution result = {}", result);
        log.trace("getInstitution end");
        return result;
    }

    @Override
    public Institution createInstitutionUsingExternalId(String institutionExternalId) {
        log.trace("createInstitutionUsingExternalId start");
        log.debug("createInstitutionUsingExternalId externalId = {}", institutionExternalId);
        Assert.hasText(institutionExternalId, REQUIRED_INSTITUTION_ID_MESSAGE);
        Institution result = partyProcessRestClient.createInstitutionUsingExternalId(institutionExternalId);
        log.debug("createInstitutionUsingExternalId result = {}", result);
        log.trace("createInstitutionUsingExternalId end");
        return result;
    }

    @Override
    public Institution createInstitutionRaw(OnboardingData onboardingData) {
        log.trace("createInstitutionUsingExternalId start");
        Assert.notNull(onboardingData, "An OnboardingData is required");
        Institution result = partyProcessRestClient.createInstitutionRaw(onboardingData.getInstitutionExternalId(), new InstitutionSeed(onboardingData));
        log.debug("createInstitutionUsingExternalId result = {}", result);
        log.trace("createInstitutionUsingExternalId end");
        return result;
    }

    @Override
    public void oldContractOnboardingOrganization(OnboardingImportData onboardingImportData) {
        Assert.notNull(onboardingImportData, "Onboarding data is required");
        OnboardingImportInstitutionRequest onboardingInstitutionRequest = new OnboardingImportInstitutionRequest();
        onboardingInstitutionRequest.setInstitutionExternalId(onboardingImportData.getInstitutionExternalId());
        onboardingInstitutionRequest.setPricingPlan(onboardingImportData.getPricingPlan());
        onboardingInstitutionRequest.setBilling(onboardingImportData.getBilling());
        onboardingInstitutionRequest.setProductId(onboardingImportData.getProductId());
        onboardingInstitutionRequest.setProductName(onboardingImportData.getProductName());
        onboardingInstitutionRequest.setContractImported(onboardingImportData.getContractImported());
        InstitutionUpdate institutionUpdate = new InstitutionUpdate();
        institutionUpdate.setInstitutionType(onboardingImportData.getInstitutionType());
        institutionUpdate.setAddress(onboardingImportData.getInstitutionUpdate().getAddress());
        institutionUpdate.setDescription(onboardingImportData.getInstitutionUpdate().getDescription());
        institutionUpdate.setDigitalAddress(onboardingImportData.getInstitutionUpdate().getDigitalAddress());
        institutionUpdate.setTaxCode(onboardingImportData.getInstitutionUpdate().getTaxCode());
        institutionUpdate.setZipCode(onboardingImportData.getInstitutionUpdate().getZipCode());
        institutionUpdate.setPaymentServiceProvider(onboardingImportData.getInstitutionUpdate().getPaymentServiceProvider());
        institutionUpdate.setDataProtectionOfficer(onboardingImportData.getInstitutionUpdate().getDataProtectionOfficer());
        if (onboardingImportData.getInstitutionUpdate().getGeographicTaxonomies() != null) {
            institutionUpdate.setGeographicTaxonomyCodes(onboardingImportData.getInstitutionUpdate().getGeographicTaxonomies().stream()
                    .map(GeographicTaxonomy::getCode).collect(Collectors.toList()));
        } else {
            institutionUpdate.setGeographicTaxonomyCodes(Collections.emptyList());
        }
        institutionUpdate.setRea(onboardingImportData.getInstitutionUpdate().getRea());
        institutionUpdate.setShareCapital(onboardingImportData.getInstitutionUpdate().getShareCapital());
        institutionUpdate.setBusinessRegisterPlace(onboardingImportData.getInstitutionUpdate().getBusinessRegisterPlace());
        institutionUpdate.setSupportEmail(onboardingImportData.getInstitutionUpdate().getSupportEmail());
        institutionUpdate.setSupportPhone(onboardingImportData.getInstitutionUpdate().getSupportPhone());
        institutionUpdate.setImported(onboardingImportData.getInstitutionUpdate().getImported());
        onboardingInstitutionRequest.setInstitutionUpdate(institutionUpdate);

        OnboardingContract onboardingContract = new OnboardingContract();
        onboardingContract.setPath(onboardingImportData.getContractPath());
        onboardingContract.setVersion(onboardingImportData.getContractVersion());
        onboardingInstitutionRequest.setContract(onboardingContract);

        onboardingInstitutionRequest.setUsers(onboardingImportData.getUsers().stream()
                .map(userInfo -> {
                    User user = new User();
                    user.setId(userInfo.getId());
                    user.setName(userInfo.getName());
                    user.setSurname(userInfo.getSurname());
                    user.setTaxCode(userInfo.getTaxCode());
                    user.setEmail(userInfo.getEmail());
                    user.setRole(userInfo.getRole());
                    user.setProductRole(userInfo.getProductRole());
                    return user;
                }).collect(Collectors.toList()));

        partyProcessRestClient.onboardingOrganization(onboardingInstitutionRequest);
    }

    @Override
    public List<GeographicTaxonomy> getGeographicTaxonomyList(String institutionId) {
        log.trace("getGeographicTaxonomyList start");
        log.debug("getGeographicTaxonomyList institutionId = {}", institutionId);
        Assert.hasText(institutionId, INSTITUTION_ID_IS_REQUIRED);
        Institution institution = partyProcessRestClient.getInstitution(institutionId);
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
        log.debug("getInstitutionByGeoTaxonomy geoTaxIds = {}, searchMode = {}", geoTaxIds, searchMode);
        Collection<Institution> institutions = partyManagementRestClient.getInstitutionsByGeoTaxonomies(geoTaxIds, searchMode).getItems();
        if (institutions == null) {
            throw new ResourceNotFoundException(String.format("No institutions where found for given taxIds = %s", geoTaxIds));
        }
        log.debug("getInstitutionByGeoTaxonomy result = {}", institutions);
        log.trace("getInstitutionByGeoTaxonomy end");
        return institutions;
    }

    @Override
    public void autoApprovalOnboarding(OnboardingData onboardingData) {
        Assert.notNull(onboardingData, "Onboarding data is required");
        OnboardingImportInstitutionRequest onboardingInstitutionRequest = new OnboardingImportInstitutionRequest();
        onboardingInstitutionRequest.setInstitutionExternalId(onboardingData.getInstitutionExternalId());
        onboardingInstitutionRequest.setPricingPlan(onboardingData.getPricingPlan());
        onboardingInstitutionRequest.setBilling(onboardingData.getBilling());
        onboardingInstitutionRequest.setProductId(onboardingData.getProductId());
        onboardingInstitutionRequest.setProductName(onboardingData.getProductName());
        InstitutionUpdate institutionUpdate = new InstitutionUpdate();
        institutionUpdate.setInstitutionType(onboardingData.getInstitutionType());
        institutionUpdate.setAddress(onboardingData.getInstitutionUpdate().getAddress());
        institutionUpdate.setDescription(onboardingData.getInstitutionUpdate().getDescription());
        institutionUpdate.setDigitalAddress(onboardingData.getInstitutionUpdate().getDigitalAddress());
        institutionUpdate.setTaxCode(onboardingData.getInstitutionUpdate().getTaxCode());
        institutionUpdate.setZipCode(onboardingData.getInstitutionUpdate().getZipCode());
        institutionUpdate.setPaymentServiceProvider(onboardingData.getInstitutionUpdate().getPaymentServiceProvider());
        institutionUpdate.setDataProtectionOfficer(onboardingData.getInstitutionUpdate().getDataProtectionOfficer());
        institutionUpdate.setGeographicTaxonomyCodes(onboardingData.getInstitutionUpdate().getGeographicTaxonomies().stream()
                .map(GeographicTaxonomy::getCode).collect(Collectors.toList()));
        institutionUpdate.setRea(onboardingData.getInstitutionUpdate().getRea());
        institutionUpdate.setShareCapital(onboardingData.getInstitutionUpdate().getShareCapital());
        institutionUpdate.setBusinessRegisterPlace(onboardingData.getInstitutionUpdate().getBusinessRegisterPlace());
        institutionUpdate.setSupportEmail(onboardingData.getInstitutionUpdate().getSupportEmail());
        institutionUpdate.setSupportPhone(onboardingData.getInstitutionUpdate().getSupportPhone());
        institutionUpdate.setImported(onboardingData.getInstitutionUpdate().getImported());
        onboardingInstitutionRequest.setInstitutionUpdate(institutionUpdate);

        onboardingInstitutionRequest.setUsers(onboardingData.getUsers().stream()
                .map(userInfo -> {
                    User user = new User();
                    user.setId(userInfo.getId());
                    user.setName(userInfo.getName());
                    user.setSurname(userInfo.getSurname());
                    user.setTaxCode(userInfo.getTaxCode());
                    user.setEmail(userInfo.getEmail());
                    user.setRole(userInfo.getRole());
                    user.setProductRole(userInfo.getProductRole());
                    return user;
                }).collect(Collectors.toList()));
        OnboardingContract onboardingContract = new OnboardingContract();
        onboardingContract.setPath(onboardingData.getContractPath());
        onboardingContract.setVersion(onboardingData.getContractVersion());
        onboardingInstitutionRequest.setContract(onboardingContract);

        partyProcessRestClient.onboardingOrganization(onboardingInstitutionRequest);
    }

    @Override
    public OnboardingResponseData getOnboardedInstitution(String institutionExternalId) {
        log.trace("getOnBoardedInstitution start");
        log.debug("getOnboardedInstitution institutionExternalId = {}", institutionExternalId);
        Assert.hasText(institutionExternalId, REQUIRED_INSTITUTION_ID_MESSAGE);
        OnBoardingInfo onBoardingInfo = partyProcessRestClient.getOnBoardingInfo(institutionExternalId, EnumSet.of(ACTIVE));
        OnboardingResponseData result = null;
        if (onBoardingInfo.getInstitutions().size() == 1) {
            result = onBoardingInfo.getInstitutions().get(0);
        } else if (onBoardingInfo.getInstitutions().size() > 1) {
            List<OnboardingResponseData> institutions = new ArrayList<>();
            onBoardingInfo.getInstitutions().forEach((institution) -> {
                        if (institution.getExternalId().equals(institutionExternalId)) {
                            institutions.add(institution);
                        }
                    }
            );
            if (institutions.size() != 0) {
                result = institutions.get(0);
            }
        }
        log.debug("getOnBoardedInstitution result = {}", result);
        log.trace("getOnBoardedInstitution end");
        return result;
    }

}
