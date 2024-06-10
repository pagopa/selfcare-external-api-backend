package it.pagopa.selfcare.external_api.core;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.api.*;
import it.pagopa.selfcare.external_api.model.institutions.GeographicTaxonomy;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.institutions.SearchMode;
import it.pagopa.selfcare.external_api.model.nationalRegistries.LegalVerification;
import it.pagopa.selfcare.external_api.model.pnpg.CreatePnPgInstitution;
import it.pagopa.selfcare.external_api.model.product.PartyProduct;
import it.pagopa.selfcare.external_api.model.product.ProductOnboardingStatus;
import it.pagopa.selfcare.external_api.model.user.User;
import it.pagopa.selfcare.external_api.model.user.UserInfo;
import it.pagopa.selfcare.external_api.model.user.UserInstitution;
import it.pagopa.selfcare.product.entity.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class})
@ContextConfiguration(classes = InstitutionServiceImpl.class)
@TestPropertySource(locations = "classpath:config/core-config.properties")
class InstitutionServiceImplTest extends BaseServiceTestUtils {

    @Autowired
    private InstitutionServiceImpl institutionService;

    @MockBean
    private MsCoreConnector msCoreConnectorMock;

    @MockBean
    private ProductsConnector productsConnectorMock;

    @MockBean
    private UserRegistryConnector userRegistryConnectorMock;

    @MockBean
    private UserMsConnector userMsConnectorMock;

    @MockBean
    private MsPartyRegistryProxyConnector registryProxyConnector;

    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    void getInstitutionUserProductsWithProductEmpty() {
        String institutionId = "institutionId";
        PartyProduct partyProduct = new PartyProduct();
        partyProduct.setStatus(ProductOnboardingStatus.ACTIVE);
        partyProduct.setRole(PartyRole.MANAGER);
        partyProduct.setId("123");
        String userId = UUID.randomUUID().toString();
        when(productsConnectorMock.getProducts()).thenReturn(Collections.emptyList());
        List<Product> expectation = institutionService.getInstitutionUserProductsV2(institutionId, userId);
        Assertions.assertEquals(0, expectation.size());
    }

    @Test
    void getInstitutionUserProductsWithOneMatch() throws Exception {
        String institutionId = "institutionId";
        ClassPathResource productResponse = new ClassPathResource("expectations/Product.json");
        byte[] productStream = Files.readAllBytes(productResponse.getFile().toPath());
        List<Product> products = objectMapper.readValue(productStream, new TypeReference<>() {});
        PartyProduct partyProduct = new PartyProduct();
        partyProduct.setStatus(ProductOnboardingStatus.ACTIVE);
        partyProduct.setRole(PartyRole.MANAGER);
        partyProduct.setId("123");
        String userId = UUID.randomUUID().toString();
        when(productsConnectorMock.getProducts()).thenReturn(products);
        when(msCoreConnectorMock.getInstitutionUserProductsV2(institutionId, userId)).thenReturn(List.of(partyProduct.getId()));
        List<Product> expectation = institutionService.getInstitutionUserProductsV2(institutionId, userId);
        Assertions.assertEquals(1, expectation.size());
        Assertions.assertEquals(expectation.get(0), products.get(0));
        Mockito.verify(msCoreConnectorMock, Mockito.times(1)).getInstitutionUserProductsV2(institutionId, userId);
        Mockito.verify(productsConnectorMock, Mockito.times(1)).getProducts();
    }

    @Test
    void getInstitutionUserProductsWithTwoMatch() throws Exception {
        String institutionId = "institutionId";
        ClassPathResource productResponse = new ClassPathResource("expectations/Product.json");
        byte[] productStream = Files.readAllBytes(productResponse.getFile().toPath());
        List<Product> products = objectMapper.readValue(productStream, new TypeReference<>() {});

        PartyProduct partyProduct = new PartyProduct();
        partyProduct.setStatus(ProductOnboardingStatus.ACTIVE);
        partyProduct.setRole(PartyRole.MANAGER);
        partyProduct.setId("123");

        PartyProduct partyProduct2 = new PartyProduct();
        partyProduct2.setStatus(ProductOnboardingStatus.ACTIVE);
        partyProduct2.setRole(PartyRole.MANAGER);
        partyProduct2.setId("321");

        String userId = UUID.randomUUID().toString();
        when(productsConnectorMock.getProducts()).thenReturn(products);
        when(msCoreConnectorMock.getInstitutionUserProductsV2(institutionId, userId)).thenReturn(List.of(partyProduct.getId(), partyProduct2.getId()));
        List<Product> expectation = institutionService.getInstitutionUserProductsV2(institutionId, userId);
        Assertions.assertEquals(2, expectation.size());
        Assertions.assertEquals(expectation, products);
        Mockito.verify(msCoreConnectorMock, Mockito.times(1)).getInstitutionUserProductsV2(institutionId, userId);
        Mockito.verify(productsConnectorMock, Mockito.times(1)).getProducts();
    }

