package it.pagopa.selfcare.external_api.service;

import it.pagopa.selfcare.product.entity.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;

import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class ProductServiceImplTest extends BaseServiceTestUtils {

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductService productServiceClient;


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
        when(productServiceClient.getProduct(productId)).thenReturn(product);
        Assertions.assertEquals(product, productService.getProduct(productId));
    }
}
