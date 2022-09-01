package org.keycloak.spi.authenticator.exception;

import lombok.Getter;
import org.keycloak.spi.authenticator.enums.AuthenticationErrorEnum;

/**
 * The type Authentication exception.
 *
 * @author yaoguoh
 */
@Getter
public class AuthenticationException extends RuntimeException {

    private final AuthenticationErrorEnum error;
    private final Object[]                args;

    public AuthenticationException(AuthenticationErrorEnum error, Object... args) {
        this.error = error;
        this.args = args;
    }
}
