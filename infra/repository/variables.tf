variable "prefix" {
  type    = string
  default = "selc"
}

variable "env" {
  type = string
}

variable "env_short" {
  type = string
}

variable "tags" {
  type = map(any)
}

variable "github_repository_environment_ci" {
  type = object({
    protected_branches       = bool
    custom_branch_policies   = bool
    reviewers_teams          = list(string)
    can_admins_bypass        = bool
    wait_timer               = number
    deployment_branch_policy = any
  })
}

variable "github_repository_environment_cd" {
  type = object({
    protected_branches       = bool
    custom_branch_policies   = bool
    reviewers_teams          = list(string)
    can_admins_bypass        = bool
    wait_timer               = number
    deployment_branch_policy = any
  })
}
