package it.pagopa.selfcare.external_api.web.model.mapper;

import it.pagopa.selfcare.external_api.model.user.UserInfoWrapper;
import it.pagopa.selfcare.external_api.web.model.user.UserInfoResource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserInfoResourceMapper {

    @Mapping(source = "user.name.value", target = "user.name")
    @Mapping(source = "user.email.value", target = "user.email")
    @Mapping(source = "user.familyName.value", target = "user.surname")
    UserInfoResource toResource(UserInfoWrapper userInfo);
}
