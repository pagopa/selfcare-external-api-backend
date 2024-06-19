terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.108.0"
    }
  }

  backend "azurerm" {
    resource_group_name  = "terraform-state-rg"
    storage_account_name = "tfappuatselfcare"
    container_name       = "terraform-state"
    key                  = "selfcare-external-api-backend.identity.tfstate"
  }
}

provider "azurerm" {
  features {
  }
}
