package it.pagopa.selfcare.external_api.web.model.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.external_api.model.user.RelationshipState;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Set;

@Data
public class SearchUserDto {

    @ApiModelProperty(value = "${swagger.external_api.user.model.fiscalCode}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String fiscalCode;

    @ApiModelProperty(value = "${swagger.external_api.user.model.statuses}")
    private List<RelationshipState> statuses;

}
