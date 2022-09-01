package org.keycloak.spi.authenticator.wechat;

import com.google.common.collect.Lists;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.spi.authenticator.base.BaseAuthenticatorFactory;
import org.keycloak.spi.authenticator.enums.LoginTypeEnum;

import java.util.List;

/**
 * The type Wechat authenticator factory.
 *
 * @author yaoguoh
 */
public class WechatAuthenticatorFactory extends BaseAuthenticatorFactory {

    public static final String PROPERTY_USER_ATTRIBUTE_WECHAT_UNIONID = "property_user_attribute_wechat_unionid";
    public static final String PROPERTY_FORM_WECHAT_UNIONID           = "property_form_wechat_unionid";

    @Override
    protected LoginTypeEnum getLoginType() {
        return LoginTypeEnum.WECHAT;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        org.keycloak.spi.authenticator.wechat.WechatAuthenticator authenticator = new WechatAuthenticator();
        authenticator.setLoginType(this.getLoginType());
        authenticator.setSession(session);
        return authenticator;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        List<ProviderConfigProperty> propertyList = Lists.newArrayList();
        // 用户属性
        propertyList.add(new ProviderConfigProperty(
                PROPERTY_USER_ATTRIBUTE_WECHAT_UNIONID, "User attribute key [wechat unionid]", "用户属性键名称[微信unionid]", ProviderConfigProperty.STRING_TYPE, "wechat_unionid"
        ));
        // 登陆表单
        propertyList.add(new ProviderConfigProperty(
                PROPERTY_FORM_WECHAT_UNIONID, "Login form key [wechat unionid]", "登录表单键名称[微信unionid]", ProviderConfigProperty.STRING_TYPE, "wechat_unionid"
        ));

        return propertyList;
    }
}
