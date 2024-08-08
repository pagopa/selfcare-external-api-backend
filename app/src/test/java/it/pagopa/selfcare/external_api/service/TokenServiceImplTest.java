package it.pagopa.selfcare.external_api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.external_api.model.token.TokenOnboardedUsers;
import it.pagopa.selfcare.onboarding.common.OnboardingStatus;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.api.OnboardingControllerApi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

@ExtendWith({MockitoExtension.class})
class TokenServiceImplTest extends BaseServiceTestUtils {

    @InjectMocks
    private TokenServiceImpl tokenService;

    @Mock
    private OnboardingControllerApi onboardingControllerApi;


    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    void findByProductId() throws Exception {
        ClassPathResource inputResource = new ClassPathResource("expectations/TokenOnboardedUsers.json");
        byte[] tokenOnboardedUsersStream = Files.readAllBytes(inputResource.getFile().toPath());
        List<TokenOnboardedUsers> tokenOnboardedUsers = objectMapper.readValue(tokenOnboardedUsersStream, new TypeReference<>() {});
        List<TokenOnboardedUsers> tokens = this.tokenService.findByProductId("id", 1, 10, OnboardingStatus.COMPLETED.name());
        Assertions.assertEquals(tokenOnboardedUsers, tokens);
    }

    @Test
    void findByProductIdEmptyList(){
        List<TokenOnboardedUsers> tokens = this.tokenService.findByProductId("id", 1, 10, null);
        Assertions.assertEquals(0, tokens.size());
    }
}
