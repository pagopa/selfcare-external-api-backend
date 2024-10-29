<policies>
    <inbound>
        <base/>
        <choose>
            <when condition="@(((string)context.Product.Id).Contains(" prod-fd"))">
                <validate-jwt header-name="Authorization" failed-validation-httpcode="401"
                              failed-validation-error-message="Unauthorized" require-expiration-time="false"
                              require-scheme="Bearer" require-signed-tokens="true">
                    <openid-config url="https://login.microsoftonline.com/${TENANT_ID}/.well-known/openid-configuration"/>
                    <required-claims>
                        <claim name="aud" match="all">
                            <value>${EXTERNAL-OAUTH2-ISSUER}</value>
                        </claim>
                    </required-claims>
                </validate-jwt>
            </when>
        </choose>
        <set-variable name="fnkey" value="${FN_KEY}"/>
        <set-header name="X-FUNCTIONS-KEY" exists-action="override">
            <value>@((string)context.Variables["fnkey"])</value>
        </set-header>
        <set-backend-service base-url="${BACKEND_BASE_URL}"/>
        <rewrite-uri template="@("/api/acknowledgment/" + (string)context.Variables["productId"] + "/message/{messageId}/status/{status}")" />
    </inbound>
    <backend>
    <base/>
    </backend>
    <on-error>
    <base/>
    </on-error>
</policies>