    @Test
    void getInstitutionProductUserWithoutInstitutionId() {
        String productId = "productId";
        String userId = "userId";
        Optional<Set<String>> productRole = Optional.empty();
        Executable executable = () -> institutionService.getInstitutionProductUsersV2(null, productId, userId, productRole, null);
        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("An Institution id is required", e.getMessage());
    }

    @Test
    void getInstitutionProductUserWithoutProductId() {
        String institutionId = "institutionId";
        String userId = "userId";
        Optional<Set<String>> productRole = Optional.empty();
        Executable executable = () -> institutionService.getInstitutionProductUsersV2(institutionId, null, userId, productRole, null);
        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("A Product id is required", e.getMessage());
    }

    @Test
    void getInstitutionProductUserV2EmptyList() throws Exception {
        String institutionId = "institutionId";
        String productId = "productId";
        Optional<Set<String>> productRole = Optional.empty();
        String userId = UUID.randomUUID().toString();
        String xSelfCareUid = "onboarding-interceptor";
        ClassPathResource resource = new ClassPathResource("expectations/UserInstitution.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        List<UserInstitution> userInstitutions = objectMapper.readValue(resourceStream, new TypeReference<>() {});
        when(userMsConnectorMock.getUsersInstitutions(userId, institutionId, null, null, null, null, null,null)).thenReturn(userInstitutions);
        ClassPathResource userResource = new ClassPathResource("expectations/User.json");
        byte[] userStream = Files.readAllBytes(userResource.getFile().toPath());
        User user = objectMapper.readValue(userStream, User.class);
        when(userRegistryConnectorMock.getUserByInternalId(eq(userId), any())).thenReturn(user);
        Collection<UserInfo> expectation = institutionService.getInstitutionProductUsersV2(institutionId, productId, userId, productRole, xSelfCareUid);
        Assertions.assertEquals(0, expectation.size());
    }

    @Test
    void getInstitutionProductUserV2WithUuidInServiceType() throws Exception {
        String institutionId = "id";
        String productId = "productId";
        Optional<Set<String>> productRole = Optional.empty();
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        String xSelfCareUid = "onboarding-interceptor";

        ClassPathResource resource = new ClassPathResource("expectations/UserInstitutionV2.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        List<UserInstitution> userInstitutions = objectMapper.readValue(resourceStream, new TypeReference<>() {});
        when(userMsConnectorMock.getUsersInstitutions(userId, institutionId, null, null, null, null, null,null)).thenReturn(userInstitutions);

        ClassPathResource userResource = new ClassPathResource("expectations/User.json");
        byte[] userStream = Files.readAllBytes(userResource.getFile().toPath());
        User user = objectMapper.readValue(userStream, User.class);
        when(userRegistryConnectorMock.getUserByInternalId(eq(userId), any())).thenReturn(user);
        Collection<UserInfo> result = institutionService.getInstitutionProductUsersV2(institutionId, productId, userId, productRole, xSelfCareUid);

        ClassPathResource userInfoResource = new ClassPathResource("expectations/UserInfoV2.json");
        byte[] userInfoStrean = Files.readAllBytes(userInfoResource.getFile().toPath());
        List<UserInfo> expectation = objectMapper.readValue(userInfoStrean, new TypeReference<>() {});

        Assertions.assertEquals(1, expectation.size());
        Assertions.assertEquals(objectMapper.writeValueAsString(expectation), objectMapper.writeValueAsString(result));
    }

    @Test
    void getInstitutionProductUserV2WithoutUuidInServiceType() throws Exception {
        String institutionId = "id";
        String productId = "productId";
        Optional<Set<String>> productRole = Optional.empty();
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        String xSelfCareUid = "uuid";

        ClassPathResource resource = new ClassPathResource("expectations/UserInstitutionV2.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        List<UserInstitution> userInstitutions = objectMapper.readValue(resourceStream, new TypeReference<>() {});
        when(userMsConnectorMock.getUsersInstitutions(userId, institutionId, null, null, null, null, null,null)).thenReturn(userInstitutions);

        ClassPathResource userResource = new ClassPathResource("expectations/UserV2.json");
        byte[] userStream = Files.readAllBytes(userResource.getFile().toPath());
        User user = objectMapper.readValue(userStream, User.class);
        when(userRegistryConnectorMock.getUserByInternalId(eq(userId), any())).thenReturn(user);

        Collection<UserInfo> result = institutionService.getInstitutionProductUsersV2(institutionId, productId, userId, productRole, xSelfCareUid);

        ClassPathResource userInfoResource = new ClassPathResource("expectations/UserInfoWithoutTaxCode.json");
        byte[] userInfoStream = Files.readAllBytes(userInfoResource.getFile().toPath());
        List<UserInfo> expectation = objectMapper.readValue(userInfoStream, new TypeReference<>() {});

        Assertions.assertEquals(1, expectation.size());
        Assertions.assertEquals(objectMapper.writeValueAsString(expectation), objectMapper.writeValueAsString(result));
    }

    @Test
    void getGeographicTaxonomyListWithoutInstitutionId() {
        Executable executable = () -> institutionService.getGeographicTaxonomyList(null);
        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("An Institution id is required", e.getMessage());
        Mockito.verifyNoInteractions(msCoreConnectorMock);
    }

    @Test
    void getGeographicTaxonomyListWithEmptyList() {
        String institutionId = "institutionId";
        when(msCoreConnectorMock.getGeographicTaxonomyList(institutionId)).thenReturn(Collections.emptyList());
        List<GeographicTaxonomy> expectation = institutionService.getGeographicTaxonomyList(institutionId);
        Assertions.assertEquals(0, expectation.size());
    }

    @Test
    void getGeographicTaxonomyList() {
        String institutionId = "institutionId";
        GeographicTaxonomy geographicTaxonomy = new GeographicTaxonomy();
        geographicTaxonomy.setCode("testCode1");
        geographicTaxonomy.setDesc("testDesc1");
        GeographicTaxonomy geographicTaxonomy2 = new GeographicTaxonomy();
        geographicTaxonomy2.setCode("testCode2");
        geographicTaxonomy2.setDesc("testDesc2");
        when(msCoreConnectorMock.getGeographicTaxonomyList(institutionId)).thenReturn(List.of(geographicTaxonomy, geographicTaxonomy2));
        List<GeographicTaxonomy> expectation = institutionService.getGeographicTaxonomyList(institutionId);
        Assertions.assertEquals(2, expectation.size());
        Assertions.assertEquals(geographicTaxonomy.getCode(), expectation.get(0).getCode());
        Assertions.assertEquals(geographicTaxonomy.getDesc(), expectation.get(0).getDesc());
        Assertions.assertEquals(geographicTaxonomy2.getCode(), expectation.get(1).getCode());
        Assertions.assertEquals(geographicTaxonomy2.getDesc(), expectation.get(1).getDesc());
        Mockito.verify(msCoreConnectorMock, Mockito.times(1)).getGeographicTaxonomyList(any());
    }

    @Test
    void getInstitutionsByGeoTaxonomiesWithoutGeoTaxonomies() {
        SearchMode searchMode = SearchMode.exact;
        Executable executable = () -> institutionService.getInstitutionsByGeoTaxonomies(Collections.emptySet(), searchMode);
        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("GeoTaxonomy ids are required in order to retrieve the institutions", e.getMessage());
    }

    @Test
    void getInstitutionsByGeoTaxonomiesWithEmptyList(){
        Set<String> geoTaxIds = Set.of("geoTaxId1", "geoTaxId2");
        SearchMode searchMode = SearchMode.any;
        when(msCoreConnectorMock.getInstitutionsByGeoTaxonomies(String.join(",",geoTaxIds), searchMode)).thenReturn(Collections.emptyList());
        Collection<Institution> expectation = institutionService.getInstitutionsByGeoTaxonomies(geoTaxIds, searchMode);
        Assertions.assertEquals(0, expectation.size());
    }

    @Test
    void getInstitutionsByGeoTaxonomies() throws IOException {
        Set<String> geoTaxIds = Set.of("geoTaxId1", "geoTaxId2");
        SearchMode searchMode = SearchMode.any;
        ClassPathResource productResponse = new ClassPathResource("expectations/Institution.json");
        byte[] institutionStream = Files.readAllBytes(productResponse.getFile().toPath());
        List<Institution> institution = objectMapper.readValue(institutionStream, new TypeReference<>() {
       });
        when(msCoreConnectorMock.getInstitutionsByGeoTaxonomies(String.join(",",geoTaxIds), searchMode)).thenReturn(institution);
        Collection<Institution> expectation = institutionService.getInstitutionsByGeoTaxonomies(geoTaxIds, searchMode);
        Assertions.assertEquals(2, expectation.size());
        Assertions.assertEquals(expectation, institution);
    }

    @Test
    void addInstitution() {
        CreatePnPgInstitution createPnPgInstitution = new CreatePnPgInstitution();
        createPnPgInstitution.setDescription("description");
        createPnPgInstitution.setExternalId("taxId");
        String institutionId = UUID.randomUUID().toString();
        when(msCoreConnectorMock.createPgInstitution("description", "taxId")).thenReturn(institutionId);
        String expectation = institutionService.addInstitution(createPnPgInstitution);
        Assertions.assertEquals(institutionId, expectation);
    }

    @Test
    void verifyLegal(){
        final String taxId = "taxId";
        final String vatNumber = "vatNumber";

        when(registryProxyConnector.verifyLegal(anyString(), anyString())).thenReturn(new LegalVerification());

        //when
        LegalVerification result = institutionService.verifyLegal(taxId, vatNumber);

        //then
        assertNotNull(result);
        verify(registryProxyConnector, times(1)).verifyLegal(taxId, vatNumber);

    }
}
