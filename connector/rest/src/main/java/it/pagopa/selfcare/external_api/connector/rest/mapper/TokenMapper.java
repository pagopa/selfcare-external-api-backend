package it.pagopa.selfcare.external_api.connector.rest.mapper;

import it.pagopa.selfcare.external_api.model.token.Token;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.TokenResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@Mapper(componentModel = "spring")
public interface TokenMapper {

    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "toOffsetDateTime")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "toOffsetDateTime")
    @Mapping(source = "closedAt", target = "closedAt", qualifiedByName = "toOffsetDateTime")
    Token toEntity(TokenResponse response);

    @Named("toOffsetDateTime")
    default OffsetDateTime map(LocalDateTime localDateTime) {
        return Optional.ofNullable(localDateTime)
                .map(time -> time.atZone(ZoneId.systemDefault()).toOffsetDateTime())
                .orElse(null);
    }
}
