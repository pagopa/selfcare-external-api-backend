variable "is_pnpg" {
  type        = bool
  default     = false
  description = "(Optional) True if you want to apply changes to PNPG environment"
}

variable "prefix" {
  description = "Domain prefix"
  type        = string
  default     = "selc"
  validation {
    condition = (
      length(var.prefix) <= 6
    )
    error_message = "Max length is 6 chars."
  }
}

variable "location" {
  type        = string
  description = "One of westeurope, northeurope"
}

variable "env_short" {
  description = "Environment short name"
  type        = string
  validation {
    condition = (
      length(var.env_short) <= 1
    )
    error_message = "Max length is 1 chars."
  }
}

variable "cae_name" {
  type        = string
  description = "Container App Environment name"
  default     = "cae-cp"
}

variable "suffix_increment" {
  type        = string
  description = "Suffix increment Container App Environment name"
  default     = ""
}

variable "env" {
  description = "Environment name"
  type        = string
  validation {
    condition = (
      length(var.env) <= 4
    )
    error_message = "Max length is 4 chars."
  }
}

variable "tags" {
  type = map(any)
}

variable "key_vault" {
  description = "KeyVault data to get secrets values from"
  type = object({
    resource_group_name = string
    name                = string
  })
}

variable "key_vault_pnpg" {
  description = "PNPG KeyVault data to get secrets values from"
  type = object({
    resource_group_name = string
    name                = string
  })
}

variable "repo_full_name" {
  description = "(Optional) GitHub repository full name used by the module"
  type        = string
  default     = "pagopa/selfcare-external-api-backend"
}

variable "env_url" {
  description = "URL fragment for the chosen environment (e.g. .dev or empty)"
  type        = string
}

variable "pnpg_suffix" {
  description = "Suffix appended when is_pnpg is true (e.g. -pnpg)"
  type        = string
}
