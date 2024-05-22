package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.api.ProductsConnector;
import it.pagopa.selfcare.product.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.TestSecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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
        Product expectedProduct = new Product();
        expectedProduct.setId(productId);
        when(productsConnectorMock.getProduct(anyString()))
                .thenReturn(expectedProduct);
        // when
        Product product = productService.getProduct(productId);
        // then
        assertNotNull(product);
        assertEquals(expectedProduct.getId(), product.getId());
    }
}
