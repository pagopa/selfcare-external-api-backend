package it.pagopa.selfcare.external_api.web.model.mapper;

import it.pagopa.selfcare.external_api.model.documents.ResourceResponse;
import it.pagopa.selfcare.external_api.web.model.document.ContractResource;

public class ContractMapper {

    public static ContractResource toResource(ResourceResponse model){
        ContractResource resource = null;
        if (model != null){
            resource = new ContractResource();
            resource.setData(model.getData());
        }
        return resource;
    }
}
