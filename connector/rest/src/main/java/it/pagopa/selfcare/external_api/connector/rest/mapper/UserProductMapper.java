package it.pagopa.selfcare.external_api.connector.rest.mapper;

import it.pagopa.selfcare.core.generated.openapi.v1.dto.UserProductsResponse;
import it.pagopa.selfcare.external_api.model.user.UserProducts;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserProductMapper {

    UserProducts toEntity(UserProductsResponse userProductsResponse);
}
