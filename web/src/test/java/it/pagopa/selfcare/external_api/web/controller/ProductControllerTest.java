package it.pagopa.selfcare.external_api.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.external_api.core.ProductService;
import it.pagopa.selfcare.external_api.model.product.Product;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {
    private static final String BASE_URL = "/v1/products";

    @InjectMocks
    protected ProductController productController;

    @Mock
    private ProductService productService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .build();
    }


    @Test
    void getProductFound() throws Exception {

        ClassPathResource inputResource = new ClassPathResource("expectations/Product.json");
        Product product = objectMapper.readValue(Files.readAllBytes(inputResource.getFile().toPath()), Product.class);

        ClassPathResource outputResource = new ClassPathResource("expectations/ProductResource.json");
        String expectedResource = StringUtils.deleteWhitespace(new String(Files.readAllBytes(outputResource.getFile().toPath())));

        String productId = "productId";

        when(productService.getProduct(anyString())).thenReturn(product);

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
                .andExpect(content().string(nullValue()))
                .andReturn();
    }

}
