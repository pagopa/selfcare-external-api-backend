package it.pagopa.selfcare.external_api.core;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.external_api.api.ProductsConnector;
import it.pagopa.selfcare.external_api.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.external_api.model.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collection;
import java.util.List;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductsConnector productsConnectorMock;

    @BeforeEach
    void beforeEach() {
        TestSecurityContextHolder.clearContext();
    }


    @Test
    void getProduct() {
        //given
        String productId = "productIds";
        Product expectedProduct = mockInstance(new Product());
        when(productsConnectorMock.getProduct(anyString()))
                .thenReturn(expectedProduct);
        // when
        Product product = productService.getProduct(productId);
        // then
        assertNotNull(product);
        assertEquals(expectedProduct.getId(), product.getId());
        assertEquals(expectedProduct.getTitle(), product.getTitle());
        assertEquals(expectedProduct.getDescription(), product.getDescription());
        assertEquals(expectedProduct.getUrlBO(), product.getUrlBO());
        assertEquals(expectedProduct.getUrlPublic(), product.getUrlPublic());
    }
}
