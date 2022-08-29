package it.pagopa.selfcare.external_api.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.external_api.core.ProductService;
import it.pagopa.selfcare.external_api.web.model.mapper.ProductsMapper;
import it.pagopa.selfcare.external_api.web.model.products.ProductResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(value = "/products", produces = APPLICATION_JSON_VALUE)
@Api(tags = "product")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping(value = "/{productId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.external_api.products.api.getProduct}")
    public ProductResource getProduct(@ApiParam("${swagger.external_api.products.model.id}") @PathVariable(value = "productId") String productId) {
        log.trace("getProduct start");
        log.debug("getProduct productId = {}", productId);
        ProductResource productResources = ProductsMapper.toResource(productService.getProduct(productId));
        log.debug("getProduct result = {}", productResources);
        log.trace("getProduct end");
        return productResources;
    }
}
