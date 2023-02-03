package it.pagopa.selfcare.external_api.web.model.mapper;

import it.pagopa.selfcare.external_api.model.onboarding.GeographicTaxonomy;
import it.pagopa.selfcare.external_api.web.model.onboarding.GeographicTaxonomyDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GeographicTaxonomyMapper {

    public static GeographicTaxonomy fromDto(GeographicTaxonomyDto model) {
        GeographicTaxonomy resource = null;
        if (model != null) {
            resource = new GeographicTaxonomy();
            resource.setCode(model.getCode());
            resource.setDesc(model.getDesc());
        }
        return resource;
    }

}
