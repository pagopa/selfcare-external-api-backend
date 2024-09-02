<policies>
  <inbound>
    <base/>

      <choose>
      <when condition="@(((string)context.Variables["productId"]).Contains("prod-fd"))">
        <validate-jwt header-name="Authorization" failed-validation-httpcode="401" failed-validation-error-message="Unauthorized" require-expiration-time="false" require-scheme="Bearer" require-signed-tokens="true">
          <openid-config url="https://login.microsoftonline.com/${TENANT_ID}/.well-known/openid-configuration" />
          <required-claims>
            <claim name="aud" match="all">
              <value>${EXTERNAL-OAUTH2-ISSUER}</value>
            </claim>
          </required-claims>
        </validate-jwt>
      </when>
      </choose>

  <set-header name="Authorization" exists-action="override">
    <value>@((string)context.Variables["jwt"])</value>
  </set-header>

  <set-query-parameter name="productId" exists-action="override">
    <value>@((string)context.Variables["productId"])</value>
  </set-query-parameter>

    <set-backend-service base-url="${MS_BACKEND_URL}" />
  </inbound>
  <backend>
    <base/>
  </backend>
  <outbound>
    <base/>
  </outbound>
  <on-error>
    <base/>
  </on-error>
</policies>
