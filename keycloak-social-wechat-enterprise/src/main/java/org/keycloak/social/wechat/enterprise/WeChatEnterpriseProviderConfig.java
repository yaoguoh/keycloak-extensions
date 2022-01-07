package org.keycloak.social.wechat.enterprise;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;


/**
 * The type We chat enterprise provider config.
 *
 * @author yaoguoh
 */
public class WeChatEnterpriseProviderConfig extends OAuth2IdentityProviderConfig {

    /**
     * Instantiates a new We chat enterprise provider config.
     */
    public WeChatEnterpriseProviderConfig() {
    }

    /**
     * Instantiates a new We chat enterprise provider config.
     *
     * @param model the model
     */
    public WeChatEnterpriseProviderConfig(IdentityProviderModel model) {
        super(model);
    }

    /**
     * Gets corp id.
     *
     * @return the corp id
     */
    public String getCorpId() {
        return getConfig().get("corpId");
    }

    /**
     * Sets corp id.
     *
     * @param corpId the corp id
     */
    public void setCorpId(String corpId) {
        getConfig().put("corpId", corpId);
    }

    /**
     * Gets qrcode authorization url.
     *
     * @return the qrcode authorization url
     */
    public String getQrcodeAuthorizationUrl() {
        return getConfig().get("qrcodeAuthorizationUrl");
    }

    /**
     * Sets qrcode authorization url.
     *
     * @param qrcodeAuthorizationUrl the qrcode authorization url
     */
    public void setQrcodeAuthorizationUrl(String qrcodeAuthorizationUrl) {
        getConfig().put("qrcodeAuthorizationUrl", qrcodeAuthorizationUrl);
    }
}
