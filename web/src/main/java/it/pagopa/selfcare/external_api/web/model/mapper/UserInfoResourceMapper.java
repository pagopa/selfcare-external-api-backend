package it.pagopa.selfcare.external_api.web.model.mapper;

import it.pagopa.selfcare.external_api.model.user.ProductDetails;
import it.pagopa.selfcare.external_api.model.user.UserDetailsWrapper;
import it.pagopa.selfcare.external_api.model.user.UserInfoWrapper;
import it.pagopa.selfcare.external_api.model.user.UserInstitution;
import it.pagopa.selfcare.external_api.web.model.onboarding.OnboardedProductResource;
import it.pagopa.selfcare.external_api.web.model.user.UserDetailsResource;
import it.pagopa.selfcare.external_api.web.model.user.UserInfoResource;
import it.pagopa.selfcare.external_api.web.model.user.UserInstitutionResource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserInfoResourceMapper {

    @Mapping(source = "user.name.value", target = "user.name")
    @Mapping(source = "user.email.value", target = "user.email")
    @Mapping(source = "user.familyName.value", target = "user.surname")
    UserInfoResource toResource(UserInfoWrapper userInfo);

    @Mapping(source = "userId", target = "id")
    @Mapping(source = "productDetails", target = "onboardedProductDetails", qualifiedByName = "toOnboardedProductResource")
    UserDetailsResource toResource(UserDetailsWrapper userDetails);

    @Named("toOnboardedProductResource")
    OnboardedProductResource toOnboardedProductResource(ProductDetails productDetails);

    UserInstitutionResource toUserInstitutionResource(UserInstitution entity);
}
