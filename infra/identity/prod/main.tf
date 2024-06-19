module "federated_identities" {
  source = "../modules/federated_identities"

  env_short = local.env_short
  env       = local.env
  domain    = local.domain
  prefix    = local.prefix
  location  = local.location

  tags = local.tags
}