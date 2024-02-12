package it.pagopa.selfcare.external_api.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import it.pagopa.selfcare.external_api.core.OnboardingService;
import it.pagopa.selfcare.external_api.core.TokenService;
import it.pagopa.selfcare.external_api.model.token.Token;
import it.pagopa.selfcare.external_api.model.token.TokenOnboardedUsers;
import it.pagopa.selfcare.external_api.web.model.mapper.OnboardingResourceMapper;
import it.pagopa.selfcare.external_api.web.model.mapper.TokenResourceMapper;
import it.pagopa.selfcare.external_api.web.model.tokens.TokensResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(value = "/v1/tokens", produces = APPLICATION_JSON_VALUE)
@Api(tags = "tokens")
public class TokenController {

    private final TokenService tokenService;
    private final TokenResourceMapper tokenResourceMapper;

    @Autowired
    public TokenController(TokenService tokenService,
                           TokenResourceMapper tokenResourceMapper) {
        this.tokenService = tokenService;
        this.tokenResourceMapper = tokenResourceMapper;
    }

    /**
     * Retrieve tokens from productId in input
     *
     * @param productId String
     * @param page      Integer
     * @param size      Integer
     * @return List
     * * Code: 200, Message: successful operation
     * * Code: 404, Message: product not found
     */
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "${swagger.external_api.api.tokens.findFromProduct}", notes = "${swagger.external_api.api.tokens.findFromProduct}")
    @GetMapping(value = "/products/{productId}")
    public ResponseEntity<TokensResource> findFromProduct(@ApiParam("${swagger.external_api.api.tokens.productId}")
                                                          @PathVariable(value = "productId") String productId,
                                                          @ApiParam("${swagger.external_api.page.number}")
                                                          @RequestParam(name = "page", defaultValue = "0") Integer page,
                                                          @ApiParam("${swagger.external_api.page.size}")
                                                          @RequestParam(name = "size", defaultValue = "100") Integer size) {
        log.trace("findFromProduct start");
        log.debug("findFromProduct productId = {}", productId);
        List<TokenOnboardedUsers> tokens = tokenService.findByProductId(productId, page, size);

        TokensResource tokenListResponse = new TokensResource(
                tokens.stream()
                        .map(tokenResourceMapper::toResponse)
                        .collect(Collectors.toList()));

        log.trace("findFromProduct end");
        return ResponseEntity.ok().body(tokenListResponse);
    }
}
