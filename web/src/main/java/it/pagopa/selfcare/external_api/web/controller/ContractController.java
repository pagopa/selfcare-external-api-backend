package it.pagopa.selfcare.external_api.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.external_api.core.ContractService;
import it.pagopa.selfcare.external_api.model.documents.ResourceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@Slf4j
@RestController
@RequestMapping(value = "/contracts")
@Api(tags = "contracts")
public class ContractController {

    private final ContractService contractService;

    @Autowired
    public ContractController(ContractService contractService) {
        log.trace("Initializing {}...", ContractController.class.getSimpleName());
        this.contractService = contractService;
    }


    @GetMapping(value = "/{institutionId}", produces = APPLICATION_OCTET_STREAM_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.external_api.documents.api.getContract}")
    public ResponseEntity<byte[]> getContract(@ApiParam("${swagger.external_api.institutions.model.id}")
                                        @PathVariable("institutionId")String institutionId,
                                                        @ApiParam("${swagger.external_api.products.model.id}")
                                        @RequestParam(value = "productId") String productId){
        log.trace("getContract start");
        log.debug("getContract institutionId = {}, productId = {}", institutionId, productId);
        ResourceResponse contract = contractService.getContract(institutionId, productId);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + contract.getFileName());
        log.info("contentType: {}", headers.getContentType());
        log.debug("getContract result = {}", contract);
        log.trace("getContract end");
        return ResponseEntity.ok().headers(headers).body(contract.getData());
    }

}
