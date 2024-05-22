package it.pagopa.selfcare.external_api.core;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.external_api.api.OnboardingMsConnector;
import it.pagopa.selfcare.external_api.model.token.TokenOnboardedUsers;
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
    private OnboardingMsConnector onboardingMsConnector;

    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    void findByProductId() throws Exception {
        ClassPathResource inputResource = new ClassPathResource("expectations/TokenOnboardedUsers.json");
        byte[] tokenOnboardedUsersStream = Files.readAllBytes(inputResource.getFile().toPath());
        List<TokenOnboardedUsers> tokenOnboardedUsers = objectMapper.readValue(tokenOnboardedUsersStream, new TypeReference<>() {});
        Mockito.when(this.onboardingMsConnector.getOnboardings("id", 1, 10)).thenReturn(tokenOnboardedUsers);
        List<TokenOnboardedUsers> tokens = this.tokenService.findByProductId("id", 1, 10);
        Assertions.assertEquals(tokenOnboardedUsers, tokens);
    }

    @Test
    void findByProductIdEmptyList(){
        Mockito.when(this.onboardingMsConnector.getOnboardings("id", 1, 10)).thenReturn(Collections.emptyList());
        List<TokenOnboardedUsers> tokens = this.tokenService.findByProductId("id", 1, 10);
        Assertions.assertEquals(0, tokens.size());
    }
}
