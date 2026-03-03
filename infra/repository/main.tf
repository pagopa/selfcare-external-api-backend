terraform {
  required_version = ">= 1.6.0"

  backend "azurerm" {
    resource_group_name  = "terraform-state-rg"
    storage_account_name = "tfinfprodselfcare"
    container_name       = "terraform-state"
    key                  = "selfcare-external-api-backend.repository.tfstate"
  }

  required_providers {
    github = {
      source  = "integrations/github"
      version = "~> 6.0"
    }
    azurerm = {
      source  = "hashicorp/azurerm"
      version = ">= 3.110, < 4.0"
    }
  }
}

provider "azurerm" {
  features {}
  skip_provider_registration = true
}

provider "github" {
  owner = locals.github.org
}

module "repository" {
  source = "github.com/pagopa/selfcare-commons//infra/terraform-modules/github_repository_settings?ref=main"

  github = {
    org        = locals.github.org
    repository = locals.github.repository
  }

  environments = {
    dev-ci = {
      reviewers_teams          = var.github_repository_environment_ci.reviewers_teams
      wait_timer               = var.github_repository_environment_ci.wait_timer
      can_admins_bypass        = var.github_repository_environment_ci.can_admins_bypass
      deployment_branch_policy = var.github_repository_environment_ci.deployment_branch_policy
    }
    dev-cd = {
      reviewers_teams          = var.github_repository_environment_cd.reviewers_teams
      wait_timer               = var.github_repository_environment_cd.wait_timer
      can_admins_bypass        = var.github_repository_environment_cd.can_admins_bypass
      deployment_branch_policy = var.github_repository_environment_cd.deployment_branch_policy
    }
  }
}
