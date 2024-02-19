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
import java.util.Optional;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", imports = List.class)
public interface TokenResourceMapper {

    @Mapping(target = "legals", source = ".", qualifiedByName = "toLegalsResponse")
    TokenResource toResponse(TokenOnboardedUsers tokens);

    @Named("toLegalsResponse")
    default List<LegalsResource> toLegalsResponse(TokenOnboardedUsers tokenOnboardedUsers){
        return tokenOnboardedUsers.getUsers().stream()
                    .map(user -> {
                        LegalsResource legalsResponse = new LegalsResource();
                        legalsResponse.setPartyId(user.getUserId());
                        legalsResponse.setRole(user.getRole());
                        return legalsResponse;
                    }).toList();
    }
}
