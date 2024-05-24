package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.api.ProductsConnector;
import it.pagopa.selfcare.product.entity.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;

@ExtendWith({MockitoExtension.class})
class ProductServiceImplTest extends BaseServiceTestUtils {

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductsConnector productsConnectorMock;

    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    void getProduct() throws IOException {
        String productId = "id";
        ClassPathResource inputResource = new ClassPathResource("expectations/ProductV2.json");
        byte[] productStream = Files.readAllBytes(inputResource.getFile().toPath());
        Product product = objectMapper.readValue(productStream, Product.class);
        Mockito.when(productsConnectorMock.getProduct(productId)).thenReturn(product);
        Assertions.assertEquals(product, productService.getProduct(productId));
    }
}
