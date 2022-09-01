package org.keycloak.spi.authenticator.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.keycloak.spi.authenticator.base.BaseAuthenticator;
import org.keycloak.spi.authenticator.email.EmailAuthenticator;
import org.keycloak.spi.authenticator.password.PasswordAuthenticator;
import org.keycloak.spi.authenticator.phone.PhoneAuthenticator;
import org.keycloak.spi.authenticator.username.UsernameAuthenticator;
import org.keycloak.spi.authenticator.wechat.WechatAuthenticator;

/**
 * The enum Login type.
 *
 * @author yaoguoh
 */
@Getter
@AllArgsConstructor
public enum LoginTypeEnum {
    /**
     * Email login type.
     */
    EMAIL("email", "Email code login", EmailAuthenticator.class),
    /**
     * Password login type.
     */
    PASSWORD("password", "username password login", PasswordAuthenticator.class),
    /**
     * Phone login type.
     */
    PHONE("phone", "Phone code login", PhoneAuthenticator.class),
    /**
     * Username login type.
     */
    USERNAME("username", "Username login", UsernameAuthenticator.class),
    /**
     * WeChat login type.
     */
    WECHAT("wechat", "Wechat unionid login", WechatAuthenticator.class);

    private final String                             code;
    private final String                             name;
    private final Class<? extends BaseAuthenticator> authenticatorClazz;

    /**
     * Contains code boolean.
     *
     * @param code the code
     * @return the boolean
     */
    public static boolean containsCode(String code) {
        for (LoginTypeEnum loginType : LoginTypeEnum.values()) {
            if (loginType.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
}
