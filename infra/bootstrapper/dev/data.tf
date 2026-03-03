data "azurerm_client_config" "current" {}

data "azurerm_subscription" "current" {}

data "azurerm_key_vault" "key_vault" {
  name                = "${local.prefix}-${local.env_short}-kv"
  resource_group_name = "${local.prefix}-${local.env_short}-sec-rg"
}