package org.keycloak.spi.authenticator.email;

import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.UserModel;
import org.keycloak.spi.authenticator.base.BaseAuthenticator;
import org.keycloak.spi.authenticator.enums.AuthenticationErrorEnum;
import org.keycloak.spi.authenticator.exception.AuthenticationException;

import java.util.Map;

/**
 * The type Email authenticator.
 *
 * @author yaoguoh
 */
public class EmailAuthenticator extends BaseAuthenticator {

    @Override
    protected void doAuthenticate(AuthenticationFlowContext context) {
        final String email = super.getRequestParameter(context, EmailAuthenticatorFactory.PROPERTY_FORM_EMAIL);
        final String code  = super.getRequestParameter(context, EmailAuthenticatorFactory.PROPERTY_FORM_CODE);

        // 参数校验
        if (StringUtils.isEmpty(email)) {
            throw new AuthenticationException(AuthenticationErrorEnum.PARAM_NOT_CHECKED_ERROR, "Email must be filled!");
        }
        if (StringUtils.isEmpty(code)) {
            throw new AuthenticationException(AuthenticationErrorEnum.PARAM_NOT_CHECKED_ERROR, "Verification code must be filled!");
        }
        // 使用邮箱查询用户
        UserModel userModel = context.getSession().users().getUserByEmail(context.getRealm(), email);
        this.doEmailAuthenticate(context, email, code);
        context.setUser(userModel);
        context.success();
    }

    private void doEmailAuthenticate(AuthenticationFlowContext context, String phone, String code) {
        String              requestUrl   = super.getPropertyValue(context, EmailAuthenticatorFactory.PROPERTY_EMS_REQUEST_URL);
        String              contentType  = super.getPropertyValue(context, EmailAuthenticatorFactory.PROPERTY_EMS_REQUEST_CONTENT_TYPE);
        Map<String, Object> requestParam = this.buildRequestParam(context, phone, code);
        // 发送校验请求
        HttpResponse response = super.codeAuthenticateExecute(requestUrl, contentType, requestParam);
        // 校验返回结果
        String checkKey   = super.getPropertyValue(context, EmailAuthenticatorFactory.PROPERTY_EMS_RESPONSE_CHECK_KEY);
        String checkValue = super.getPropertyValue(context, EmailAuthenticatorFactory.PROPERTY_EMS_RESPONSE_CHECK_VALUE);
        super.codeAuthenticateValidate(response, checkKey, checkValue);
    }

    private Map<String, Object> buildRequestParam(AuthenticationFlowContext context, String phone, String code) {
        String requestParamDefault = super.getPropertyValue(context, EmailAuthenticatorFactory.PROPERTY_EMS_REQUEST_PARAM_DEFAULT);
        String requestParamEmail   = super.getPropertyValue(context, EmailAuthenticatorFactory.PROPERTY_EMS_REQUEST_PARAM_EMAIL);
        String requestParamCode    = super.getPropertyValue(context, EmailAuthenticatorFactory.PROPERTY_EMS_REQUEST_PARAM_CODE);

        Map<String, Object> requestParam = JSONUtil.parseObj(requestParamDefault);
        requestParam.put(requestParamEmail, phone);
        requestParam.put(requestParamCode, code);

        return requestParam;
    }
}
