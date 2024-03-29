package org.keycloak.spi.authenticator.wechat;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.UserModel;
import org.keycloak.spi.authenticator.base.BaseAuthenticator;
import org.keycloak.spi.authenticator.enums.AuthenticationErrorEnum;
import org.keycloak.spi.authenticator.exception.AuthenticationException;

import java.util.Optional;

/**
 * The type Wechat authenticator.
 *
 * @author yaoguoh
 */
public class WechatAuthenticator extends BaseAuthenticator {

    @Override
    protected void doAuthenticate(AuthenticationFlowContext context) {
        final String unionId = super.getRequestParameter(context, WechatAuthenticatorFactory.PROPERTY_FORM_WECHAT_UNIONID);
        // 参数校验
        if (StringUtils.isEmpty(unionId)) {
            throw new AuthenticationException(AuthenticationErrorEnum.PARAM_NOT_CHECKED_ERROR, "Wechat unionid must be filled!");
        }
        // 使用微信 unionId 查询用户
        Optional<UserModel> optional = context.getSession().users()
                .searchForUserByUserAttributeStream(
                        context.getRealm(),
                        super.getPropertyValue(context, WechatAuthenticatorFactory.PROPERTY_USER_ATTRIBUTE_WECHAT_UNIONID),
                        unionId)
                .findFirst();
        UserModel userModel = super.validateUser("wechat unionid", unionId, optional.orElse(null));
        context.setUser(userModel);
        context.success();
    }
}
