package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.model.user.UserInfoWrapper;

public interface UserService {

    UserInfoWrapper getUserInfo(String fiscalCode);

}
