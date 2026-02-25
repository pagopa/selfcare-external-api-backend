locals {
  is_pnpg          = false
  env_short        = "u"
  suffix_increment = "-002"
  cae_name         = "cae-002"
  prefix           = "selc"
  env              = "uat"
  location         = "westeurope"

  repo_full_name = "pagopa/selfcare-external-api-backend"

  pnpg_suffix                    = local.is_pnpg ? "-pnpg" : ""
  project                        = "selc-${local.env_short}"
  env_url                        = local.env_short == "p" ? "" : ".${local.env}"
  container_app_environment_name = "${local.project}${local.pnpg_suffix}-${local.cae_name}"
  ca_resource_group_name         = "${local.project}-container-app${local.suffix_increment}-rg"
  monitor_rg_name                = "${local.project}-monitor-rg"
  monitor_appinsights_name       = "${local.project}-appinsights"

  tags = {
    CreatedBy   = "Terraform"
    Environment = "Uat"
    Owner       = "SelfCare"
    Source      = "https://github.com/pagopa/selfcare-external-api-backend"
    CostCenter  = "TS310 - PAGAMENTI & SERVIZI"
  }

  key_vault = {
    resource_group_name = "selc-u-sec-rg"
    name                = "selc-u-kv"
  }

  key_vault_pnpg = {
    resource_group_name = "selc-u-pnpg-sec-rg"
    name                = "selc-u-pnpg-kv"
  }
}
