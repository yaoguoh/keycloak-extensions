package org.keycloak.social.wechat.enterprise;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;


public class WeChatEnterpriseProviderConfig extends OAuth2IdentityProviderConfig {

    public WeChatEnterpriseProviderConfig() {
    }

    public WeChatEnterpriseProviderConfig(IdentityProviderModel model) {
        super(model);
    }

    public String getCorpId() {
        return getConfig().get("corpId");
    }

    public void setCorpId(String corpId) {
        getConfig().put("corpId", corpId);
    }

    public String getQrcodeAuthorizationUrl() {
        return getConfig().get("qrcodeAuthorizationUrl");
    }

    public void setQrcodeAuthorizationUrl(String qrcodeAuthorizationUrl) {
        getConfig().put("qrcodeAuthorizationUrl", qrcodeAuthorizationUrl);
    }
}
