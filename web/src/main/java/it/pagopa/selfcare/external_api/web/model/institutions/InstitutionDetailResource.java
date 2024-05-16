package it.pagopa.selfcare.external_api.web.model.institutions;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class InstitutionDetailResource {

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.id}")
    private UUID id;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.name}")
    private String description;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.externalId}")
    private String externalId;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.originId}")
    private String originId;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.institutionType}")
    private InstitutionType institutionType;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.digitalAddress}")
    private String digitalAddress;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.address}")
    private String address;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.zipCode}")
    private String zipCode;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.taxCode}")
    private String taxCode;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.taxCodeInvoicing}")
    private String taxCodeInvoicing;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.origin}")
    private String origin;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.geographicTaxonomy}")
    private List<GeographicTaxonomyResource> geographicTaxonomies;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.rea}")
    private String rea;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.shareCapital}")
    private String shareCapital;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.businessRegisterPlace}")
    private String businessRegisterPlace;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.supportEmail}")
    private String supportEmail;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.supportPhone}")
    private String supportPhone;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.imported}")
    private Boolean imported;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.subUnitCode}")
    private String subunitCode;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.subUnitType}")
    private String subunitType;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.parentDescription}")
    private String parentDescription;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.aooParentCode}")
    private String aooParentCode;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.country}")
    private String country;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.county}")
    private String county;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.city}")
    private String city;
}
