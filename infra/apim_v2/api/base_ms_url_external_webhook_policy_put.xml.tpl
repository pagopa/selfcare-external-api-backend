<policies>
    <inbound>
    <base/>

    <set-header name="Authorization" exists-action="override">
        <value>@((string)context.Variables["jwt"])</value>
    </set-header>

    <rewrite-uri template="@{
        string productId = (string)context.Variables["productId"];
        return $"/webhooks/{productId}";
    }" />
    <set-body template="none">@{
        var body = context.Request.Body.As<JObject>(preserveContent: true);
        body["productId"] = (string)context.Variables["productId"];
        return body.ToString();
    }</set-body>
    <trace source="WEBHOOK" severity="information">
        <message>WEBHOOK GET</message>
        <metadata name="Path" value="@((string)context.Request.Url.Path)" />
    </trace>

    <set-backend-service base-url="${MS_BACKEND_URL}"/>
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
