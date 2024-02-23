package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.model.token.Token;
import it.pagopa.selfcare.external_api.model.token.TokenOnboardedUsers;

import java.util.List;

public interface TokenService {
    List<TokenOnboardedUsers> findByProductId(String productId, int page, int size);
}
