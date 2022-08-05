package org.keycloak.spi.authenticator.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.http.HttpStatus;
import org.keycloak.authentication.AuthenticationFlowError;

/**
 * The enum Authentication error.
 *
 * @author yaoguoh
 */
@Getter
@AllArgsConstructor
public enum AuthenticationErrorEnum {
    /**
     * Config uninitialized error authentication error.
     */
    CONFIG_UNINITIALIZED_ERROR(HttpStatus.SC_INTERNAL_SERVER_ERROR, AuthenticationFlowError.IDENTITY_PROVIDER_ERROR,
            "1001", "配置文件未初始化: %s", "配置文件未初始化"),
    /**
     * Config invalid error authentication error.
     */
    CONFIG_INVALID_ERROR(HttpStatus.SC_INTERNAL_SERVER_ERROR, AuthenticationFlowError.IDENTITY_PROVIDER_ERROR,
            "1002", "配置文件参数异常: 不可用的[%s]-[]", "配置文件参数异常"),

    /**
     * User not found error authentication error.
     */
    USER_NOT_FOUND_ERROR(HttpStatus.SC_UNAUTHORIZED, AuthenticationFlowError.INVALID_USER,
            "2001", "未查询到用户: %s[%s]", "未查询到用户"),

    /**
     * User disabled error authentication error enum.
     */
    USER_DISABLED_ERROR(HttpStatus.SC_UNAUTHORIZED, AuthenticationFlowError.USER_DISABLED,
            "2002", "用户已禁用: %s[%s]", "用户已禁用"),

    /**
     * Password invalid error authentication error.
     */
    PASSWORD_INVALID_ERROR(HttpStatus.SC_UNAUTHORIZED, AuthenticationFlowError.INVALID_CREDENTIALS,
            "3001", "用户密码不匹配", "用户密码不匹配"),
    /**
     * Phone code invalid error authentication error.
     */
    CODE_INVALID_ERROR(HttpStatus.SC_UNAUTHORIZED, AuthenticationFlowError.INVALID_CREDENTIALS,
            "3002", "验证码校验失败: %s", "验证码不匹配"),

    /**
     * Param not checked error authentication error.
     */
    PARAM_NOT_CHECKED_ERROR(HttpStatus.SC_BAD_REQUEST, AuthenticationFlowError.IDENTITY_PROVIDER_ERROR,
            "9001", "参数校验异常: %s", "参数校验异常"),

    /**
     * System error authentication error.
     */
    SYSTEM_ERROR(HttpStatus.SC_INTERNAL_SERVER_ERROR, AuthenticationFlowError.IDENTITY_PROVIDER_ERROR,
            "9999", "未知内部异常: %s", "未知内部异常");

    private final int                     httpStatus;
    private final AuthenticationFlowError flowError;
    private final String                  code;
    private final String                  formatMessage;
    private final String                  defaultMessage;

}
