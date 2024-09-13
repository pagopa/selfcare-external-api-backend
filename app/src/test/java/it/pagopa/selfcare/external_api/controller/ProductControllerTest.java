package it.pagopa.selfcare.external_api.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.external_api.mapper.ProductsMapper;
import it.pagopa.selfcare.external_api.mapper.ProductsMapperImpl;
import it.pagopa.selfcare.external_api.service.ProductService;
import it.pagopa.selfcare.product.entity.Product;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.file.Files;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest extends BaseControllerTest {
    private static final String BASE_URL = "/v1/products";

    @InjectMocks
    protected ProductController productController;

    @Mock
    private ProductService productService;

    @Spy
    private ProductsMapper productsMapper = new ProductsMapperImpl();

    @BeforeEach
    void setUp(){
        super.setUp(productController);
    }

    @Test
    void getProductFound() throws Exception {

        ClassPathResource inputResource = new ClassPathResource("expectations/Product.json");
        List<Product> product = objectMapper.readValue(Files.readAllBytes(inputResource.getFile().toPath()), new TypeReference<>() {});

        ClassPathResource outputResource = new ClassPathResource("expectations/ProductResource.json");
        String expectedResource = StringUtils.deleteWhitespace(new String(Files.readAllBytes(outputResource.getFile().toPath())));

        String productId = "productId";

        when(productService.getProduct(anyString())).thenReturn(product.get(0));

        mockMvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{productId}", productId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResource))
                .andReturn();
    }

    @Test
    void getProductNotFound() throws Exception {
        String productId = "productId";

        when(productService.getProduct(anyString())).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{productId}", productId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(""))
                .andReturn();
    }

}
