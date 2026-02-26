terraform {
  required_version = ">=1.6.0"

  required_providers {
    azuread = {
      source  = "hashicorp/azuread"
      version = "2.30.0"
    }
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "<= 3.86.0"
    }
    github = {
      source  = "integrations/github"
      version = "~> 6.0"
    }
  }

  backend "azurerm" {
    resource_group_name  = "terraform-state-rg"
    storage_account_name = "tfappdevselfcare"
    container_name       = "terraform-state"
    key                  = "selfcare-pnpg-external-api-backend.integration-test.tfstate"
  }
}

provider "azurerm" {
  features {}
  skip_provider_registration = true
}

provider "github" {
  owner = "pagopa"
}