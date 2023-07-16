package it.pagopa.selfcare.external_api.web.model.mapper;

import it.pagopa.selfcare.external_api.model.user.UserInfoWrapper;
import it.pagopa.selfcare.external_api.web.model.user.UserInfoResource;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserInfoResourceMapper {

    UserInfoResource toResource(UserInfoWrapper userInfo);
}
