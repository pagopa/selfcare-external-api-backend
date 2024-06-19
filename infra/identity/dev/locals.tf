locals {
  env       = "dev"
  env_short = "d"
  domain    = "external-api-backend"
  prefix    = "selc"
  location  = "West Europe"
  repo_name = "selfcare-external-api-backend"

  tags = {
    CreatedBy   = "Terraform"
    Environment = "Dev"
    Owner       = "SelfCare"
    Source      = "https://github.com/pagopa/selfcare-external-api-backend"
    CostCenter  = "TS310 - PAGAMENTI & SERVIZI"
  }
}