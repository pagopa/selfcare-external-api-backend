package it.pagopa.selfcare.external_api.connector.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.pagopa.selfcare.external_api.connector.rest.client.MsCoreRestClient;
import it.pagopa.selfcare.external_api.connector.rest.client.MsUserApiRestClient;
import it.pagopa.selfcare.external_api.connector.rest.mapper.InstitutionMapper;
import it.pagopa.selfcare.external_api.connector.rest.mapper.InstitutionMapperImpl;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.Institutions;
import it.pagopa.selfcare.external_api.connector.rest.model.onboarding.OnboardingImportInstitutionRequest;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.institutions.Attribute;
import it.pagopa.selfcare.external_api.model.institutions.GeographicTaxonomy;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.institutions.SearchMode;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.UserDataResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;

import javax.validation.ValidationException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.external_api.connector.rest.MsCoreConnectorImpl.*;
import static it.pagopa.selfcare.external_api.model.user.RelationshipState.ACTIVE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartyConnectorImplTest {

    private final ObjectMapper mapper;

    public PartyConnectorImplTest() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setTimeZone(TimeZone.getDefault());
    }

    @InjectMocks
    private MsCoreConnectorImpl partyConnector;

    @Mock
    private MsCoreRestClient msCoreRestClient;

    @Mock
    private MsUserApiRestClient msUserApiRestClientMock;

    @Captor
    ArgumentCaptor<OnboardingImportInstitutionRequest> onboardingImportRequestCaptor;

    @Spy
    private InstitutionMapper institutionMapper = new InstitutionMapperImpl();





    @Test
    void getInstitutionUserProductsV2_nullInstitutionId() {
        //given
        final String institutionId = null;
        final String userId = "userId";
        //when
        Executable executable = () -> partyConnector.getInstitutionUserProductsV2(institutionId, userId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(INSTITUTION_ID_IS_REQUIRED, e.getMessage());
    }

    @Test
    void getInstitutionUserProductsV2_nullUserId() {
        //given
        final String institutionId = "institutionId";
        final String userId = null;
        //when
        Executable executable = () -> partyConnector.getInstitutionUserProductsV2(institutionId, userId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(USER_ID_IS_REQUIRED, e.getMessage());
    }


    @Test
    void getInstitutionUserProductsV2_nullResponse() {
        //given
        final String institutionId = "institutionId";
        final String userId = "userId";
        //when
        List<String> products = partyConnector.getInstitutionUserProductsV2(institutionId, userId);
        //then
        assertNotNull(products);
        assertTrue(products.isEmpty());
        verify(msUserApiRestClientMock, times(1))
                ._usersUserIdInstitutionInstitutionIdGet(eq(institutionId),
                        eq(userId),
                        eq(userId),
                        isNull(),
                        isNull(),
                        isNull(),
                        eq(List.of(ACTIVE.name())));
        verifyNoMoreInteractions(msUserApiRestClientMock);
    }


    @Test
    void getInstitutionUserProductsV2() throws IOException {
        //given
        final String institutionId = "institutionId";
        final String userId = "userId";

        File stubs = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/user_institutions_to_product.json");
        List<UserDataResponse> response = mapper.readValue(stubs, new TypeReference<List<UserDataResponse>>(){});
        when(msUserApiRestClientMock._usersUserIdInstitutionInstitutionIdGet(any(), any(), any(), any(), any(), any(), anyList()))
                .thenReturn(ResponseEntity.of(Optional.of(response)));
        //when
        List<String> products = partyConnector.getInstitutionUserProductsV2(institutionId, userId);
        //then
        assertNotNull(products);
        assertEquals(1, products.size());
        verify(msUserApiRestClientMock, times(1))
                ._usersUserIdInstitutionInstitutionIdGet(eq(institutionId),
                        eq(userId),
                        eq(userId),
                        isNull(),
                        isNull(),
                        isNull(),
                        eq(List.of(ACTIVE.name())));
        verifyNoMoreInteractions(msUserApiRestClientMock);
    }




    @Test
    void getInstitutionByExternalId() {
        //given
        String institutionId = "institutionId";
        Institution institutionMock = mockInstance(new Institution());
        institutionMock.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        when(msCoreRestClient.getInstitutionByExternalId(anyString()))
                .thenReturn(institutionMock);
        //when
        Institution institution = partyConnector.getInstitutionByExternalId(institutionId);
        //then
        assertNotNull(institution);
        assertEquals(institutionMock.getExternalId(), institution.getExternalId());
        assertEquals(institutionMock.getDescription(), institution.getDescription());
        assertEquals(institutionMock.getAddress(), institution.getAddress());
        assertEquals(institutionMock.getTaxCode(), institution.getTaxCode());
        assertEquals(institutionMock.getId(), institution.getId());
        assertEquals(institutionMock.getZipCode(), institution.getZipCode());
        assertEquals(institutionMock.getDigitalAddress(), institution.getDigitalAddress());
        assertEquals(institutionMock.getInstitutionType(), institution.getInstitutionType());
        assertEquals(institutionMock.getGeographicTaxonomies().get(0).getCode(), institution.getGeographicTaxonomies().get(0).getCode());
        assertEquals(institutionMock.getGeographicTaxonomies().get(0).getDesc(), institution.getGeographicTaxonomies().get(0).getDesc());
        verify(msCoreRestClient, times(1))
                .getInstitutionByExternalId(institutionId);
        verifyNoMoreInteractions(msCoreRestClient);
    }

    @Test
    void getInstitutionByExternalId_nullInstitutionId() {
        //given
        String institutionId = null;
        //when
        Executable exe = () -> partyConnector.getInstitutionByExternalId(institutionId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, exe);
        assertEquals(REQUIRED_INSTITUTION_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(msCoreRestClient);
    }

    @Test
    void getGeographicTaxonomyList_nullInstitutionId() {
        // given
        String institutionId = null;
        // when
        Executable executable = () -> partyConnector.getGeographicTaxonomyList(institutionId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(INSTITUTION_ID_IS_REQUIRED, e.getMessage());
        verifyNoInteractions(msCoreRestClient, msCoreRestClient);
    }

    @Test
    void getGeographicTaxonomyList_noGeographicTaxonomies() {
        // given
        String institutionId = "institutionId";
        Institution institutionMock = mockInstance(new Institution());
        when(msCoreRestClient.getInstitution(any()))
                .thenReturn(institutionMock);
        // when
        Executable executable = () -> partyConnector.getGeographicTaxonomyList(institutionId);
        // then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals(String.format("The institution %s does not have geographic taxonomies.", institutionId), e.getMessage());
        verify(msCoreRestClient, times(1))
                .getInstitution(institutionId);
        verifyNoMoreInteractions(msCoreRestClient);
    }

    @Test
    void getGeographicTaxonomyList() {
        // given
        String institutionId = "institutionId";
        Institution institutionMock = mockInstance(new Institution());
        Attribute attribute = mockInstance(new Attribute());
        institutionMock.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        institutionMock.setAttributes(List.of(attribute));
        when(msCoreRestClient.getInstitution(any()))
                .thenReturn(institutionMock);
        // when
        List<GeographicTaxonomy> geographicTaxonomies = partyConnector.getGeographicTaxonomyList(institutionId);
        // then
        assertSame(institutionMock.getGeographicTaxonomies(), geographicTaxonomies);
        assertNotNull(geographicTaxonomies);
        verify(msCoreRestClient, times(1))
                .getInstitution(institutionId);
        verifyNoMoreInteractions(msCoreRestClient);
    }

    @Test
    void getInstitutionsByGeoTaxonomies() throws IOException {
        //given
        String geoTaxIds = "taxId";
        SearchMode searchMode = SearchMode.any;
        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/institutions.json");
        Institutions institutions = mapper.readValue(stub, Institutions.class);

        when(msCoreRestClient.getInstitutionsByGeoTaxonomies(anyString(), any()))
                .thenReturn(institutions);
        //when
        Collection<Institution> results = partyConnector.getInstitutionsByGeoTaxonomies(geoTaxIds, searchMode);
        //then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(2, results.size());
        verify(msCoreRestClient, times(1))
                .getInstitutionsByGeoTaxonomies(geoTaxIds, searchMode);
    }

    @Test
    void getInstitutionsByGeoTaxonomies_nullResponse() {
        //given
        String geoTax = "geoTaxIds";
        SearchMode searchMode = SearchMode.any;
        when(msCoreRestClient.getInstitutionsByGeoTaxonomies(any(), any()))
                .thenReturn(new Institutions());
        //when
        Executable executable = () -> partyConnector.getInstitutionsByGeoTaxonomies(geoTax, searchMode);
        //then
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, executable);
        assertEquals(String.format("No institutions where found for given taxIds = %s", geoTax), e.getMessage());
        verify(msCoreRestClient, times(1))
                .getInstitutionsByGeoTaxonomies(geoTax, searchMode);
        verifyNoMoreInteractions(msCoreRestClient);
    }


}
