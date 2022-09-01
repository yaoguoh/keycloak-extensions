package org.keycloak.spi.authenticator.username;

import com.google.common.collect.Lists;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.spi.authenticator.base.BaseAuthenticatorFactory;
import org.keycloak.spi.authenticator.enums.LoginTypeEnum;

import java.util.List;

/**
 * The type Username authenticator factory.
 *
 * @author yaoguoh
 */
public class UsernameAuthenticatorFactory extends BaseAuthenticatorFactory {

    /**
     * The constant PROPERTY_FORM_USERNAME.
     */
    public static final String PROPERTY_FORM_USERNAME = "property_form_username";

    @Override
    protected LoginTypeEnum getLoginType() {
        return LoginTypeEnum.USERNAME;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        org.keycloak.spi.authenticator.username.UsernameAuthenticator authenticator = new UsernameAuthenticator();
        authenticator.setLoginType(this.getLoginType());
        authenticator.setSession(session);
        return authenticator;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        List<ProviderConfigProperty> propertyList = Lists.newArrayList();
        // 登陆表单
        propertyList.add(new ProviderConfigProperty(
                PROPERTY_FORM_USERNAME, "Login form key [username]", "登录表单键名称[用户名]", ProviderConfigProperty.STRING_TYPE, "username"
        ));
        return propertyList;
    }
}
