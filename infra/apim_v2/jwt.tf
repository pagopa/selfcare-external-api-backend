resource "pkcs12_from_pem" "jwt_pkcs12" {
  password        = ""
  cert_pem        = data.azurerm_key_vault_secret.jwt_certificate_data_pem.value
  private_key_pem = data.azurerm_key_vault_secret.jwt_private_key_pem.value
}

resource "azurerm_api_management_certificate" "jwt_certificate" {
  name                = "jwt-spid-crt"
  api_management_name = module.apim.name
  resource_group_name = azurerm_resource_group.rg_api.name
  data                = pkcs12_from_pem.jwt_pkcs12.result
}
