output "identity_ci_client_id" {
  value       = module.identity_setup.identity_ci_client_id
  description = "Client ID of the CI Managed Identity"
}

output "identity_cd_client_id" {
  value       = module.identity_setup.identity_cd_client_id
  description = "Client ID of the CD Managed Identity"
}
