package org.keycloak.spi.authenticator.password;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.spi.authenticator.base.BaseAuthenticator;
import org.keycloak.spi.authenticator.enums.AuthenticationErrorEnum;
import org.keycloak.spi.authenticator.exception.AuthenticationException;

import java.util.Optional;

/**
 * The type Password authenticator.
 *
 * @author yaoguoh
 */
public class PasswordAuthenticator extends BaseAuthenticator {

    @Override
    protected void doAuthenticate(AuthenticationFlowContext context) {
        final String username = super.getRequestParameter(context, PasswordAuthenticatorFactory.PROPERTY_FORM_USERNAME);
        final String password = super.getRequestParameter(context, PasswordAuthenticatorFactory.PROPERTY_FORM_PASSWORD);

        // 参数校验
        if (StringUtils.isEmpty(username)) {
            throw new AuthenticationException(AuthenticationErrorEnum.PARAM_NOT_CHECKED_ERROR, "用户名不能为空");
        }
        if (StringUtils.isEmpty(password)) {
            throw new AuthenticationException(AuthenticationErrorEnum.PARAM_NOT_CHECKED_ERROR, "密码不能为空");
        }
        // 通过用户名查询用户
        Optional<UserModel> optional  = Optional.of(context.getSession().userStorageManager().getUserByUsername(context.getRealm(), username));
        UserModel           userModel = super.validateUser("用户名", username, optional);
        // 校验密码
        boolean isValid = context.getSession().userCredentialManager().isValid(context.getRealm(), userModel, UserCredentialModel.password(password));
        if (!isValid) {
            throw new AuthenticationException(AuthenticationErrorEnum.PASSWORD_INVALID_ERROR);
        }
        context.setUser(userModel);
        context.success();
    }
}
