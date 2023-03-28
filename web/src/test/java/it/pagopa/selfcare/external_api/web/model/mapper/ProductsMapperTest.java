package it.pagopa.selfcare.external_api.web.model.mapper;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.external_api.model.product.Product;
import it.pagopa.selfcare.external_api.web.model.products.ProductResource;
import it.pagopa.selfcare.external_api.web.model.products.ProductRoleInfo;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.commons.utils.TestUtils.reflectionEqualsByName;
import static org.junit.jupiter.api.Assertions.*;

class ProductsMapperTest {
    
    @Test
    void toResource_notNull() {
        // given
        Product product = TestUtils.mockInstance(new Product());
        // when
        ProductResource resource = ProductsMapper.toResource(product);
        // then
        assertEquals(product.getId(), resource.getId());
        assertEquals(product.getTitle(), resource.getTitle());
        assertEquals(product.getContractTemplatePath(), resource.getContractTemplatePath());
        assertEquals(product.getContractTemplateVersion(), resource.getContractTemplateVersion());
        assertEquals(product.getContractTemplateUpdatedAt(), resource.getContractTemplateUpdatedAt());
        assertEquals(product.getCreatedAt(), resource.getCreatedAt());
        assertEquals(product.getDescription(), resource.getDescription());
        assertEquals(product.getLogo(), resource.getLogo());
        assertEquals(product.getLogoBgColor(), resource.getLogoBgColor());
        assertEquals(product.getUrlBO(), resource.getUrlBO());
        assertEquals(product.getUrlPublic(), resource.getUrlPublic());
        assertEquals(product.getDepictImageUrl(), resource.getDepictImageUrl());
        assertEquals(product.getIdentityTokenAudience(), resource.getIdentityTokenAudience());
        assertEquals(product.getParentId(), resource.getParentId());
        assertEquals(product.getRoleManagementURL(), resource.getRoleManagementURL());
        assertEquals(product.getRoleMappings(), resource.getRoleMappings());
        TestUtils.reflectionEqualsByName(product, resource);
    }

    @Test
    void toResource_null() {
        // given
        Product model = null;
        // when
        ProductResource resource = ProductsMapper.toResource(model);
        // then
        assertNull(resource);
    }

    @Test
    void toRoleMappings_null() {
        // given
        EnumMap<PartyRole, it.pagopa.selfcare.external_api.model.product.ProductRoleInfo> roleMappings = null;
        // when
        EnumMap<PartyRole, ProductRoleInfo> result = ProductsMapper.toRoleMappings(roleMappings);
        // then
        assertNull(result);
    }

    @Test
    void toRoleMappings_notNull() {
        // given
        EnumMap<PartyRole, it.pagopa.selfcare.external_api.model.product.ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class);
        for (PartyRole partyRole : PartyRole.values()) {
            it.pagopa.selfcare.external_api.model.product.ProductRoleInfo productRoleInfo = new it.pagopa.selfcare.external_api.model.product.ProductRoleInfo();
            List<it.pagopa.selfcare.external_api.model.product.ProductRoleInfo.ProductRole> roles = new ArrayList<>();
            roles.add(mockInstance(new it.pagopa.selfcare.external_api.model.product.ProductRoleInfo.ProductRole(), partyRole.ordinal() + 1));
            roles.add(mockInstance(new it.pagopa.selfcare.external_api.model.product.ProductRoleInfo.ProductRole(), partyRole.ordinal() + 2));
            productRoleInfo.setRoles(roles);
            productRoleInfo.setMultiroleAllowed(true);
            roleMappings.put(partyRole, productRoleInfo);
        }
        // when
        EnumMap<PartyRole, ProductRoleInfo> result = ProductsMapper.toRoleMappings(roleMappings);
        // then
        assertNotNull(result);
        reflectionEqualsByName(roleMappings.get(PartyRole.MANAGER).getRoles().get(0), result.get(PartyRole.MANAGER).getRoles().get(0));
        reflectionEqualsByName(roleMappings.get(PartyRole.MANAGER).getRoles().get(1), result.get(PartyRole.MANAGER).getRoles().get(1));
        reflectionEqualsByName(roleMappings.get(PartyRole.DELEGATE).getRoles().get(0), result.get(PartyRole.DELEGATE).getRoles().get(0));
        reflectionEqualsByName(roleMappings.get(PartyRole.DELEGATE).getRoles().get(1), result.get(PartyRole.DELEGATE).getRoles().get(1));
        reflectionEqualsByName(roleMappings.get(PartyRole.SUB_DELEGATE).getRoles().get(0), result.get(PartyRole.SUB_DELEGATE).getRoles().get(0));
        reflectionEqualsByName(roleMappings.get(PartyRole.SUB_DELEGATE).getRoles().get(1), result.get(PartyRole.SUB_DELEGATE).getRoles().get(1));
        reflectionEqualsByName(roleMappings.get(PartyRole.OPERATOR).getRoles().get(0), result.get(PartyRole.OPERATOR).getRoles().get(0));
        reflectionEqualsByName(roleMappings.get(PartyRole.OPERATOR).getRoles().get(1), result.get(PartyRole.OPERATOR).getRoles().get(1));
        assertEquals(true, result.get(PartyRole.MANAGER).getMultiroleAllowed());
        assertEquals(true, result.get(PartyRole.DELEGATE).getMultiroleAllowed());
        assertEquals(true, result.get(PartyRole.SUB_DELEGATE).getMultiroleAllowed());
        assertEquals(true, result.get(PartyRole.OPERATOR).getMultiroleAllowed());
    }

}
