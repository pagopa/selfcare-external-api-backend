package it.pagopa.selfcare.external_api.model.token;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.base.utils.Env;
import lombok.Data;

@Data
public class LegalsResource {

    private String partyId;
    private String relationshipId;
    private PartyRole role;
    private Env env;
}
