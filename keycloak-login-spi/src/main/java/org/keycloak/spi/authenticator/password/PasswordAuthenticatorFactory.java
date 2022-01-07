package org.keycloak.spi.authenticator.password;

import com.google.common.collect.Lists;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.spi.authenticator.base.BaseAuthenticatorFactory;
import org.keycloak.spi.authenticator.enums.LoginTypeEnum;

import java.util.List;

/**
 * The type Password authenticator factory.
 *
 * @author yaoguoh
 */
public class PasswordAuthenticatorFactory extends BaseAuthenticatorFactory {

    public static final String PROPERTY_FORM_USERNAME = "form.username";
    public static final String PROPERTY_FORM_PASSWORD = "form.password";

    @Override
    protected LoginTypeEnum getLoginType() {
        return LoginTypeEnum.PASSWORD;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        PasswordAuthenticator authenticator = new PasswordAuthenticator();
        authenticator.setLoginType(this.getLoginType());
        authenticator.setSession(session);
        return authenticator;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        List<ProviderConfigProperty> propertyList = Lists.newArrayList();
        // 登陆表单
        propertyList.add(new ProviderConfigProperty(
                PROPERTY_FORM_USERNAME, "Login Form Key [username]", "登录表单键名称[用户名]", ProviderConfigProperty.STRING_TYPE, "username"
        ));
        propertyList.add(new ProviderConfigProperty(
                PROPERTY_FORM_PASSWORD, "Login Form Key [password]", "登陆表单键名称[密码]", ProviderConfigProperty.STRING_TYPE, "password"
        ));

        return propertyList;
    }
}
