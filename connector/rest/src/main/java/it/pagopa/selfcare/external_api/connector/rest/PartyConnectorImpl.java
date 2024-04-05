package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.api.PartyConnector;
import it.pagopa.selfcare.external_api.connector.rest.client.PartyManagementRestClient;
import it.pagopa.selfcare.external_api.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.external_api.connector.rest.client.UserApiRestClient;
import it.pagopa.selfcare.external_api.connector.rest.mapper.InstitutionMapper;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.*;
import it.pagopa.selfcare.external_api.connector.rest.model.onboarding.InstitutionUpdate;
import it.pagopa.selfcare.external_api.connector.rest.model.onboarding.*;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.institutions.*;
import it.pagopa.selfcare.external_api.model.onboarding.*;
import it.pagopa.selfcare.external_api.model.product.PartyProduct;
import it.pagopa.selfcare.external_api.model.relationship.Relationship;
import it.pagopa.selfcare.external_api.model.relationship.Relationships;
import it.pagopa.selfcare.external_api.model.user.ProductInfo;
import it.pagopa.selfcare.external_api.model.user.RoleInfo;
import it.pagopa.selfcare.external_api.model.user.UserInfo;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.OnboardedProductResponse;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.UserDataResponse;
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

    protected static final String ONBOARDING_DATA_IS_REQUIRED = "An OnboardingData is required";
    protected static final String REQUIRED_INSTITUTION_ID_MESSAGE = "An Institution external id is required";
    protected static final String REQUIRED_INSTITUTION_TAX_CODE_MESSAGE = "An Institution tax code is required";

    private final PartyProcessRestClient partyProcessRestClient;
    private final PartyManagementRestClient partyManagementRestClient;
    private final UserApiRestClient userApiRestClient;
    private final InstitutionMapper institutionMapper;

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
        if(onboardingData.getInstitutionLocation() != null) {
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

    @Autowired
    public PartyConnectorImpl(PartyProcessRestClient partyProcessRestClient,
                              PartyManagementRestClient partyManagementRestClient,
                              InstitutionMapper institutionMapper,
                              UserApiRestClient userApiRestClient) {
        this.partyProcessRestClient = partyProcessRestClient;
        this.partyManagementRestClient = partyManagementRestClient;
        this.institutionMapper = institutionMapper;
        this.userApiRestClient = userApiRestClient;
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

    @Override
    public List<String> getInstitutionUserProductsV2(String institutionId, String userId) {
        log.trace("getInstitutionUserProducts start");
        Assert.hasText(institutionId, INSTITUTION_ID_IS_REQUIRED);
        Assert.hasText(userId, USER_ID_IS_REQUIRED);
        Set<String> products = new HashSet<>();
        ResponseEntity<List<UserDataResponse>> response = userApiRestClient._usersUserIdInstitutionInstitutionIdGet(institutionId, userId, null, null, null, null, List.of(ACTIVE.name()));
        if (Objects.nonNull(response) && Objects.nonNull(response.getBody()) && Objects.nonNull(response.getBody().get(0))) {
            //There is only a document for the couple institutionId/userId
            products = response.getBody().get(0).getProducts().stream()
                    .map(OnboardedProductResponse::getProductId)
                    .collect(Collectors.toSet());
        }
        log.debug("getInstitutionUserProducts result = {}", products);
        log.trace("getInstitutionUserProducts start");
        return products.stream().toList();
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
