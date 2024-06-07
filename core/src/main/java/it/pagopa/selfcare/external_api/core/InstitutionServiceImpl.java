package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.external_api.api.*;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.institutions.GeographicTaxonomy;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.institutions.SearchMode;
import it.pagopa.selfcare.external_api.model.nationalRegistries.LegalVerification;
import it.pagopa.selfcare.external_api.model.pnpg.CreatePnPgInstitution;
import it.pagopa.selfcare.external_api.model.user.*;
import it.pagopa.selfcare.product.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.external_api.model.user.User.Fields.*;

@Service
@Slf4j
class InstitutionServiceImpl implements InstitutionService {

    private static final EnumSet<User.Fields> USER_FIELD_LIST = EnumSet.of(name, familyName, workContacts);
    private static final EnumSet<User.Fields> USER_FIELD_LIST_FISCAL_CODE = EnumSet.of(name, familyName, workContacts, fiscalCode);
    static final String REQUIRED_INSTITUTION_MESSAGE = "An Institution id is required";
    private final ProductsConnector productsConnector;
    private final MsCoreConnector msCoreConnector;
    private final UserRegistryConnector userRegistryConnector;
    private final UserMsConnector userMsConnector;
    private static final String TAG_LOG_INSTITUTION_USER_PRODUCTS = "getInstitutionUserProducts";
    private final Set<String> serviceType;

    private final MsPartyRegistryProxyConnector msPartyRegistryProxyConnector;

    @Autowired
    InstitutionServiceImpl(ProductsConnector productsConnector,
                           MsCoreConnector msCoreConnector, UserRegistryConnector userRegistryConnector,
                           UserMsConnector userMsConnector, @Value("${external_api.allowed-service-types}")String[] serviceType, MsPartyRegistryProxyConnector msPartyRegistryProxyConnector) {
        this.productsConnector = productsConnector;
        this.msCoreConnector = msCoreConnector;
        this.userRegistryConnector = userRegistryConnector;
        this.userMsConnector = userMsConnector;
        this.serviceType = Set.of(serviceType);
        this.msPartyRegistryProxyConnector = msPartyRegistryProxyConnector;
    }


    @Override
    public List<Product> getInstitutionUserProductsV2(String institutionId, String userId) {
        log.trace(TAG_LOG_INSTITUTION_USER_PRODUCTS + " start");
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);

