package it.pagopa.selfcare.external_api.web.model.institutions;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.external_api.model.onboarding.InstitutionType;
import lombok.Data;

import java.util.Collection;
import java.util.UUID;

@Data
public class InstitutionResource {

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

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.status}")
    private String status;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.address}")
    private String address;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.zipCode}")
    private String zipCode;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.taxCode}")
    private String taxCode;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.origin}")
    private String origin;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.productRoles}")
    private Collection<String> userProductRoles;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.recipientCode}")
    private String recipientCode;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.companyInformations}")
    private CompanyInformationsResource companyInformations;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.assistance}")
    private AssistanceContactsResource assistanceContacts;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.pspData}")
    private PspDataResource pspData;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.pspData.dpoData}")
    private DpoDataResource dpoData;


    @ApiModelProperty(value = "${swagger.external_api.institutions.model.subUnitCode}")
    private String subunitCode;
    @ApiModelProperty(value = "${swagger.external_api.institutions.model.subUnitType}")
    private String subunitType;
    @ApiModelProperty(value = "${swagger.external_api.institutions.model.parentDescription}")
    private String parentDescription;
    @ApiModelProperty(value = "${swagger.external_api.institutions.model.aooParentCode}")
    private String aooParentCode;
}
