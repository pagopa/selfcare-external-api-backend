is_pnpg   = true
env_short = "u"

tags = {
  CreatedBy   = "Terraform"
  Environment = "Uat"
  Owner       = "SelfCare"
  Source      = "https://github.com/pagopa/selfcare-external-api-backend"
  CostCenter  = "TS310 - PAGAMENTI & SERVIZI"
}

container_app = {
  min_replicas = 1
  max_replicas = 2
  scale_rules  = []
  cpu          = 0.5
  memory       = "1Gi"
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
    value = "DEBUG"
  },
  {
    name  = "MS_ONBOARDING_URL"
    value = "https://selc-u-pnpg-onboarding-ms-ca.calmforest-ffe47bf1.westeurope.azurecontainerapps.io"
  },
  {
    name  = "MS_CORE_URL"
    value = "https://selc-u-pnpg-ms-core-ca.calmforest-ffe47bf1.westeurope.azurecontainerapps.io"
  },
  {
    name  = "USERVICE_PARTY_REGISTRY_PROXY_URL"
    value = "https://selc-u-pnpg-party-reg-proxy-ca.calmforest-ffe47bf1.westeurope.azurecontainerapps.io"
  },
  {
    name  = "USERVICE_PARTY_PROCESS_URL"
    value = "https://selc-u-pnpg-ms-core-ca.calmforest-ffe47bf1.westeurope.azurecontainerapps.io"
  },
  {
    name  = "MS_PRODUCT_URL"
    value = "https://selc-u-pnpg-product-ca.calmforest-ffe47bf1.westeurope.azurecontainerapps.io"
  },
  {
    name  = "USERVICE_USER_REGISTRY_URL"
    value = "https://api.uat.pdv.pagopa.it/user-registry/v1"
  },
  {
    name  = "USERVICE_PARTY_MANAGEMENT_URL"
    value = "https://selc-u-pnpg-ms-core-ca.calmforest-ffe47bf1.westeurope.azurecontainerapps.io"
  },
  {
    name  = "SELFCARE_USER_URL"
    value = "https://selc-u-pnpg-user-ms-ca.calmforest-ffe47bf1.westeurope.azurecontainerapps.io"
  }
]

secrets_names = {
  "APPLICATIONINSIGHTS_CONNECTION_STRING" = "appinsights-connection-string"
  "BLOB_STORAGE_CONN_STRING"              = "web-storage-connection-string"
  "USERVICE_USER_REGISTRY_API_KEY"        = "user-registry-api-key"
  "JWT_TOKEN_PUBLIC_KEY"                  = "jwt-public-key"
}
