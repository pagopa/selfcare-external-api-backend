data "azurerm_key_vault" "key_vault" {
  resource_group_name = var.key_vault.resource_group_name
  name                = var.key_vault.name
}

data "azurerm_key_vault_secret" "apim_product_pn_sk" {
  name         = "apim-product-pn-sk"
  key_vault_id = data.azurerm_key_vault.key_vault.id
}

data "azurerm_key_vault" "key_vault_pnpg" {
  resource_group_name = var.key_vault_pnpg.resource_group_name
  name                = var.key_vault_pnpg.name
}

data "azurerm_key_vault_secret" "apim_product_pnpg_sk" {
  name         = "external-api-key"
  key_vault_id = data.azurerm_key_vault.key_vault_pnpg.id
}

data "github_repository" "repo" {
  full_name = var.repo_full_name
}

# resource definitions
resource "github_repository_environment" "repo_environment" {
  repository  = data.github_repository.repo.name
  environment = "${var.env}-ci"
}

resource "github_actions_environment_secret" "integration_environment" {
  repository      = data.github_repository.repo.name
  environment     = github_repository_environment.repo_environment.environment
  secret_name     = "integration_environment${var.pnpg_suffix}"
  plaintext_value = base64encode(
    templatefile("${path.module}/Selfcare-external-Integration.postman_environment.json",
      {
        env                  = var.env_url
        apimKeyPN            = data.azurerm_key_vault_secret.apim_product_pn_sk.value
        apimKeyDataVaultPNPG = data.azurerm_key_vault_secret.apim_product_pnpg_sk.value
      })
  )
}

resource "github_actions_environment_secret" "integration_environment_bruno" {
  repository      = data.github_repository.repo.name
  environment     = github_repository_environment.repo_environment.environment
  secret_name     = "integration_environment_bruno${var.pnpg_suffix}"
  plaintext_value = base64encode(
    templatefile("${path.module}/Selfcare-External-Integration-Environment.bru",
      {
        env                  = var.env_url
        apimKeyPN            = data.azurerm_key_vault_secret.apim_product_pn_sk.value
        apimKeyDataVaultPNPG = data.azurerm_key_vault_secret.apim_product_pnpg_sk.value
      })
  )
}
