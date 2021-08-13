package org.keycloak.social.wechat.enterprise;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.OAuth2Constants;
import org.keycloak.OAuthErrorException;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.ErrorPage;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.utils.StringUtil;

import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public class WeChatEnterpriseIdentityProvider extends AbstractOAuth2IdentityProvider<WeChatEnterpriseProviderConfig>
        implements SocialIdentityProvider<WeChatEnterpriseProviderConfig> {

    /**
     * [获取下级企业的access_token](https://work.weixin.qq.com/api/doc/90000/90135/91039)
     */
    public static final String TOKEN_URL                = "https://qyapi.weixin.qq.com/cgi-bin/gettoken";
    /**
     * [构造网页授权链接](https://work.weixin.qq.com/api/doc/90000/90135/91857)
     */
    public static final String AUTHORIZATION_URL        = "https://open.weixin.qq.com/connect/oauth2/authorize";
    /**
     * [构造扫码登录链接](https://work.weixin.qq.com/api/doc/90000/90135/91019)
     */
    public static final String QRCODE_AUTHORIZATION_URL = "https://open.work.weixin.qq.com/wwopen/sso/qrConnect";
    /**
     * [获取访问用户身份](https://work.weixin.qq.com/api/doc/90000/90135/91707)
     */
    public static final String GET_USERINFO_URL         = "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo";
    /**
     * [读取成员详细信息](https://work.weixin.qq.com/api/doc/90000/90135/90196)
     */
    public static final String GET_DETAIL_USERINFO_URL  = "https://qyapi.weixin.qq.com/cgi-bin/user/get";

    public static final String CORP_ID               = "corpid";
    public static final String CORP_SECRET           = "corpsecret";
    public static final String APPID                 = "appid";
    public static final String AGENT_ID              = "agentid";
    public static final String ACCESS_TOKEN          = "access_token";
    public static final String DEFAULT_SCOPE         = "snsapi_base";
    public static final String REDIRECT_FRAGMENT     = "wechat_redirect";
    public static final String DEFAULT_RESPONSE_TYPE = "code";
    public static final String USER_AGENT            = "wxwork";

    public static final String PROFILE_NAME   = "name";
    public static final String PROFILE_EMAIL  = "email";
    public static final String PROFILE_MOBILE = "mobile";
    public static final String PROFILE_GENDER = "gender";
    public static final String PROFILE_STATUS = "status";
    public static final String PROFILE_ENABLE = "enable";
    public static final String PROFILE_USERID = "userid";

    public static final String                CATCH_ACCESS_TOKEN_KEY = "access_token_";
    public static final Cache<String, String> CACHE_ACCESS_TOKEN     = CacheBuilder.newBuilder()
            .initialCapacity(100)
            .expireAfterWrite(7000, TimeUnit.SECONDS)
            .build();

    public WeChatEnterpriseIdentityProvider(KeycloakSession session, WeChatEnterpriseProviderConfig config) {
        super(session, config);
        config.setTokenUrl(TOKEN_URL);
        config.setAuthorizationUrl(AUTHORIZATION_URL);
        config.setQrcodeAuthorizationUrl(QRCODE_AUTHORIZATION_URL);
    }

    @Override
    protected String getDefaultScopes() {
        return DEFAULT_SCOPE;
    }

    @Override
    protected boolean supportsExternalExchange() {
        return true;
    }

    @Override
    public Response performLogin(AuthenticationRequest request) {
        try {
            URI authorizationUrl = this.createAuthorizationUrl(request).build();
            logger.debugf("Authorization url [%s]", authorizationUrl.toString());
            return Response.seeOther(authorizationUrl).build();
        } catch (Exception e) {
            throw new IdentityBrokerException("Could not create authentication request.", e);
        }
    }

    @Override
    protected UriBuilder createAuthorizationUrl(AuthenticationRequest request) {
        String userAgent = request.getHttpRequest().getHttpHeaders().getHeaderString("user-agent").toLowerCase();
        if (userAgent.contains(USER_AGENT)) {
            return UriBuilder.fromUri(this.getConfig().getAuthorizationUrl())
                    .queryParam(APPID, this.getConfig().getCorpId())
                    .queryParam(OAUTH2_PARAMETER_REDIRECT_URI, request.getRedirectUri())
                    .queryParam(OAUTH2_PARAMETER_SCOPE, this.getConfig().getDefaultScope())
                    .queryParam(OAUTH2_PARAMETER_STATE, request.getState().getEncoded())
                    .queryParam(OAUTH2_PARAMETER_RESPONSE_TYPE, DEFAULT_RESPONSE_TYPE)
                    .fragment(REDIRECT_FRAGMENT);
        } else {
            return UriBuilder.fromUri(this.getConfig().getQrcodeAuthorizationUrl())
                    .queryParam(APPID, this.getConfig().getCorpId())
                    .queryParam(AGENT_ID, this.getConfig().getClientId())
                    .queryParam(OAUTH2_PARAMETER_REDIRECT_URI, request.getRedirectUri())
                    .queryParam(OAUTH2_PARAMETER_STATE, request.getState().getEncoded());
        }
    }

    @Override
    public BrokeredIdentityContext getFederatedIdentity(String authorizationCode) {
        String                  accessToken             = this.getAccessToken();
        JsonNode                profile                 = this.getProfile(authorizationCode, accessToken);
        BrokeredIdentityContext brokeredIdentityContext = this.extractIdentityFromProfile(null, profile);
        brokeredIdentityContext.getContextData().put(FEDERATED_ACCESS_TOKEN, accessToken);
        return brokeredIdentityContext;
    }

    private JsonNode getProfile(String authorizationCode, String accessToken) {
        // 获取访问用户身份
        try {
            JsonNode profile;
            profile = SimpleHttp.doGet(GET_USERINFO_URL, session)
                    .param(ACCESS_TOKEN, accessToken)
                    .param("code", authorizationCode)
                    .asJson();
            logger.debugf("WeChat enterprise userinfo [%s] ", profile);
            // 读取成员详细信息
            profile = SimpleHttp.doGet(GET_DETAIL_USERINFO_URL, session)
                    .param(ACCESS_TOKEN, accessToken)
                    .param(PROFILE_USERID, this.getJsonProperty(profile, "UserId"))
                    .asJson();
            logger.debugf("WeChat enterprise detail userinfo [%s]", profile);
            return profile;
        } catch (IOException e) {
            throw new IdentityBrokerException("Failed to get user profile.", e);
        }
    }

    private String getAccessToken() {
        try {
            String accessToken = CACHE_ACCESS_TOKEN.get(CATCH_ACCESS_TOKEN_KEY + this.getConfig().getCorpId(), () -> "");
            if (StringUtil.isBlank(accessToken)) {
                JsonNode jsonNode = SimpleHttp.doGet(TOKEN_URL, session)
                        .param(CORP_ID, this.getConfig().getCorpId())
                        .param(CORP_SECRET, this.getConfig().getClientSecret())
                        .asJson();
                accessToken = this.getJsonProperty(jsonNode, ACCESS_TOKEN);
                logger.debugf("Renew wechat enterprise access token [%s] ", jsonNode);
                CACHE_ACCESS_TOKEN.put(CATCH_ACCESS_TOKEN_KEY + this.getConfig().getCorpId(), accessToken);
            }
            return accessToken;
        } catch (Exception e) {
            throw new IdentityBrokerException("Failed to get wechat enterprise access token", e);
        }
    }

    @Override
    protected BrokeredIdentityContext extractIdentityFromProfile(EventBuilder event, JsonNode profile) {
        // profile: see https://work.weixin.qq.com/api/doc/90000/90135/90196
        BrokeredIdentityContext identityContext = new BrokeredIdentityContext((this.getJsonProperty(profile, PROFILE_USERID)));
        identityContext.setUsername(this.getJsonProperty(profile, PROFILE_USERID).toLowerCase());
        identityContext.setBrokerUserId(this.getJsonProperty(profile, PROFILE_USERID).toLowerCase());
        identityContext.setModelUsername(this.getJsonProperty(profile, PROFILE_USERID).toLowerCase());
        identityContext.setFirstName(this.getJsonProperty(profile, PROFILE_NAME));
        identityContext.setEmail(this.getJsonProperty(profile, PROFILE_EMAIL));
        // 成员UserID。对应管理端的帐号，企业内必须唯一。不区分大小写，长度为1~64个字节
        identityContext.setUserAttribute(PROFILE_USERID, this.getJsonProperty(profile, PROFILE_USERID));
        // 手机号码，第三方仅通讯录应用可获取；对于非第三方创建的成员，第三方通讯录应用也不可获取
        identityContext.setUserAttribute(PROFILE_MOBILE, this.getJsonProperty(profile, PROFILE_MOBILE));
        // 性别: 0=未定义，1=男性，2=女性
        identityContext.setUserAttribute(PROFILE_GENDER, this.getJsonProperty(profile, PROFILE_GENDER));
        // 激活状态: 1=已激活，2=已禁用，4=未激活，5=退出企业。
        // 已激活代表已激活企业微信或已关注微工作台（原企业号）。未激活代表既未激活企业微信又未关注微工作台（原企业号）。
        identityContext.setUserAttribute(PROFILE_STATUS, this.getJsonProperty(profile, PROFILE_STATUS));
        // 成员启用状态。1表示启用的成员，0表示被禁用。注意，服务商调用接口不会返回此字段
        identityContext.setUserAttribute(PROFILE_ENABLE, this.getJsonProperty(profile, PROFILE_ENABLE));

        identityContext.setIdpConfig(this.getConfig());
        identityContext.setIdp(this);
        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(identityContext, profile, this.getConfig().getAlias());
        return identityContext;
    }

    @Override
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, BrokeredIdentityContext context) {
        user.setSingleAttribute(PROFILE_MOBILE, context.getUserAttribute(PROFILE_MOBILE));
        user.setSingleAttribute(PROFILE_GENDER, context.getUserAttribute(PROFILE_GENDER));
        user.setSingleAttribute(PROFILE_STATUS, context.getUserAttribute(PROFILE_STATUS));
        user.setSingleAttribute(PROFILE_ENABLE, context.getUserAttribute(PROFILE_ENABLE));
        user.setSingleAttribute(PROFILE_USERID, context.getUserAttribute(PROFILE_USERID));

        user.setUsername(context.getUsername());
        user.setFirstName(context.getFirstName());
        user.setLastName(context.getLastName());
        user.setEmail(context.getEmail());
    }

    @Override
    public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
        return new Endpoint(callback, realm, event);
    }

    protected class Endpoint {
        protected AuthenticationCallback callback;
        protected RealmModel             realm;
        protected EventBuilder           event;

        @Context
        protected KeycloakSession  session;
        @Context
        protected ClientConnection clientConnection;
        @Context
        protected HttpHeaders      headers;
        @Context
        protected HttpRequest      httpRequest;

        public Endpoint(AuthenticationCallback callback, RealmModel realm, EventBuilder event) {
            this.callback = callback;
            this.realm = realm;
            this.event = event;
        }

        @GET
        public Response authResponse(@QueryParam(AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_STATE) String state,
                                     @QueryParam(AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_CODE) String authorizationCode,
                                     @QueryParam(OAuth2Constants.ERROR) String error,
                                     @QueryParam(AGENT_ID) String agentId) {

            logger.debugf("Authorization code [%s]", authorizationCode);
            if (StringUtil.isBlank(state)) {
                return this.error(Messages.IDENTITY_PROVIDER_MISSING_STATE_ERROR);
            }
            if (StringUtil.isNotBlank(error)) {
                logger.error(error + " for broker login " + getConfig().getProviderId());
                if (error.equals(ACCESS_DENIED)) {
                    return callback.cancelled();
                } else if (error.equals(OAuthErrorException.LOGIN_REQUIRED) || error.equals(OAuthErrorException.INTERACTION_REQUIRED)) {
                    return callback.error(error);
                } else {
                    return callback.error(Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
                }
            }
            try {
                AuthenticationSessionModel authSession = this.callback.getAndVerifyAuthenticationSession(state);
                session.getContext().setAuthenticationSession(authSession);

                BrokeredIdentityContext identityContext = getFederatedIdentity(authorizationCode);
                identityContext.setAuthenticationSession(authSession);
                return callback.authenticated(identityContext);
            } catch (WebApplicationException e) {
                return e.getResponse();
            } catch (Exception e) {
                logger.error("Failed to make identity provider oauth callback", e);
            }
            return this.error(Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
        }

        private Response error(String message) {
            event.event(EventType.IDENTITY_PROVIDER_LOGIN);
            event.error(Errors.IDENTITY_PROVIDER_LOGIN_FAILURE);
            return ErrorPage.error(session, null, Response.Status.BAD_GATEWAY, message);
        }
    }
}
