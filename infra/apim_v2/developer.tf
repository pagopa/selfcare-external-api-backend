locals {
  resource_groups_name   = azurerm_resource_group.rg_api.name
  service_name           = module.apim.name
  azure_apim_api_version = "2021-08-01"
  checkout_cdn_name      = "${local.project}-checkout-cdn-endpoint"
}

resource "null_resource" "download_apim_external_api_v2" {
  triggers = {
    file_sha1 = filesha1("./api/ms_external_api/v2/openapi.${var.env}.json")
  }

  depends_on = [module.apim_external_api_ms_v2]

  provisioner "local-exec" {
    command = <<EOT
      mkdir -p "${path.module}/.terraform/tmp/env/${var.env}/developer/external"
      az rest \
        --method get \
        --url https://management.azure.com/subscriptions/${data.azurerm_subscription.current.subscription_id}/resourceGroups/${module.apim.resource_group_name}/providers/Microsoft.ApiManagement/service/${module.apim.name}/apis/${module.apim_external_api_ms_v2.name} \
        --url-parameters api-version=${local.azure_apim_api_version} export=true format=openapi \
 	      --output-file ${path.module}/.terraform/tmp/env/${var.env}/developer/external/ms-external-api-v2.yaml
    EOT
  }
}

resource "null_resource" "upload_developer_external_api_v2" {
  triggers = {
    file_sha1 = filesha1("./api/ms_external_api/v2/openapi.${var.env}.json")
  }

  depends_on = [null_resource.download_apim_external_api_v2]

  provisioner "local-exec" {
    command = <<EOT
              az storage blob upload \
                --container '$web' \
                --account-name ${replace(replace(local.checkout_cdn_name, "-cdn-endpoint", "-sa"), "-", "")} \
                --account-key ${data.azurerm_storage_account.checkout.primary_access_key} \
                --file "${path.module}/.terraform/tmp/env/${var.env}/developer/external/ms-external-api-v2.yaml" \
                --overwrite true \
                --name 'developer/external/v2/ms-external-api.yaml' &&
              az cdn endpoint purge \
                --resource-group ${data.azurerm_storage_account.checkout.resource_group_name} \
                --name ${local.checkout_cdn_name} \
                --profile-name ${replace(local.checkout_cdn_name, "-cdn-endpoint", "-cdn-profile")}  \
                --content-paths "/developer/external/v2/ms-external-api.yaml" \
                --no-wait
          EOT
  }
}

resource "null_resource" "upload_developer_index_v2" {
  triggers = {
    file_sha1 = filesha1("./env/${var.env}/developer/external/v2/index.html")
  }

  provisioner "local-exec" {
    command = <<EOT
              az storage blob upload \
                --container '$web' \
                --account-name ${replace(replace(local.checkout_cdn_name, "-cdn-endpoint", "-sa"), "-", "")} \
                --account-key ${data.azurerm_storage_account.checkout.primary_access_key} \
                --file "./env/${var.env}/developer/external/v2/index.html" \
                --overwrite true \
                --name 'developer/external/v2/index.html' &&
              az cdn endpoint purge \
                --resource-group ${data.azurerm_storage_account.checkout.resource_group_name} \
                --name ${local.checkout_cdn_name} \
                --profile-name ${replace(local.checkout_cdn_name, "-cdn-endpoint", "-cdn-profile")}  \
                --content-paths "/developer/external/v2/index.html" \
                --no-wait
          EOT
  }
}
