package it.pagopa.selfcare.external_api.connector.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.InstitutionResponse;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.OnboardingResponse;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.OnboardingsResponse;
import it.pagopa.selfcare.external_api.connector.rest.client.MsCoreInstitutionApiClient;
import it.pagopa.selfcare.external_api.connector.rest.client.MsCoreRestClient;
import it.pagopa.selfcare.external_api.connector.rest.client.MsUserApiRestClient;
import it.pagopa.selfcare.external_api.connector.rest.config.BaseConnectorTest;
import it.pagopa.selfcare.external_api.connector.rest.mapper.InstitutionMapperImpl;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.Institutions;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.institutions.GeographicTaxonomy;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.institutions.SearchMode;
import it.pagopa.selfcare.external_api.model.onboarding.InstitutionOnboarding;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionInfo;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.UserDataResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;

import javax.validation.ValidationException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static it.pagopa.selfcare.external_api.model.user.RelationshipState.ACTIVE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MsCoreConnectorImplTest extends BaseConnectorTest {

    @InjectMocks
    private MsCoreConnectorImpl msCoreConnector;

    @Mock
    private MsCoreRestClient msCoreRestClient;

    @Mock
    private MsCoreInstitutionApiClient institutionApiClient;

    @Mock
    private MsUserApiRestClient msUserApiRestClient;

    @Spy
    InstitutionMapperImpl institutionMapper;


    @Test
    void getInstitutionOnboardings() throws IOException {
        String institutionId = "institutionId";
        String productId = "productId";

        ClassPathResource resource = new ClassPathResource("stubs/OnboardingResponse.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        List<OnboardingResponse> onboardings = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });
        OnboardingsResponse onboardingsResponse = new OnboardingsResponse();
        onboardingsResponse.setOnboardings(onboardings);
        when(institutionApiClient._getOnboardingsInstitutionUsingGET(institutionId, productId))
                .thenReturn(ResponseEntity.ok(onboardingsResponse));

        InstitutionOnboarding result = msCoreConnector.getInstitutionOnboardings(institutionId, productId);
        Assertions.assertEquals(onboardingsResponse.getOnboardings().get(0).getTokenId(), result.getTokenId());
    }


    @Test
    void getInstitutionOnboardingsResourceNotFound() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";

        OnboardingsResponse onboardings = new OnboardingsResponse();
        onboardings.setOnboardings(Collections.emptyList());
        when(institutionApiClient._getOnboardingsInstitutionUsingGET(institutionId, productId))
                .thenReturn(ResponseEntity.ok(onboardings));

        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> msCoreConnector.getInstitutionOnboardings(institutionId, productId));

    }

    @Test
    void getInstitutionByExternalIdWithoutExternalInstitutionId() {
        assertThrows(IllegalArgumentException.class,
                () -> msCoreConnector.getInstitutionByExternalId(null),
                "An Institution external id is required");
    }

    @Test
    void getInstitutionByExternalId() {
        String externalInstitutionId = "externalInstitutionId";
        Institution institution = new Institution();
        institution.setId("institutionId");
        when(msCoreRestClient.getInstitutionByExternalId(externalInstitutionId))
                .thenReturn(institution);

        Institution result = msCoreConnector.getInstitutionByExternalId(externalInstitutionId);
        assertEquals(institution.getId(), result.getId());
    }

    @Test
    void getGeographicTaxonomyListWithoutInstitutionId() {
        assertThrows(IllegalArgumentException.class,
                () -> msCoreConnector.getGeographicTaxonomyList(null),
                "An institutionId is required ");
    }

    @Test
    void getGeographicTaxonomyListWithoutGeographicTaxonomies() {

        String institutionId = "institutionId";

        Institution institution = new Institution();
        institution.setGeographicTaxonomies(null);

        when(msCoreRestClient.getInstitution(institutionId)).thenReturn(institution);

        assertThrows(ValidationException.class,
                () -> msCoreConnector.getGeographicTaxonomyList(institutionId),
                "The institution institutionId does not have geographic taxonomies.");
    }

    @Test
    void getGeographicTaxonomyList() throws IOException {

        String institutionId = "institutionId";

        ClassPathResource resource = new ClassPathResource("stubs/Institution.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        Institution institution = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(msCoreRestClient.getInstitution(any())).thenReturn(institution);

        List<GeographicTaxonomy> result = msCoreConnector.getGeographicTaxonomyList(institutionId);

        assertEquals(institution.getGeographicTaxonomies(), result);
        verify(msCoreRestClient, times(1)).getInstitution(institutionId);
    }

    @Test
    void getInstitutionsByGeoTaxonomiesWithInstitutionNull()  {

        String geoTaxIds = "geoTaxIds";
        SearchMode searchMode = SearchMode.any;

        Institutions institutions = new Institutions();

        when(msCoreRestClient.getInstitutionsByGeoTaxonomies(geoTaxIds, searchMode))
                .thenReturn(institutions);

        assertThrows(ResourceNotFoundException.class,
                () -> msCoreConnector.getInstitutionsByGeoTaxonomies(geoTaxIds, searchMode),
                "No institutions where found for given taxIds = geoTaxIds");
    }

    @Test
    void getInstitutionsByGeoTaxonomies() throws IOException {
        //given
        String geoTaxIds = "geoTaxIds";
        SearchMode searchMode = SearchMode.all;

        ClassPathResource resource = new ClassPathResource("stubs/Institution.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        Institution institution = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });
        Institutions institutions = new Institutions();
        institutions.setItems(List.of(institution));
        when(msCoreRestClient.getInstitutionsByGeoTaxonomies(geoTaxIds, searchMode))
                .thenReturn(institutions);

        Collection<Institution> result = msCoreConnector.getInstitutionsByGeoTaxonomies(geoTaxIds, searchMode);

        assertNotNull(result);
        Assertions.assertEquals(institutions.getItems(), result);
        verify(msCoreRestClient, times(1)).getInstitutionsByGeoTaxonomies(geoTaxIds, searchMode);
        verifyNoMoreInteractions(msCoreRestClient);
    }

    @Test
    void getInstitutionUserProductsV2WithoutInstitutionId() {
        assertThrows(IllegalArgumentException.class,
                () -> msCoreConnector.getInstitutionUserProductsV2(null, "userId"),
                "An institutionId is required ");
    }

    @Test
    void getInstitutionUserProductsV2WithoutUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> msCoreConnector.getInstitutionUserProductsV2("institutionId", null),
                "A userId is required");
    }

    @Test
    void getInstitutionUserProductsV2() throws IOException {

        String institutionId = "institutionId";
        String userId = "userId";

        ClassPathResource resource = new ClassPathResource("stubs/UserDataResponse.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        List<UserDataResponse> userDataResponses = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(msUserApiRestClient._usersUserIdInstitutionInstitutionIdGet(institutionId, userId, userId, null, null, null, List.of(ACTIVE.name())))
                .thenReturn(ResponseEntity.ok(userDataResponses));

        List<String> response = msCoreConnector.getInstitutionUserProductsV2(institutionId, userId);
        Assertions.assertEquals(1, response.size());
        Assertions.assertEquals("productId",String.join(",", response));
    }

    @Test
    void getInstitutionUserProductsV2EmptyList1()  {

        String institutionId = "institutionId";
        String userId = "userId";

        when(msUserApiRestClient._usersUserIdInstitutionInstitutionIdGet(institutionId, userId, userId, null, null, null, List.of(ACTIVE.name())))
                .thenReturn(ResponseEntity.of(Optional.empty()));

        List<String> response = msCoreConnector.getInstitutionUserProductsV2(institutionId, userId);
        Assertions.assertEquals(0, response.size());
    }

    @Test
    void getInstitutionUserProductsV2EmptyList2() {

        String institutionId = "institutionId";
        String userId = "userId";

        when(msUserApiRestClient._usersUserIdInstitutionInstitutionIdGet(institutionId, userId, userId, null, null, null, List.of(ACTIVE.name())))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        List<String> response = msCoreConnector.getInstitutionUserProductsV2(institutionId, userId);
        Assertions.assertEquals(0, response.size());
    }

    @Test
    void getInstitutionDetailsEmptyResponse() {
        when(institutionApiClient._retrieveInstitutionByIdUsingGET(anyString()))
                .thenReturn(ResponseEntity.of(Optional.empty()));

        List<OnboardedInstitutionInfo> onboardedInstitutionInfos = msCoreConnector.getInstitutionDetails("id");

        assertEquals(0, onboardedInstitutionInfos.size());
    }

    @Test
    void getInstitutionDetailsEmptyResponse2() throws IOException {
        String institutionId = "institutionId";

        ClassPathResource resource = new ClassPathResource("stubs/institutionResponse.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        InstitutionResponse response = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });
        response.setOnboarding(null);

        when(institutionApiClient._retrieveInstitutionByIdUsingGET(institutionId))
                .thenReturn(ResponseEntity.ok(response));

        List<OnboardedInstitutionInfo> onboardedInstitutionInfos = msCoreConnector.getInstitutionDetails(institutionId);

        assertEquals(0, onboardedInstitutionInfos.size());
    }

    @Test
    void getInstitutionsDetails() throws IOException {
        String institutionId = "institutionId";

        ClassPathResource resource = new ClassPathResource("stubs/institutionResponse.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        InstitutionResponse response = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(institutionApiClient._retrieveInstitutionByIdUsingGET(anyString()))
                .thenReturn(ResponseEntity.of(Optional.of(response)));

        ClassPathResource expectationResource = new ClassPathResource("stubs/OnboardedInstitutionInfo.json");
        byte[] expectationStream = Files.readAllBytes(expectationResource.getFile().toPath());
        List<OnboardedInstitutionInfo> expectation = objectMapper.readValue(expectationStream, new TypeReference<>() {
        });

        List<OnboardedInstitutionInfo> onboardedInstitutionInfos = msCoreConnector.getInstitutionDetails(institutionId);
        assertEquals(1, onboardedInstitutionInfos.size());
        assertEquals(expectation, onboardedInstitutionInfos);
        verify(institutionApiClient, times(1))._retrieveInstitutionByIdUsingGET(institutionId);

    }

    @Test
    void createPgInstitution() {
        String institutionId = UUID.randomUUID().toString();
        //given
        InstitutionResponse response = new InstitutionResponse();
        response.setId(institutionId);
        when(institutionApiClient._createPgInstitutionUsingPOST(any())).thenReturn(ResponseEntity.of(Optional.of(response)));
        //when
        String institutionPnPgResponse = msCoreConnector.createPgInstitution("description",  "taxId");
        //then
        assertEquals(institutionId, institutionPnPgResponse);

    }

    @Test
    void getInstitutionsDetailsBillingNull() throws IOException {
        String institutionId = "institutionId";

        ClassPathResource resource = new ClassPathResource("stubs/institutionResponse.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        InstitutionResponse response = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });
        response.getOnboarding().forEach(onboardedProductResponse -> onboardedProductResponse.setBilling(null));

        when(institutionApiClient._retrieveInstitutionByIdUsingGET(anyString()))
                .thenReturn(ResponseEntity.of(Optional.of(response)));

        ClassPathResource expectationResource = new ClassPathResource("stubs/OnboardedInstitutionInfo.json");
        byte[] expectationStream = Files.readAllBytes(expectationResource.getFile().toPath());
        List<OnboardedInstitutionInfo> expectation = objectMapper.readValue(expectationStream, new TypeReference<>() {
        });
        expectation.forEach(onboardedInstitutionInfo -> onboardedInstitutionInfo.setBilling(null));

        List<OnboardedInstitutionInfo> onboardedInstitutionInfos = msCoreConnector.getInstitutionDetails(institutionId);
        assertEquals(1, onboardedInstitutionInfos.size());
        assertEquals(expectation, onboardedInstitutionInfos);
        verify(institutionApiClient, times(1))._retrieveInstitutionByIdUsingGET(institutionId);

    }
}
