package it.pagopa.selfcare.external_api.web.model.mapper;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.model.product.Product;
import it.pagopa.selfcare.external_api.web.model.products.ProductResource;
import it.pagopa.selfcare.external_api.web.model.products.ProductRoleInfo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.EnumMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductsMapper {

    public static ProductResource toResource(Product model){
        ProductResource resource = null;
        if(model != null){
            resource = new ProductResource();
            resource.setId(model.getId());
            resource.setDescription(model.getDescription());
            resource.setTitle(model.getTitle());
            resource.setUrlPublic(model.getUrlPublic());
            resource.setUrlBO(model.getUrlBO());
            resource.setContractTemplateVersion(model.getContractTemplateVersion());
            resource.setContractTemplatePath(model.getContractTemplatePath());
            resource.setContractTemplateUpdatedAt(model.getContractTemplateUpdatedAt());
            resource.setCreatedAt(model.getCreatedAt());
            resource.setDepictImageUrl(model.getDepictImageUrl());
            resource.setIdentityTokenAudience(model.getIdentityTokenAudience());
            resource.setLogo(model.getLogo());
            resource.setLogoBgColor(model.getLogoBgColor());
            resource.setParentId(model.getParentId());
            resource.setRoleManagementURL(model.getRoleManagementURL());
            resource.setRoleMappings(toRoleMappings(model.getRoleMappings()));
        }
        return resource;
    }

    public static EnumMap<PartyRole, ProductRoleInfo> toRoleMappings(EnumMap<PartyRole, it.pagopa.selfcare.external_api.model.product.ProductRoleInfo> roleMappings){
        EnumMap<PartyRole, ProductRoleInfo> result;
        if(roleMappings != null){
            result = new EnumMap<>(PartyRole.class);
            roleMappings.forEach((key, value) -> result.put(key, new ProductRoleInfo(value)));
        }
        else{
            result = null;
        }
        return result;
    }
}
