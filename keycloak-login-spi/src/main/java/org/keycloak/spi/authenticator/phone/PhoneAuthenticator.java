package org.keycloak.spi.authenticator.phone;

import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.UserModel;
import org.keycloak.spi.authenticator.base.BaseAuthenticator;
import org.keycloak.spi.authenticator.enums.AuthenticationErrorEnum;
import org.keycloak.spi.authenticator.exception.AuthenticationException;

import java.util.Map;
import java.util.Optional;

/**
 * The type Phone authenticator.
 *
 * @author yaoguoh
 */
public class PhoneAuthenticator extends BaseAuthenticator {

    @Override
    public void doAuthenticate(AuthenticationFlowContext context) {
        final String phone = super.getRequestParameter(context, PhoneAuthenticatorFactory.PROPERTY_FORM_PHONE);
        final String code  = super.getRequestParameter(context, PhoneAuthenticatorFactory.PROPERTY_FORM_CODE);
        // 参数校验
        if (StringUtils.isEmpty(phone)) {
            throw new AuthenticationException(AuthenticationErrorEnum.PARAM_NOT_CHECKED_ERROR, "Phone must be filled!");
        }
        if (StringUtils.isEmpty(code)) {
            throw new AuthenticationException(AuthenticationErrorEnum.PARAM_NOT_CHECKED_ERROR, "Verification code must be filled!");
        }
        // 使用手机号查询用户
        Optional<UserModel> optional = context.getSession().users()
                .searchForUserByUserAttributeStream(
                        context.getRealm(),
                        super.getPropertyValue(context, PhoneAuthenticatorFactory.PROPERTY_USER_ATTRIBUTE_PHONE),
                        phone)
                .findFirst();
        UserModel userModel = super.validateUser("phone", phone, optional.orElse(null));
        this.doSmsAuthenticate(context, phone, code);
        context.setUser(userModel);
        context.success();
    }

    private void doSmsAuthenticate(AuthenticationFlowContext context, String phone, String code) {
        String              requestUrl   = super.getPropertyValue(context, PhoneAuthenticatorFactory.PROPERTY_SMS_REQUEST_URL);
        String              contentType  = super.getPropertyValue(context, PhoneAuthenticatorFactory.PROPERTY_SMS_REQUEST_CONTENT_TYPE);
        Map<String, Object> requestParam = this.buildRequestParam(context, phone, code);
        // 发送校验请求
        HttpResponse response = super.codeAuthenticateExecute(requestUrl, contentType, requestParam);
        // 校验返回结果
        String checkKey   = super.getPropertyValue(context, PhoneAuthenticatorFactory.PROPERTY_SMS_RESPONSE_CHECK_KEY);
        String checkValue = super.getPropertyValue(context, PhoneAuthenticatorFactory.PROPERTY_SMS_RESPONSE_CHECK_VALUE);
        super.codeAuthenticateValidate(response, checkKey, checkValue);
    }

    private Map<String, Object> buildRequestParam(AuthenticationFlowContext context, String phone, String code) {
        String requestParamDefault = super.getPropertyValue(context, PhoneAuthenticatorFactory.PROPERTY_SMS_REQUEST_PARAM_DEFAULT);
        String requestParamPhone   = super.getPropertyValue(context, PhoneAuthenticatorFactory.PROPERTY_SMS_REQUEST_PARAM_PHONE);
        String requestParamCode    = super.getPropertyValue(context, PhoneAuthenticatorFactory.PROPERTY_SMS_REQUEST_PARAM_CODE);

        Map<String, Object> requestParam = JSONUtil.parseObj(requestParamDefault);
        requestParam.put(requestParamPhone, phone);
        requestParam.put(requestParamCode, code);

        return requestParam;
    }
}
