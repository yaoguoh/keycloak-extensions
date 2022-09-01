package org.keycloak.spi.authenticator.enums;

import cn.hutool.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
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
    CONFIG_UNINITIALIZED_ERROR(AuthenticationFlowError.IDENTITY_PROVIDER_ERROR, HttpStatus.HTTP_INTERNAL_ERROR,
            "Configuration not initialized", "Configuration not initialized %s"),
    /**
     * Config invalid error authentication error.
     */
    CONFIG_INVALID_ERROR(AuthenticationFlowError.IDENTITY_PROVIDER_ERROR, HttpStatus.HTTP_INTERNAL_ERROR,
            "Configuration parameter exception", "Configuration parameter exception unavailable[%s]-[]"),

    /**
     * User not found error authentication error.
     */
    USER_NOT_FOUND_ERROR(AuthenticationFlowError.INVALID_USER, HttpStatus.HTTP_UNAUTHORIZED,
            "User not found", "User not found %s:[%s]"),

    /**
     * User disabled error authentication error enum.
     */
    USER_DISABLED_ERROR(AuthenticationFlowError.USER_DISABLED, HttpStatus.HTTP_UNAUTHORIZED,
            "User is disabled", "User is disabled %s:[%s]"),

    /**
     * Password invalid error authentication error.
     */
    PASSWORD_INVALID_ERROR(AuthenticationFlowError.INVALID_CREDENTIALS, HttpStatus.HTTP_UNAUTHORIZED,
            "Invalid credentials", "Invalid username or Invalid password"),
    /**
     * Phone code invalid error authentication error.
     */
    CODE_INVALID_ERROR(AuthenticationFlowError.INVALID_CREDENTIALS, HttpStatus.HTTP_UNAUTHORIZED,
            "Invalid credentials", "Verification code does not match [%s]"),

    /**
     * Param not checked error authentication error.
     */
    PARAM_NOT_CHECKED_ERROR(AuthenticationFlowError.IDENTITY_PROVIDER_ERROR, HttpStatus.HTTP_BAD_REQUEST,
            "Invalid parameter", "Invalid parameter %s"),

    /**
     * System error authentication error.
     */
    SYSTEM_ERROR(AuthenticationFlowError.IDENTITY_PROVIDER_ERROR, HttpStatus.HTTP_INTERNAL_ERROR, "System error", "System error %s");

    private final AuthenticationFlowError flowError;

    private final int    status;
    private final String error;
    private final String errorDescription;

}
