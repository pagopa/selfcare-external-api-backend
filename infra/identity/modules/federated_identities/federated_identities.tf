module "federated_identities" {
  source = "github.com/pagopa/dx//infra/modules/azure_federated_identity_with_github?ref=main"

  prefix    = var.prefix
  env_short = var.env_short
  env       = var.env
  domain    = var.domain

  repositories = [var.repo_name]

  continuos_integration = {
    enable = true
    roles = {
      subscription = [
        "Reader",
        "Reader and Data Access",
        "PagoPA IaC Reader"
      ]
      resource_groups = {}
    }
  }

  continuos_delivery = {
    enable = false
    roles = {
      subscription = [
        "Contributor"
      ]
      resource_groups = {}
    }
  }

  tags = var.tags
}
