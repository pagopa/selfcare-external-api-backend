locals {
  env       = "uat"
  env_short = "u"
  domain    = "external-api-backend"
  prefix    = "selc"
  location  = "West Europe"
  repo_name = "selfcare-external-api-backend"

  tags = {
    CreatedBy   = "Terraform"
    Environment = "Uat"
    Owner       = "SelfCare"
    Source      = "https://github.com/pagopa/selfcare-external-api-backend"
    CostCenter  = "TS310 - PAGAMENTI & SERVIZI"
  }
}