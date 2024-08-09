package it.pagopa.selfcare.external_api.service;

import it.pagopa.selfcare.external_api.client.MsOnboardingControllerApi;
import it.pagopa.selfcare.external_api.mapper.TokenMapper;
import it.pagopa.selfcare.external_api.model.token.TokenOnboardedUsers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final MsOnboardingControllerApi onboardingControllerApi;
    private final TokenMapper tokenMapper;

    @Override
    public List<TokenOnboardedUsers> findByProductId(String productId, int page, int size, String status) {
        log.trace("findByProductId start");
        log.debug("findByProductId parameter: {}", productId);
        List<TokenOnboardedUsers> tokenOnboardedUsers =  Objects.requireNonNull(
                        onboardingControllerApi._v1OnboardingGet(null, page, productId, size, status, null, null).getBody())
                .getItems().stream()
                .map(tokenMapper::toEntity)
                .toList();
        log.trace("findByProductId end");
        return tokenOnboardedUsers;
    }
}
