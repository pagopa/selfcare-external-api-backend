/*
 * Party Process Micro Service
 * This service is the party process
 *
 * OpenAPI spec version: {{version}}
 * Contact: support@example.com
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package it.pagopa.selfcare.external_api.web.model.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
public class UserResource {

    @ApiModelProperty(value = "${swagger.external_api.user.model.id}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private UUID id;

    @ApiModelProperty(value = "${swagger.external_api.user.model.name}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String name;

    @ApiModelProperty(value = "${swagger.external_api.user.model.surname}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String surname;

    @ApiModelProperty(value = "${swagger.external_api.user.model.institutionalEmail}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    @Email
    private String email;

    @ApiModelProperty(value = "${swagger.external_api.user.model.productRoles}", required = true)
    @JsonProperty(required = true)
    @NotEmpty
    private List<String> roles;

}
