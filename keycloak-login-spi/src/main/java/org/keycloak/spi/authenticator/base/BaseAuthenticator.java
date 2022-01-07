package org.keycloak.spi.authenticator.base;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Setter;
import lombok.extern.jbosslog.JBossLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.OAuth2ErrorRepresentation;
import org.keycloak.spi.authenticator.enums.AuthenticationErrorEnum;
import org.keycloak.spi.authenticator.enums.LoginTypeEnum;
import org.keycloak.spi.authenticator.exception.AuthenticationException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The type Base authenticator.
 *
 * @author yaoguoh
 */
@Setter
@JBossLog
public abstract class BaseAuthenticator implements Authenticator {

    public static final String          PROPERTY_LOGIN_TYPE = "login_type";
    protected           KeycloakSession session;
    protected           LoginTypeEnum   loginType;

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        try {
            MultivaluedMap<String, String> formParameters = context.getHttpRequest().getDecodedFormParameters();
            String                         loginTypeCode  = formParameters.getFirst(PROPERTY_LOGIN_TYPE);
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
                context.attempted();
            }
        } catch (AuthenticationException e) {
            Response challengeResponse = this.getErrorResponse(e);
            context.failure(e.getError().getFlowError(), challengeResponse);
        } catch (Exception e) {
            Response challengeResponse = this.getErrorResponse(AuthenticationErrorEnum.SYSTEM_ERROR, ExceptionUtils.getStackTrace(e));
            context.failure(AuthenticationErrorEnum.SYSTEM_ERROR.getFlowError(), challengeResponse);
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
     * @param httpClient         the http client
     * @param requestUrl         the request url
     * @param requestContentType the request content type
     * @param requestParam       the request param
     * @return http response
     * @throws IOException the io exception
     */
    protected HttpResponse codeAuthenticateExecute(CloseableHttpClient httpClient, String requestUrl,
                                                   String requestContentType, Map<String, Object> requestParam) throws IOException {
        final HttpPost httpPost = new HttpPost(requestUrl);
        // 判断`content-type`处理请求参数
        httpPost.addHeader(HttpHeaders.CONTENT_TYPE, requestContentType);
        if (StringUtils.equals(requestContentType, ContentType.APPLICATION_FORM_URLENCODED.getMimeType())) {
            final List<NameValuePair> nameValuePairList = new ArrayList<>();
            requestParam.forEach((key, value) -> nameValuePairList.add(new BasicNameValuePair(key, value.toString())));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairList, Consts.UTF_8));
        } else {
            httpPost.setEntity(new StringEntity(JSONUtil.toJsonStr(requestParam)));
        }
        log.debugf("HttpPost url [%s] content-type [%s] params [%s]", requestUrl, requestContentType, requestParam);
        return httpClient.execute(httpPost);
    }

    /**
     * Code authenticate validate.
     *
     * @param response   the response
     * @param checkKey   the check key
     * @param checkValue the check value
     * @throws IOException the io exception
     */
    protected void codeAuthenticateValidate(HttpResponse response, String checkKey, String checkValue) throws IOException {
        if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
            throw new AuthenticationException(AuthenticationErrorEnum.CODE_INVALID_ERROR, String.valueOf(response.getStatusLine().getStatusCode()));
        }
        final JSONObject responseData = JSONUtil.parseObj(EntityUtils.toString(response.getEntity()));
        log.debugf("HttpResponse Entity [%s]", responseData);
        if (!StringUtils.equals(checkValue, responseData.getStr(checkKey))) {
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
     * Gets error response.
     *
     * @param e the e
     * @return the error response
     */
    protected Response getErrorResponse(AuthenticationException e) {
        return getErrorResponse(e.getError(), e.getArgs());
    }

    /**
     * Gets error response.
     *
     * @param error the error
     * @param args  the args
     * @return the error response
     */
    protected Response getErrorResponse(AuthenticationErrorEnum error, String... args) {
        String message = args.length > 0 ? String.format(error.getFormatMessage(), (Object[]) args) : error.getDefaultMessage();
        return getErrorResponse(error.getHttpStatus(), error.getCode(), message);
    }

    /**
     * Gets error response.
     *
     * @param status           the status
     * @param error            the error
     * @param errorDescription the error description
     * @return the error response
     */
    protected Response getErrorResponse(int status, String error, String errorDescription) {
        OAuth2ErrorRepresentation errorRep = new OAuth2ErrorRepresentation(error, errorDescription);
        return Response.status(status).entity(errorRep).type(MediaType.APPLICATION_JSON_TYPE).build();
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
