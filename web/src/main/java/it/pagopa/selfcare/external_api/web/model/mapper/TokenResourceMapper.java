package it.pagopa.selfcare.external_api.web.model.mapper;

import it.pagopa.selfcare.external_api.model.token.Token;
import it.pagopa.selfcare.external_api.web.model.tokens.TokenResource;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TokenResourceMapper {

    TokenResource toResponse(Token token);
}
