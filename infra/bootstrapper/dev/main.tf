terraform {
  required_version = ">= 1.6.0"
  backend "azurerm" {
    resource_group_name  = "terraform-state-rg"
    storage_account_name = "tfappdevselfcare"
    container_name       = "terraform-state"
    key                  = "selfcare-external-api-backend.integration-test.tfstate"
  }

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "<= 4.27.0"
    }
    github = {
      source  = "integrations/github"
      version = "5.45.0"
    }
  }

}

provider "azurerm" {
  features {}
}
provider "github" {
  owner = local.github_org
}

resource "azurerm_resource_group" "identity_rg" {
  name     = "${local.prefix}-${local.env_short}-identity-rg"
  location = "westeurope"
  tags     = local.tags
}

module "identity_setup" {
  source                = "../_modules/identity-setup"
  prefix                = local.prefix
  env_short             = local.env_short
  domain                = local.domain
  tags                  = local.tags
  #identity_rg_id        = module.integration_test.identity_rg_id
  tenant_id             = data.azurerm_client_config.current.tenant_id
  key_vault_id          = data.azurerm_key_vault.key_vault.id
  ci_github_federations = local.ci_github_federations
  cd_github_federations = local.cd_github_federations
  environment_ci_roles  = local.environment_ci_roles
  environment_cd_roles  = local.environment_cd_roles
}

resource "github_repository_environment" "ci" {
  environment = "${local.env}-ci"
  repository  = local.github_repository
}

resource "github_repository_environment" "cd" {
  environment = "${local.env}-cd"
  repository  = local.github_repository
}

resource "github_actions_environment_secret" "ci_secrets" {
  for_each        = local.env_ci_secrets
  repository      = local.github_repository
  environment     = github_repository_environment.ci.environment
  secret_name     = each.key
  plaintext_value = each.value
}

resource "github_actions_environment_variable" "ci_vars" {
  for_each      = local.env_ci_vars
  repository    = local.github_repository
  environment   = github_repository_environment.ci.environment
  variable_name = each.key
  value         = each.value
}

resource "github_actions_environment_secret" "cd_secrets" {
  for_each        = local.env_cd_secrets
  repository      = local.github_repository
  environment     = github_repository_environment.cd.environment
  secret_name     = each.key
  plaintext_value = each.value
}

resource "github_actions_environment_variable" "cd_vars" {
  for_each      = local.env_cd_vars
  repository    = local.github_repository
  environment   = github_repository_environment.cd.environment
  variable_name = each.key
  value         = each.value
}
