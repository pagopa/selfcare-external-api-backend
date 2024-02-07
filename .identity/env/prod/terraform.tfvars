prefix    = "selc"
env       = "prod"
env_short = "p"
domain    = "b4f-external-api"

cd_github_federations = [
  {
    repository = "selfcare-external-api-backend"
    subject    = "prod-cd"
  }
]

environment_cd_roles = {
  subscription    = ["Contributor"]
  resource_groups = {
    "terraform-state-rg" = [
      "Storage Blob Data Contributor"
    ]
  }
}
