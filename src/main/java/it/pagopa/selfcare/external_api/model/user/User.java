package it.pagopa.selfcare.external_api.model.user;

import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.util.Map;

@Data
@FieldNameConstants(asEnum = true)
public class User {

    @FieldNameConstants.Exclude
    private String id;
    private String fiscalCode;
    private String lastActiveOnboardingUserEmail;
    private CertifiedField<String> name;
    private CertifiedField<String> familyName;
    private CertifiedField<String> email;
    private Map<String, WorkContact> workContacts;


    public WorkContact getWorkContact(String key) {
        return workContacts != null ? workContacts.get(key) : null;
    }

}