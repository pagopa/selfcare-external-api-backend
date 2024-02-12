package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.api.MsCoreConnector;
import it.pagopa.selfcare.external_api.api.OnboardingMsConnector;
import it.pagopa.selfcare.external_api.api.ProductsConnector;
import it.pagopa.selfcare.external_api.model.product.Product;
import it.pagopa.selfcare.external_api.model.token.ProductToken;
import it.pagopa.selfcare.external_api.model.token.TokenOnboardedUsers;
import it.pagopa.selfcare.external_api.model.user.InstitutionProducts;
import it.pagopa.selfcare.external_api.model.user.UserProducts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.TestSecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TokenServiceImplTest {

    @InjectMocks
    private TokenServiceImpl tokenService;

    @Mock
    private OnboardingMsConnector onboardingMsConnector;

    @Mock
    private MsCoreConnector msCoreConnector;

    @BeforeEach
    void beforeEach() {
        TestSecurityContextHolder.clearContext();
    }


    @Test
    void findByProductId() {
        //given
        String productId = "productId";
        TokenOnboardedUsers tokenOnboardedUsers = new TokenOnboardedUsers();
        tokenOnboardedUsers.setProductId(productId);
        when(onboardingMsConnector.getOnboardings(productId, 1, 10))
                .thenReturn(List.of(tokenOnboardedUsers));
        UserProducts userProducts = new UserProducts();
        userProducts.setId("userId");
        InstitutionProducts bindings = new InstitutionProducts();
        bindings.setInstitutionId("institutionId");
        ProductToken productToken = new ProductToken();
        productToken.setProductId(productId);
        bindings.setProducts(List.of(productToken));
        userProducts.setBindings(List.of(bindings));
        when(msCoreConnector.getOnboarderUsers(any())).thenReturn(List.of(userProducts));
        // when
        List<TokenOnboardedUsers> tokens = tokenService.findByProductId(productId, 1, 10);
        // then
        assertNotNull(tokens);

    }
}
