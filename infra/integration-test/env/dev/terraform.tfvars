env_short        = "d"
suffix_increment = "-002"
cae_name         = "cae-002"
prefix           = "selc"
env              = "dev"
location         = "westeurope"

tags = {
  CreatedBy   = "Terraform"
  Environment = "Dev"
  Owner       = "SelfCare"
  Source      = "https://github.com/pagopa/selfcare-external-api-backend"
  CostCenter  = "TS310 - PAGAMENTI & SERVIZI"
}

key_vault = {
  resource_group_name = "selc-d-sec-rg"
  name                = "selc-d-kv"
}
