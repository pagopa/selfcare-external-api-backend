package it.pagopa.selfcare.external_api.connector.rest.mapper;


import it.pagopa.selfcare.external_api.model.user.User;
import it.pagopa.selfcare.external_api.model.user.UserInstitutions;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.UserDetailResponse;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.UserInstitutionResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserInstitutions toUserInstitutionsFromUserInstitutionResponse(UserInstitutionResponse userInstitutionResponse);

    User toUserFromUserDetailResponse(UserDetailResponse onboardingData);
}
