package com.bembosevents.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.mercadopago")
public class MercadoPagoProperties {

    private boolean enabled;
    private String baseUrl = "https://api.mercadopago.com";
    private String accessToken = "";
    private String webhookSecret = "";
    private String returnBaseUrl = "";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getWebhookSecret() { return webhookSecret; }
    public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }
    public String getReturnBaseUrl() { return returnBaseUrl; }
    public void setReturnBaseUrl(String returnBaseUrl) { this.returnBaseUrl = returnBaseUrl; }

    public boolean hasAccessToken() {
        return accessToken != null && !accessToken.isBlank();
    }

    public boolean hasWebhookSecret() {
        return webhookSecret != null && !webhookSecret.isBlank();
    }

    public boolean hasReturnBaseUrl() {
        return returnBaseUrl != null && !returnBaseUrl.isBlank();
    }
}