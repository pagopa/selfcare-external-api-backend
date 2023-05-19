package it.pagopa.selfcare.external_api.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.external_api.core.ContractService;
import it.pagopa.selfcare.external_api.model.documents.ResourceResponse;
import it.pagopa.selfcare.external_api.web.model.document.ContractResource;
import it.pagopa.selfcare.external_api.web.model.mapper.ContractMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/contracts")
@Api(tags = "contracts")
public class ContractController {

    private final ContractService contractService;

    @Autowired
    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }


    @GetMapping(value = "/{institutionId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.external_api.documents.api.getContract}")
    public ContractResource getContract(@ApiParam("${swagger.external_api.institutions.model.id}")
                                        @PathVariable("institutionId")String institutionId,
                                        @ApiParam("${swagger.external_api.products.model.id}")
                                        @RequestParam(value = "productId") String productId){

        ResourceResponse contract = contractService.getContract(institutionId, productId);
        ContractResource resource = ContractMapper.toResource(contract);
        return resource;
    }

}
