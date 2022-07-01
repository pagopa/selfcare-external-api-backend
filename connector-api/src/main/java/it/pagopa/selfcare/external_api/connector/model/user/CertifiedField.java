package it.pagopa.selfcare.external_api.connector.model.user;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CertifiedField<T> {

    private Certification certification;
    private T value;

}
