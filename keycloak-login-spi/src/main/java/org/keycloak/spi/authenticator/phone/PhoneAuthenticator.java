package org.keycloak.spi.authenticator.phone;

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
import java.util.Optional;
import java.util.stream.Stream;

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
            throw new AuthenticationException(AuthenticationErrorEnum.PARAM_NOT_CHECKED_ERROR, "手机号不能为空!");
        }
        if (StringUtils.isEmpty(code)) {
            throw new AuthenticationException(AuthenticationErrorEnum.PARAM_NOT_CHECKED_ERROR, "验证码不能为空!");
        }
        // 使用手机号查询用户
        final Stream<UserModel> userModelStream = context.getSession().userStorageManager().searchForUserByUserAttributeStream(
                context.getRealm(),
                super.getPropertyValue(context, PhoneAuthenticatorFactory.PROPERTY_USER_ATTRIBUTE_PHONE),
                phone);
        final Optional<UserModel> optional = userModelStream.findFirst();
        final UserModel userModel = optional.orElseThrow(() -> {
            throw new AuthenticationException(AuthenticationErrorEnum.USER_NOT_FOUND_ERROR, "手机号", phone);
        });
        try {
            this.doSmsAuthenticate(context, phone, code);
        } catch (IOException e) {
            throw new AuthenticationException(AuthenticationErrorEnum.CODE_INVALID_ERROR, ExceptionUtils.getStackTrace(e));
        }
        context.setUser(userModel);
        context.success();
    }

    private void doSmsAuthenticate(AuthenticationFlowContext context, String phone, String code) throws IOException {
        final String requestUrl         = super.getPropertyValue(context, PhoneAuthenticatorFactory.PROPERTY_SMS_REQUEST_URL);
        final String requestContentType = super.getPropertyValue(context, PhoneAuthenticatorFactory.PROPERTY_SMS_REQUEST_CONTENT_TYPE);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final Map<String, Object> requestParam = this.buildRequestParam(context, phone, code);
            // 发送校验请求
            final HttpResponse response = super.codeAuthenticateExecute(httpClient, requestUrl, requestContentType, requestParam);
            // 校验返回结果
            final String checkKey   = super.getPropertyValue(context, PhoneAuthenticatorFactory.PROPERTY_SMS_RESPONSE_CHECK_KEY);
            final String checkValue = super.getPropertyValue(context, PhoneAuthenticatorFactory.PROPERTY_SMS_RESPONSE_CHECK_VALUE);
            super.codeAuthenticateValidate(response, checkKey, checkValue);
        }
    }

    private Map<String, Object> buildRequestParam(AuthenticationFlowContext context, String phone, String code) {
        final String requestParamDefault = super.getPropertyValue(context, PhoneAuthenticatorFactory.PROPERTY_SMS_REQUEST_PARAM_DEFAULT);
        final String requestParamPhone   = super.getPropertyValue(context, PhoneAuthenticatorFactory.PROPERTY_SMS_REQUEST_PARAM_PHONE);
        final String requestParamCode    = super.getPropertyValue(context, PhoneAuthenticatorFactory.PROPERTY_SMS_REQUEST_PARAM_CODE);

        final Map<String, Object> requestParam = JSONUtil.parseObj(requestParamDefault);
        requestParam.put(requestParamPhone, phone);
        requestParam.put(requestParamCode, code);

        return requestParam;
    }
}