        List<Product> products = productsConnector.getProducts();
        if (!products.isEmpty()) {
            List<String> productIds = msCoreConnector.getInstitutionUserProductsV2(institutionId, userId);
            products = products.stream()
                    .filter(product -> productIds.contains(product.getId()))
                    .toList();
        }
        log.trace(TAG_LOG_INSTITUTION_USER_PRODUCTS + " result = {}", products);
        log.trace(TAG_LOG_INSTITUTION_USER_PRODUCTS + " end");
        return products;
    }


    @Override
    public Collection<UserInfo> getInstitutionProductUsersV2(String institutionId, String productId, String userId, Optional<Set<String>> productRoles, String xSelfCareUid) {
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        Assert.hasText(productId, "A Product id is required");

        List<UserInstitution> usersInstitutions = userMsConnector.getUsersInstitutions(userId, institutionId, null, null, null, null, null, null);
        if(Objects.isNull(usersInstitutions) || usersInstitutions.isEmpty()) return List.of();

        Collection<UserInfo> userInfos = usersInstitutions.stream()
                .peek(userInstitution -> userInstitution.setProducts(userInstitution.getProducts().stream()
                        .filter(item -> Objects.isNull(productId) || productId.equals(item.getProductId()))
                        .filter(item -> productRoles.isEmpty() || productRoles.get().stream().anyMatch(role -> role.equals(item.getProductRole())))
                        .filter(item -> RelationshipState.ACTIVE.name().equals(item.getStatus()))
                        .toList()))
                .flatMap(userInstitution -> userInstitution.getProducts().stream()
                        .map(onboardedProduct -> toUserInfos(userInstitution, onboardedProduct)))
                .collect(Collectors.toMap(UserInfo::getId,
                        Function.identity(),
                        USER_INFO_MERGE_FUNCTION))
                .values();

        if(xSelfCareUid != null && serviceType.contains(xSelfCareUid)) {
            userInfos.forEach(userInfo ->
                    userInfo.setUser(userRegistryConnector.getUserByInternalId(userInfo.getId(), USER_FIELD_LIST_FISCAL_CODE)));
        } else {
            userInfos.forEach(userInfo ->
                    userInfo.setUser(userRegistryConnector.getUserByInternalId(userInfo.getId(), USER_FIELD_LIST)));
        }
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutionProductUsers result = {}", userInfos);
        log.trace("getInstitutionProductUsers end");
        return userInfos;
    }

    private UserInfo toUserInfos(UserInstitution userInstitution, OnboardedProductResponse onboardedProduct) {
        SelfCareAuthority selfCareAuthority = Arrays.stream(PartyRole.values())
                    .filter(partyRole -> partyRole.name().equals(onboardedProduct.getRole()))
                    .findAny()
                    .map(PartyRole::getSelfCareAuthority)
                    .orElseThrow(() -> new RuntimeException(String.format("Role for user %s and institution %s not found!", userInstitution.getUserId(), userInstitution.getInstitutionId())));

        UserInfo userInfo = new UserInfo();
        userInfo.setId(userInstitution.getUserId());
        userInfo.setStatus(onboardedProduct.getStatus());
        userInfo.setRole(selfCareAuthority);
        userInfo.setPartyRole(PartyRole.valueOf(onboardedProduct.getRole()));
        ProductInfo productInfo = new ProductInfo();
        productInfo.setId(onboardedProduct.getProductId());
        RoleInfo roleInfo = new RoleInfo();
        roleInfo.setRelationshipId(userInstitution.getUserId());
        roleInfo.setSelcRole(selfCareAuthority);
        roleInfo.setRole(onboardedProduct.getProductRole());
        roleInfo.setStatus(onboardedProduct.getStatus());
        ArrayList<RoleInfo> roleInfos = new ArrayList<>();
        roleInfos.add(roleInfo);
        productInfo.setRoleInfos(roleInfos);
        Map<String, ProductInfo> products = new HashMap<>();
        products.put(productInfo.getId(), productInfo);
        userInfo.setProducts(products);
        userInfo.setInstitutionId(userInstitution.getInstitutionId());
        userInfo.setUserUuidMail(userInstitution.getUserMailUuid());
        return userInfo;
    }

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

    @Override
    public List<GeographicTaxonomy> getGeographicTaxonomyList(String institutionId) {
        log.trace("getGeographicTaxonomyList start");
        log.debug("getGeographicTaxonomyList externalInstitutionId = {}", institutionId);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        List<GeographicTaxonomy> result = msCoreConnector.getGeographicTaxonomyList(institutionId);
        log.debug("getGeographicTaxonomyList result = {}", result);
        log.trace("getGeographicTaxonomyList end");
        return result;
    }

    @Override
    public Collection<Institution> getInstitutionsByGeoTaxonomies(Set<String> geoTaxIds, SearchMode searchMode) {
        log.trace("getInstitutionByGeoTaxonomy start");
        log.debug("getInstitutionByGeoTaxonomy geoTaxIds = {}, searchMode = {}", geoTaxIds, searchMode);
        Assert.notEmpty(geoTaxIds, "GeoTaxonomy ids are required in order to retrieve the institutions");
        String geoIds = String.join(",", geoTaxIds);
        Collection<Institution> institutions = msCoreConnector.getInstitutionsByGeoTaxonomies(geoIds, searchMode);
        log.debug("getInstitutionByGeoTaxonomy result = {}", "null)");
        log.trace("getInstitutionByGeoTaxonomy end");
        return institutions;
    }

    @Override
    public String addInstitution(CreatePnPgInstitution request) {
        log.trace("addInstitution start");
        log.debug("addInstitution request = {}", request);
        String institutionInternalId;
        try {
            institutionInternalId = msCoreConnector.getInstitutionByExternalId(request.getExternalId()).getId();
        } catch (ResourceNotFoundException e) {
            institutionInternalId = msCoreConnector.createPnPgInstitution(request);
        }
        log.debug("addInstitution result = {}", institutionInternalId);
        log.trace("addInstitution end");
        return institutionInternalId;
    }

    @Override
    public LegalVerification verifyLegal(String taxId, String vatNumber) {
        log.trace("verifyLegal start");
        log.debug("verifyLegal taxId = {}, vatNumber = {}", Encode.forJava(taxId), Encode.forJava(vatNumber));
        LegalVerification legalVerification = msPartyRegistryProxyConnector.verifyLegal(taxId, vatNumber);
        log.trace("verifyLegal end");
        return legalVerification;
    }

}
