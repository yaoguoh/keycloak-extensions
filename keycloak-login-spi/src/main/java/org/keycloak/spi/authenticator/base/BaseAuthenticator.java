package org.keycloak.spi.authenticator.base;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.client.ClientAuthUtil;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.spi.authenticator.enums.AuthenticationErrorEnum;
import org.keycloak.spi.authenticator.enums.LoginTypeEnum;
import org.keycloak.spi.authenticator.exception.AuthenticationException;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * The type Base authenticator.
 *
 * @author yaoguoh
 */
@Setter
@Slf4j
public abstract class BaseAuthenticator implements Authenticator {

    /**
     * The constant PROPERTY_LOGIN_TYPE.
     */
    public static final String PROPERTY_LOGIN_TYPE = "login_type";

    /**
     * The Session.
     */
    protected KeycloakSession session;
    /**
     * The Login type.
     */
    protected LoginTypeEnum   loginType;

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        try {
            MultivaluedMap<String, String> formParameters = context.getHttpRequest().getDecodedFormParameters();
            log.debug("Authentication flow form parameters {}", formParameters);
            String loginTypeCode = formParameters.getFirst(PROPERTY_LOGIN_TYPE);
            // 参数校验
            if (StringUtils.isBlank(loginTypeCode)) {
                throw new AuthenticationException(AuthenticationErrorEnum.PARAM_NOT_CHECKED_ERROR, "login_type can not be empty!");
            }
            if (!LoginTypeEnum.containsCode(loginTypeCode)) {
                throw new AuthenticationException(AuthenticationErrorEnum.PARAM_NOT_CHECKED_ERROR, "unsupported login_type [" + loginTypeCode + "]");
            }
            if (loginType.getCode().equals(loginTypeCode)) {
                doAuthenticate(context);
            } else {
                log.debug("Login type code [{}] not equals {}", loginType.getCode(), loginTypeCode);
                context.attempted();
            }
        } catch (AuthenticationException e) {
            AuthenticationErrorEnum error = e.getError();
            log.error(e.getLocalizedMessage(), error);
            Response challengeResponse =
                    ClientAuthUtil.errorResponse(error.getStatus(), error.getError(), String.format(error.getErrorDescription(), e.getArgs()));
            context.failure(error.getFlowError(), challengeResponse);
        }
    }

    /**
     * Do authenticate.
     *
     * @param context the context
     */
    protected abstract void doAuthenticate(AuthenticationFlowContext context);

    /**
     * Code authenticate execute http response.
     *
     * @param requestUrl   the request url
     * @param contentType  the content type
     * @param requestParam the request param
     * @return the http response
     */
    protected HttpResponse codeAuthenticateExecute(String requestUrl, String contentType, Map<String, Object> requestParam) {
        HttpRequest httpRequest = HttpRequest
                .post(requestUrl)
                .contentType(contentType);
        // 判断`content-type`处理请求参数
        if (StringUtils.equals(contentType, ContentType.FORM_URLENCODED.getValue())) {
            requestParam.forEach(httpRequest::form);
        } else {
            httpRequest.body(JSONUtil.toJsonStr(requestParam));
        }
        log.debug("HttpPost url [{}] content-type [{}] params [{}]", requestUrl, contentType, requestParam);
        return httpRequest.execute();
    }

    /**
     * Code authenticate validate.
     *
     * @param response   the response
     * @param checkKey   the check key
     * @param checkValue the check value
     */
    protected void codeAuthenticateValidate(HttpResponse response, String checkKey, String checkValue) {
        if (!response.isOk()) {
            throw new AuthenticationException(AuthenticationErrorEnum.CODE_INVALID_ERROR, response.body());
        }
        JSONObject body = JSONUtil.parseObj(response.body());
        log.debug("Response body [{}]", body);
        if (!StringUtils.equals(checkValue, body.getStr(checkKey))) {
            throw new AuthenticationException(AuthenticationErrorEnum.CODE_INVALID_ERROR);
        }
    }

    /**
     * Gets request parameter.
     *
     * @param context the context
     * @param param   the param
     * @return the string
     */
    protected String getRequestParameter(AuthenticationFlowContext context, String param) {
        MultivaluedMap<String, String> formParameters = context.getHttpRequest().getDecodedFormParameters();
        return formParameters.getFirst(this.getPropertyValue(context, param));
    }

    /**
     * Gets property value.
     *
     * @param context the context
     * @param key     the key
     * @return the property value
     */
    protected String getPropertyValue(AuthenticationFlowContext context, String key) {
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        if (config == null) {
            throw new AuthenticationException(AuthenticationErrorEnum.CONFIG_UNINITIALIZED_ERROR, loginType.getCode() + " login authenticator");
        }
        return config.getConfig().get(key);
    }

    /**
     * Validate user user model.
     *
     * @param key       identifier name
     * @param value     identifier value
     * @param userModel the user model
     * @return the user model
     */
    protected UserModel validateUser(String key, String value, UserModel userModel) {
        if (ObjectUtils.isEmpty(userModel)) {
            throw new AuthenticationException(AuthenticationErrorEnum.USER_NOT_FOUND_ERROR, key, value);
        }
        if (!userModel.isEnabled()) {
            throw new AuthenticationException(AuthenticationErrorEnum.USER_DISABLED_ERROR, key, value);
        }
        return userModel;
    }

    @Override
    public void action(AuthenticationFlowContext context) {
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public void close() {
    }
}
