package it.pagopa.selfcare.external_api.web.model.mapper;

import it.pagopa.selfcare.external_api.model.token.TokenOnboardedUsers;
import it.pagopa.selfcare.external_api.model.user.UserProducts;
import it.pagopa.selfcare.external_api.web.model.tokens.LegalsResource;
import it.pagopa.selfcare.external_api.web.model.tokens.TokenResource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", imports = List.class)
public interface TokenResourceMapper {

    @Mapping(target = "legals",  expression = "java(toLegalsResponse(tokens))")
    TokenResource toResponse(TokenOnboardedUsers tokens);

    @Named("toLegalsResponse")
    default List<LegalsResource> toLegalsResponse(TokenOnboardedUsers tokenOnboardedUsers){
        List<LegalsResource> legalsResponses = new ArrayList<>();
        for(UserProducts user: tokenOnboardedUsers.getOnboardedUsers()){
            List<LegalsResource> list = user.getBindings().stream()
                    .filter(userBinding -> tokenOnboardedUsers.getInstitutionId().equalsIgnoreCase(userBinding.getInstitutionId()))
                    .flatMap(userBinding -> userBinding.getProducts().stream())
                    .filter(onboardedProduct -> tokenOnboardedUsers.getProductId().equalsIgnoreCase(onboardedProduct.getProductId()))
                    .filter(onboardedProduct -> tokenOnboardedUsers.getId().equals(onboardedProduct.getTokenId()))
                    .map(product -> {
                        LegalsResource legalsResponse = new LegalsResource();
                        legalsResponse.setPartyId(user.getId());
                        legalsResponse.setEnv(product.getEnv());
                        legalsResponse.setRole(product.getRole());
                        return legalsResponse;
                    }).collect(Collectors.toList());
            legalsResponses.addAll(list);
        }
        return legalsResponses;
    }
}
