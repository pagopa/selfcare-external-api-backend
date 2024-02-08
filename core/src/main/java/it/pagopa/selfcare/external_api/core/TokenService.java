package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.model.token.Token;

import java.util.List;

public interface TokenService {
    List<Token> findByProductId(String productId, int page, int size);
}
