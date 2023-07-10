package it.pagopa.selfcare.external_api.model.documents;

import lombok.Data;

@Data
public class ResourceResponse {
    private byte[] data;
    private String fileName;
    private String mimetype;
}
