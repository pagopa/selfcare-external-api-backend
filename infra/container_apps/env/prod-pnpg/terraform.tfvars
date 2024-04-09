is_pnpg   = true
env_short = "p"

tags = {
  CreatedBy   = "Terraform"
  Environment = "Prod"
  Owner       = "SelfCare"
  Source      = "https://github.com/pagopa/selfcare-external-api-backend"
  CostCenter  = "TS310 - PAGAMENTI & SERVIZI"
}

container_app = {
  min_replicas = 1
  max_replicas = 5
  scale_rules = [
    {
      custom = {
        metadata = {
          "desiredReplicas" = "3"
          "start"           = "0 8 * * MON-FRI"
          "end"             = "0 19 * * MON-FRI"
          "timezone"        = "Europe/Rome"
        }
        type = "cron"
      }
      name = "cron-scale-rule"
    }
  ]
  cpu    = 1.25
  memory = "2.5Gi"
}

app_settings = [

  {
    name  = "APPLICATIONINSIGHTS_ROLE_NAME"
    value = "external-api"
  },
  {
    name  = "ALLOWED_SERVICE_TYPES"
    value = "onboarding-interceptor,external-interceptor"
  },
  {
    name  = "REST_CLIENT_READ_TIMEOUT"
    value = "60000"
  },
  {
    name  = "REST_CLIENT_CONNECT_TIMEOUT"
    value = "60000"
  },
  {
    name  = "JAVA_TOOL_OPTIONS"
    value = "-javaagent:applicationinsights-agent.jar"
  },
  {
    name  = "APPLICATIONINSIGHTS_INSTRUMENTATION_LOGGING_LEVEL"
    value = "OFF"
  },
  {
    name  = "EXTERNAL_API_LOG_LEVEL"
    value = "INFO"
  },
  {
    name  = "MS_ONBOARDING_URL"
    value = "https://selc-p-pnpg-onboarding-ms-ca.redbeach-5da5166f.westeurope.azurecontainerapps.io"
  },
  {
    name  = "MS_CORE_URL"
    value = "https://selc-p-pnpg-ms-core-ca.redbeach-5da5166f.westeurope.azurecontainerapps.io"
  },
  {
    name  = "USERVICE_PARTY_REGISTRY_PROXY_URL"
    value = "https://selc-p-pnpg-party-reg-proxy-ca.redbeach-5da5166f.westeurope.azurecontainerapps.io"
  },
  {
    name  = "USERVICE_PARTY_PROCESS_URL"
    value = "https://selc-p-pnpg-ms-core-ca.redbeach-5da5166f.westeurope.azurecontainerapps.io"
  },
  {
    name  = "MS_PRODUCT_URL"
    value = "https://selc-p-pnpg-product-ca.redbeach-5da5166f.westeurope.azurecontainerapps.io"
  },
  {
    name  = "USERVICE_USER_REGISTRY_URL"
    value = "https://api.pdv.pagopa.it/user-registry/v1"
  },
  {
    name  = "USERVICE_PARTY_MANAGEMENT_URL"
    value = "https://selc-p-pnpg-ms-core-ca.redbeach-5da5166f.westeurope.azurecontainerapps.io"
  },
  {
    name  = "SELFCARE_USER_URL"
    value = "https://selc-p-pnpg-user-ms-ca.redbeach-5da5166f.westeurope.azurecontainerapps.io"
  }
]

secrets_names = {
  "APPLICATIONINSIGHTS_CONNECTION_STRING" = "appinsights-connection-string"
  "BLOB_STORAGE_CONN_STRING"              = "web-storage-connection-string"
  "USERVICE_USER_REGISTRY_API_KEY"        = "user-registry-api-key"
  "JWT_TOKEN_PUBLIC_KEY"                  = "jwt-public-key"
}