package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.external_api.api.*;
import it.pagopa.selfcare.external_api.core.strategy.OnboardingValidationStrategy;
import it.pagopa.selfcare.external_api.model.onboarding.User;
import it.pagopa.selfcare.external_api.model.onboarding.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.TestSecurityContextHolder;
import java.util.*;
import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceImplTest {

    @InjectMocks
    private OnboardingServiceImpl onboardingServiceImpl;

    @Mock
    private MsCoreConnector partyConnectorMock;

    @Mock
    private ProductsConnector productsConnectorMock;

    @Mock
    private OnboardingValidationStrategy onboardingValidationStrategyMock;

    @Mock
    private UserRegistryConnector userRegistryConnectorMock;

    @Mock
    private MsPartyRegistryProxyConnector msPartyRegistryProxyConnectorMock;

    @Mock
    private OnboardingMsConnector onboardingMsConnectorMock;

    @Captor
    private ArgumentCaptor<OnboardingImportData> onboardingImportDataCaptor;

    @Captor
    private ArgumentCaptor<OnboardingData> onboardingDataCaptor;

    private final static User dummyManager;
    private final static User dummyDelegate;

    @BeforeEach
    void beforeEach() {
        TestSecurityContextHolder.clearContext();
    }

    static {
        dummyManager = new User();
        dummyManager.setEmail("manager@pec.it");
        dummyManager.setName("manager");
        dummyManager.setSurname("manager");
        dummyManager.setTaxCode("manager");
        dummyManager.setRole(PartyRole.MANAGER);


        dummyDelegate = new User();
        dummyDelegate.setEmail("delegate@pec.it");
        dummyDelegate.setName("delegate");
        dummyDelegate.setSurname("delegate");
        dummyDelegate.setTaxCode("delegate");
        dummyDelegate.setRole(PartyRole.DELEGATE);
    }


    @Test
    void onboardingProductAsync() {
        // given
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.PA);
        onboardingData.setUsers(List.of(dummyManager, dummyDelegate));
        // when
        onboardingServiceImpl.autoApprovalOnboardingProductV2(onboardingData);
        // then
        verify(onboardingMsConnectorMock, times(1))
                .onboarding(any());
    }

    @Test
    void onboardingImportPa() {
        // given
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setUsers(List.of(dummyManager, dummyDelegate));
        // when
        onboardingServiceImpl.oldContractOnboardingV2(onboardingData);
        // then
        verify(onboardingMsConnectorMock, times(1))
                .onboardingImportPA(any());
    }

}
