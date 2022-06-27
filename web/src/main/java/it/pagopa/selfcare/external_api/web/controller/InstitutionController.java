package it.pagopa.selfcare.external_api.web.controller;

import io.swagger.annotations.Api;
import it.pagopa.selfcare.external_api.core.InstitutionService;
import it.pagopa.selfcare.external_api.web.model.product.ProductsResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/institutions")
@Api(tags = "institutions")
public class InstitutionController {

    private final InstitutionService institutionService;


    @Autowired
    public InstitutionController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @GetMapping(value = "/{institutionId}/users/{userId}/products")
    @ResponseStatus(HttpStatus.OK)
    public List<ProductsResource> getInstitutionUserProducts() {
        return null;
    }
}
