
module "integration_test" {
  source = "../../_modules/integration-test"

  is_pnpg          = local.is_pnpg
  prefix           = local.prefix
  location         = local.location
  env_short        = local.env_short
  cae_name         = local.cae_name
  suffix_increment = local.suffix_increment
  env              = local.env
  tags             = local.tags
  key_vault        = local.key_vault
  key_vault_pnpg   = local.key_vault_pnpg
  repo_full_name   = local.repo_full_name

  env_url     = local.env_url
  pnpg_suffix = local.pnpg_suffix
}

output "current_env" {
  value = local.env
}
