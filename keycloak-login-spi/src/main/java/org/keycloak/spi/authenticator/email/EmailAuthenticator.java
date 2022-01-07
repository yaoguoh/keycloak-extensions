package org.keycloak.spi.authenticator.email;

import cn.hutool.json.JSONUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.UserModel;
import org.keycloak.spi.authenticator.base.BaseAuthenticator;
import org.keycloak.spi.authenticator.enums.AuthenticationErrorEnum;
import org.keycloak.spi.authenticator.exception.AuthenticationException;

import java.io.IOException;
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
            throw new AuthenticationException(AuthenticationErrorEnum.PARAM_NOT_CHECKED_ERROR, "邮箱不能为空");
        }
        if (StringUtils.isEmpty(code)) {
            throw new AuthenticationException(AuthenticationErrorEnum.PARAM_NOT_CHECKED_ERROR, "验证码不能为空");
        }
        // 使用邮箱查询用户
        final UserModel userModel = context.getSession().userStorageManager().getUserByEmail(context.getRealm(), email);
        try {
            this.doEmailAuthenticate(context, email, code);
            context.setUser(userModel);
            context.success();
        } catch (Exception e) {
            throw new AuthenticationException(AuthenticationErrorEnum.CODE_INVALID_ERROR, ExceptionUtils.getStackTrace(e));
        }
    }

    private void doEmailAuthenticate(AuthenticationFlowContext context, String phone, String code) throws IOException {
        final String requestUrl         = super.getPropertyValue(context, EmailAuthenticatorFactory.PROPERTY_EMS_REQUEST_URL);
        final String requestContentType = super.getPropertyValue(context, EmailAuthenticatorFactory.PROPERTY_EMS_REQUEST_CONTENT_TYPE);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final Map<String, Object> requestParam = this.buildRequestParam(context, phone, code);
            // 发送校验请求
            final HttpResponse response = super.codeAuthenticateExecute(httpClient, requestUrl, requestContentType, requestParam);
            // 校验返回结果
            final String checkKey   = super.getPropertyValue(context, EmailAuthenticatorFactory.PROPERTY_EMS_RESPONSE_CHECK_KEY);
            final String checkValue = super.getPropertyValue(context, EmailAuthenticatorFactory.PROPERTY_EMS_RESPONSE_CHECK_VALUE);
            super.codeAuthenticateValidate(response, checkKey, checkValue);
        }
    }

    private Map<String, Object> buildRequestParam(AuthenticationFlowContext context, String phone, String code) {
        final String requestParamDefault = super.getPropertyValue(context, EmailAuthenticatorFactory.PROPERTY_EMS_REQUEST_PARAM_DEFAULT);
        final String requestParamEmail   = super.getPropertyValue(context, EmailAuthenticatorFactory.PROPERTY_EMS_REQUEST_PARAM_EMAIL);
        final String requestParamCode    = super.getPropertyValue(context, EmailAuthenticatorFactory.PROPERTY_EMS_REQUEST_PARAM_CODE);

        final Map<String, Object> requestParam = JSONUtil.parseObj(requestParamDefault);
        requestParam.put(requestParamEmail, phone);
        requestParam.put(requestParamCode, code);

        return requestParam;
    }
}
