package org.keycloak.spi.authenticator.email;

import com.google.common.collect.Lists;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.spi.authenticator.base.BaseAuthenticatorFactory;
import org.keycloak.spi.authenticator.enums.LoginTypeEnum;

import java.util.List;

/**
 * The type Email authenticator factory.
 *
 * @author yaoguoh
 */
public class EmailAuthenticatorFactory extends BaseAuthenticatorFactory {

    public static final String PROPERTY_FORM_EMAIL                = "form.email";
    public static final String PROPERTY_FORM_CODE                 = "form.code";
    public static final String PROPERTY_EMS_REQUEST_URL           = "ems.request.url";
    public static final String PROPERTY_EMS_REQUEST_CONTENT_TYPE  = "ems.request.content-type";
    public static final String PROPERTY_EMS_REQUEST_PARAM_DEFAULT = "ems.request.param.default";
    public static final String PROPERTY_EMS_REQUEST_PARAM_EMAIL   = "ems.request.param.email";
    public static final String PROPERTY_EMS_REQUEST_PARAM_CODE    = "ems.request.param.code";
    public static final String PROPERTY_EMS_RESPONSE_CHECK_KEY    = "ems.response.check.key";
    public static final String PROPERTY_EMS_RESPONSE_CHECK_VALUE  = "ems.response.check.value";

    @Override
    protected LoginTypeEnum getLoginType() {
        return LoginTypeEnum.EMAIL;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        EmailAuthenticator authenticator = new EmailAuthenticator();
        authenticator.setLoginType(this.getLoginType());
        authenticator.setSession(session);
        return authenticator;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        final List<ProviderConfigProperty> propertyList = Lists.newArrayList();
        // 登陆表单
        propertyList.add(new ProviderConfigProperty(
                PROPERTY_FORM_EMAIL, "Login Form Key [email]", "登录表单键名称[邮箱]", ProviderConfigProperty.STRING_TYPE, LoginTypeEnum.EMAIL.getCode()
        ));
        propertyList.add(new ProviderConfigProperty(
                PROPERTY_FORM_CODE, "Login Form Key [code]", "登陆表单键名称[验证码]", ProviderConfigProperty.STRING_TYPE, "code"
        ));
        // 邮件服务接口
        propertyList.add(new ProviderConfigProperty(
                PROPERTY_EMS_REQUEST_URL, "Ems Request Url", "EMS校验接口地址[请求方式需为POST]", ProviderConfigProperty.STRING_TYPE, "https://"
        ));
        propertyList.add(new ProviderConfigProperty(
                PROPERTY_EMS_REQUEST_CONTENT_TYPE, "Ems Request Content-Type", "EMS校验接口请求内容类型", ProviderConfigProperty.LIST_TYPE, "application/json",
                "application/json", "x-www-form-urlencoded"
        ));
        // 邮件校验接口请求参数
        propertyList.add(new ProviderConfigProperty(
                PROPERTY_EMS_REQUEST_PARAM_DEFAULT, "Ems Request Default Param", "EMS校验默认参数", ProviderConfigProperty.STRING_TYPE, "{}"
        ));
        propertyList.add(new ProviderConfigProperty(
                PROPERTY_EMS_REQUEST_PARAM_EMAIL, "Ems Request Param [email]", "EMS校验邮箱参数名", ProviderConfigProperty.STRING_TYPE, "email"
        ));
        propertyList.add(new ProviderConfigProperty(
                PROPERTY_EMS_REQUEST_PARAM_CODE, "Ems Request Param [code]", "EMS校验验证码参数名", ProviderConfigProperty.STRING_TYPE, "code"
        ));
        // 邮件校验接口响应结果
        propertyList.add(new ProviderConfigProperty(
                PROPERTY_EMS_RESPONSE_CHECK_KEY, "Ems Response Check [key]", "EMS校验结果确认键", ProviderConfigProperty.STRING_TYPE, "email"
        ));
        propertyList.add(new ProviderConfigProperty(
                PROPERTY_EMS_RESPONSE_CHECK_VALUE, "Ems Response Check [value]", "EMS校验结果确认值", ProviderConfigProperty.STRING_TYPE, "true"
        ));

        return propertyList;
    }
}
