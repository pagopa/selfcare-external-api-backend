package it.pagopa.selfcare.external_api.web.model.mapper;

import it.pagopa.selfcare.external_api.model.onboarding.User;
import it.pagopa.selfcare.external_api.model.user.ProductInfo;
import it.pagopa.selfcare.external_api.model.user.RoleInfo;
import it.pagopa.selfcare.external_api.model.user.UserInfo;
import it.pagopa.selfcare.external_api.model.user.WorkContact;
import it.pagopa.selfcare.external_api.web.model.user.UserDto;
import it.pagopa.selfcare.external_api.web.model.user.UserResource;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.commons.utils.TestUtils.reflectionEqualsByName;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void toUserResource_null() {
        // given
        UserInfo model = null;
        final String productId = null;
        // when
        UserResource resource = UserMapper.toUserResource(model, productId);
        // then
        assertNull(resource);
    }


    @Test
    void toUserResource_notNull() {
        // given
        UserInfo model = mockInstance(new UserInfo());
        model.setId(randomUUID().toString());
        ProductInfo productMock = mockInstance(new ProductInfo());
        productMock.setRoleInfos(List.of(mockInstance(new RoleInfo())));
        Map<String, ProductInfo> product = new HashMap<>();
        product.put(productMock.getId(), productMock);
        model.setProducts(product);
        model.getUser().setWorkContacts(Map.of(model.getInstitutionId(), mockInstance(new WorkContact())));
        String id = model.getProducts().keySet().toArray()[0].toString();
        ProductInfo productInfo = model.getProducts().get(id);
        // when
        UserResource resource = UserMapper.toUserResource(model, productMock.getId());
        // then
        assertEquals(model.getId(), resource.getId().toString());
        assertEquals(model.getUser().getName().getValue(), resource.getName());
        assertEquals(model.getUser().getFamilyName().getValue(), resource.getSurname());
        assertEquals(model.getUser().getWorkContacts().get(model.getInstitutionId()).getEmail().getValue(), resource.getEmail());
        assertIterableEquals(productInfo.getRoleInfos().stream().map(RoleInfo::getRole).collect(Collectors.toList()), resource.getRoles());
    }

    @Test
    void toUser() {
        // given
        UserDto model = mockInstance(new UserDto());
        // when
        User resource = UserMapper.toUser(model);
        // then
        assertNotNull(resource);
        reflectionEqualsByName(model, resource);
    }

    @Test
    void toUser_null() {
        // given
        UserDto model = null;
        // when
        User resource = UserMapper.toUser(model);
        // then
        assertNull(resource);
    }

}
