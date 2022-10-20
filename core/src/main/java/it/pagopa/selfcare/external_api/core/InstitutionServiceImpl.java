package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.external_api.api.PartyConnector;
import it.pagopa.selfcare.external_api.api.ProductsConnector;
import it.pagopa.selfcare.external_api.api.UserRegistryConnector;
import it.pagopa.selfcare.external_api.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.external_api.model.product.PartyProduct;
import it.pagopa.selfcare.external_api.model.product.Product;
import it.pagopa.selfcare.external_api.model.user.RelationshipState;
import it.pagopa.selfcare.external_api.model.user.User;
import it.pagopa.selfcare.external_api.model.user.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.external_api.model.user.User.Fields.*;

@Service
@Slf4j
class InstitutionServiceImpl implements InstitutionService {

    private static final EnumSet<User.Fields> USER_FIELD_LIST = EnumSet.of(name, familyName, workContacts);

    private final PartyConnector partyConnector;
    private final ProductsConnector productsConnector;
    private final UserRegistryConnector userRegistryConnector;

    @Autowired
    InstitutionServiceImpl(PartyConnector partyConnector,
                           ProductsConnector productsConnector,
                           UserRegistryConnector userRegistryConnector) {
        this.partyConnector = partyConnector;
        this.productsConnector = productsConnector;
        this.userRegistryConnector = userRegistryConnector;
    }

    @Override
    public Collection<InstitutionInfo> getInstitutions(String productId) {
        log.trace("getInstitutions start");
        log.debug("getInstitutions productId = {}", productId);
        Collection<InstitutionInfo> result = partyConnector.getOnBoardedInstitutions(productId);
        log.debug("getInstitutions result = {}", result);
        log.trace("getInstitutions end");
        return result;
    }

    @Override
    public List<Product> getInstitutionUserProducts(String institutionId) {
        log.trace("getInstitutionUserProducts start");
        log.debug("getInstitutionUserProducts institutionId = {}", institutionId);
        Assert.hasText(institutionId, "An institutionId is required");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Assert.state(authentication != null, "Authentication is required");
        Assert.state(authentication.getPrincipal() instanceof SelfCareUser, "Not SelfCareUser principal");
        SelfCareUser user = (SelfCareUser) authentication.getPrincipal();
        List<Product> products = productsConnector.getProducts();
        if (!products.isEmpty()) {
            Map<String, PartyProduct> institutionUserProducts = partyConnector.getInstitutionUserProducts(institutionId, user.getId()).stream()
                    .collect(Collectors.toMap(PartyProduct::getId, Function.identity(), (partyProduct, partyProduct2) -> partyProduct));
            products = products.stream()
                    .filter(product -> institutionUserProducts.containsKey(product.getId()))
                    .collect(Collectors.toList());
        }
        log.debug("getInstitutionUserProducts result = {}", products);
        log.trace("getInstitutionUserProducts end");
        return products;
    }


    @Override
    public Collection<UserInfo> getInstitutionProductUsers(String institutionId, String productId, Optional<String> userId, Optional<Set<String>> productRoles) {
        log.trace("getInstitutionProductUsers start");
        log.debug("getInstitutionProductUsers institutionId = {}, productId = {}, productRoles = {}", institutionId, productId, productRoles);
        Assert.hasText(institutionId, "An Institution id is required");
        Assert.hasText(productId, "A Product id is required");
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setInstitutionId(Optional.of(institutionId));
        userInfoFilter.setProductId(Optional.of(productId));
        userInfoFilter.setUserId(userId);
        userInfoFilter.setProductRoles(productRoles);
        userInfoFilter.setAllowedState(Optional.of(EnumSet.of(RelationshipState.ACTIVE)));
        Collection<UserInfo> result = partyConnector.getUsers(userInfoFilter);
        result.forEach(userInfo ->
                userInfo.setUser(userRegistryConnector.getUserByInternalId(userInfo.getId(), USER_FIELD_LIST)));
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutionProductUsers result = {}", result);
        log.trace("getInstitutionProductUsers end");
        return result;
    }

}
