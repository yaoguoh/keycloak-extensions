package org.keycloak.spi.authenticator.wechat;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.UserModel;
import org.keycloak.spi.authenticator.base.BaseAuthenticator;
import org.keycloak.spi.authenticator.enums.AuthenticationErrorEnum;
import org.keycloak.spi.authenticator.exception.AuthenticationException;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * The type Wechat authenticator.
 *
 * @author yaoguoh
 */
public class WechatAuthenticator extends BaseAuthenticator {

    @Override
    protected void doAuthenticate(AuthenticationFlowContext context) {
        String unionId = super.getRequestParameter(context, WechatAuthenticatorFactory.PROPERTY_FORM_WECHAT_UNIONID);
        // 参数校验
        if (StringUtils.isEmpty(unionId)) {
            throw new AuthenticationException(AuthenticationErrorEnum.PARAM_NOT_CHECKED_ERROR, "微信号不能为空");
        }
        // 使用微信 unionId 查询用户
        final Stream<UserModel> userModelStream = context.getSession().userStorageManager()
                .searchForUserByUserAttributeStream(
                        context.getRealm(),
                        super.getPropertyValue(context, WechatAuthenticatorFactory.PROPERTY_USER_ATTRIBUTE_WECHAT_UNIONID),
                        unionId);
        final Optional<UserModel> optional = userModelStream.findFirst();
        final UserModel userModel = optional.orElseThrow(() -> {
            throw new AuthenticationException(AuthenticationErrorEnum.USER_NOT_FOUND_ERROR, "微信号", unionId);
        });
        context.setUser(userModel);
        context.success();
    }
}
