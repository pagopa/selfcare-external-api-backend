locals {
  prefix            = "selc"
  env_short         = "d"
  env               = "dev"
  domain            = "external-api"
  github_org        = "pagopa"
  github_repository = "selfcare-external-api-backend"

  tags = {
    CreatedBy   = "Terraform"
    Environment = "Dev"
    Owner       = "SelfCare"
    Source      = "https://github.com/pagopa/selfcare-external-api-backend"
    CostCenter  = "TS310 - PAGAMENTI & SERVIZI"
  }

  ci_github_federations = [
    { repository = local.github_repository, subject = local.env_short == "d" ? "dev-ci" : "${local.env}-ci" }
  ]
  cd_github_federations = [
    { repository = local.github_repository, subject = local.env_short == "d" ? "dev-cd" : "${local.env}-cd" }
  ]

  environment_ci_roles = {
    subscription_roles = ["Reader"]
    resource_groups    = { "selc-d-identity-rg" = ["Owner"] }
  }
  environment_cd_roles = {
    subscription_roles = ["Contributor"]
    resource_groups    = { "selc-d-identity-rg" = ["Owner"] }
  }

  env_ci_secrets = {
    "ARM_CLIENT_ID" = module.identity_setup.identity_ci_client_id
  }

  env_ci_vars = {
    "ARM_SUBSCRIPTION_ID" = data.azurerm_client_config.current.subscription_id
    "ARM_TENANT_ID"       = data.azurerm_client_config.current.tenant_id
  }

  env_cd_secrets = {
    "ARM_CLIENT_ID" = module.identity_setup.identity_cd_client_id
  }

  env_cd_vars = {
    "ARM_SUBSCRIPTION_ID" = data.azurerm_client_config.current.subscription_id
    "ARM_TENANT_ID"       = data.azurerm_client_config.current.tenant_id
  }
}
