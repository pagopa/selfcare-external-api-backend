module "identity_ci" {
  source             = "github.com/pagopa/terraform-azurerm-v4//github_federated_identity?ref=v7.26.5"
  prefix             = var.prefix
  env_short          = var.env_short
  domain             = var.domain
  identity_role      = "ci"
  github_federations = var.ci_github_federations
  ci_rbac_roles      = var.environment_ci_roles
  tags               = var.tags
}

module "identity_cd" {
  source             = "github.com/pagopa/terraform-azurerm-v4//github_federated_identity?ref=v7.26.5"
  prefix             = var.prefix
  env_short          = var.env_short
  domain             = var.domain
  identity_role      = "cd"
  github_federations = var.cd_github_federations
  cd_rbac_roles      = var.environment_cd_roles
  tags               = var.tags
}

resource "azurerm_key_vault_access_policy" "kv_ci" {
  key_vault_id            = var.key_vault_id
  tenant_id               = var.tenant_id
  object_id               = module.identity_ci.identity_principal_id
  secret_permissions      = ["Get", "List"]
  certificate_permissions = ["Get", "List"]
}

resource "azurerm_key_vault_access_policy" "kv_cd" {
  key_vault_id            = var.key_vault_id
  tenant_id               = var.tenant_id
  object_id               = module.identity_cd.identity_principal_id
  secret_permissions      = ["Get", "List", "Set"]
  certificate_permissions = ["Get", "List"]
}
