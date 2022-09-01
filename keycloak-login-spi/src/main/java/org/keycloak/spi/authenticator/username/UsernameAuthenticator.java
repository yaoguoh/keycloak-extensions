package org.keycloak.spi.authenticator.username;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.UserModel;
import org.keycloak.spi.authenticator.base.BaseAuthenticator;
import org.keycloak.spi.authenticator.enums.AuthenticationErrorEnum;
import org.keycloak.spi.authenticator.exception.AuthenticationException;

/**
 * The type Username authenticator.
 *
 * @author yaoguoh
 */
public class UsernameAuthenticator extends BaseAuthenticator {

    @Override
    protected void doAuthenticate(AuthenticationFlowContext context) {
        final String username = super.getRequestParameter(context, UsernameAuthenticatorFactory.PROPERTY_FORM_USERNAME);
        // 参数校验
        if (StringUtils.isEmpty(username)) {
            throw new AuthenticationException(AuthenticationErrorEnum.PARAM_NOT_CHECKED_ERROR, "Username must be filled!");
        }
        // 通过用户名查询用户
        UserModel findUser  = context.getSession().users().getUserByUsername(context.getRealm(), username);
        UserModel userModel = super.validateUser("username", username, findUser);
        context.setUser(userModel);
        context.success();
    }
}
