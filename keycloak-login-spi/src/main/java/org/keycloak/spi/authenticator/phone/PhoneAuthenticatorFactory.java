package org.keycloak.spi.authenticator.phone;

import com.google.common.collect.Lists;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.spi.authenticator.base.BaseAuthenticatorFactory;
import org.keycloak.spi.authenticator.enums.LoginTypeEnum;

import java.util.List;

/**
 * The type Phone authenticator factory.
 *
 * @author yaoguoh
 */
public class PhoneAuthenticatorFactory extends BaseAuthenticatorFactory {

    public static final String PROPERTY_USER_ATTRIBUTE_PHONE      = "property_user_attribute_phone";
    public static final String PROPERTY_FORM_PHONE                = "property_form_phone";
    public static final String PROPERTY_FORM_CODE                 = "property_form_code";
    public static final String PROPERTY_SMS_REQUEST_URL           = "property_sms_request_url";
    public static final String PROPERTY_SMS_REQUEST_CONTENT_TYPE  = "property_sms_request_content-type";
    public static final String PROPERTY_SMS_REQUEST_PARAM_DEFAULT = "property_sms_request_param_default";
    public static final String PROPERTY_SMS_REQUEST_PARAM_PHONE   = "property_sms_request_param_phone";
    public static final String PROPERTY_SMS_REQUEST_PARAM_CODE    = "property_sms_request_param_code";
    public static final String PROPERTY_SMS_RESPONSE_CHECK_KEY    = "property_sms_response_check_key";
    public static final String PROPERTY_SMS_RESPONSE_CHECK_VALUE  = "property_sms_response_check_value";

    @Override
    protected LoginTypeEnum getLoginType() {
        return LoginTypeEnum.PHONE;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        org.keycloak.spi.authenticator.phone.PhoneAuthenticator authenticator = new PhoneAuthenticator();
        authenticator.setLoginType(this.getLoginType());
        authenticator.setSession(session);
        return authenticator;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        List<ProviderConfigProperty> propertyList = Lists.newArrayList();
        // 用户属性
        propertyList.add(new ProviderConfigProperty(
                PROPERTY_USER_ATTRIBUTE_PHONE, "User attribute key [phone]", "用户属性键名称[手机号]", ProviderConfigProperty.STRING_TYPE, LoginTypeEnum.PHONE.getCode()
        ));
        // 登陆表单
        propertyList.add(new ProviderConfigProperty(
                PROPERTY_FORM_PHONE, "Login form key [phone]", "登录表单键名称[手机号]", ProviderConfigProperty.STRING_TYPE, "phone"
        ));
        propertyList.add(new ProviderConfigProperty(
                PROPERTY_FORM_CODE, "Login form key [code]", "登陆表单键名称[验证码]", ProviderConfigProperty.STRING_TYPE, "code"
        ));
        // 短信服务接口
        propertyList.add(new ProviderConfigProperty(
                PROPERTY_SMS_REQUEST_URL, "Sms Request Url", "SMS校验接口地址[请求方式需为POST]", ProviderConfigProperty.STRING_TYPE, "https://"
        ));
        // 短信校验接口请求参数
        propertyList.add(new ProviderConfigProperty(
                PROPERTY_SMS_REQUEST_PARAM_DEFAULT, "Sms request default param", "SMS校验默认参数", ProviderConfigProperty.STRING_TYPE, "{}"
        ));
        propertyList.add(new ProviderConfigProperty(
                PROPERTY_SMS_REQUEST_CONTENT_TYPE, "Sms request default param", "SMS校验接口请求内容类型", ProviderConfigProperty.LIST_TYPE,
                "application/json", "application/json", "x-www-form-urlencoded"
        ));
        propertyList.add(new ProviderConfigProperty(
                PROPERTY_SMS_REQUEST_PARAM_PHONE, "Sms request param [phone]", "SMS校验手机号参数名", ProviderConfigProperty.STRING_TYPE, "phone"
        ));
        propertyList.add(new ProviderConfigProperty(
                PROPERTY_SMS_REQUEST_PARAM_CODE, "Sms request param [code]", "SMS校验验证码参数名", ProviderConfigProperty.STRING_TYPE, "code"
        ));
        // 短信校验接口响应结果
        propertyList.add(new ProviderConfigProperty(
                PROPERTY_SMS_RESPONSE_CHECK_KEY, "Sms response check [key]", "SMS校验结果确认键", ProviderConfigProperty.STRING_TYPE, "result"
        ));
        propertyList.add(new ProviderConfigProperty(
                PROPERTY_SMS_RESPONSE_CHECK_VALUE, "Sms response check [value]", "SMS校验结果确认值", ProviderConfigProperty.STRING_TYPE, "true"
        ));

        return propertyList;
    }
}
