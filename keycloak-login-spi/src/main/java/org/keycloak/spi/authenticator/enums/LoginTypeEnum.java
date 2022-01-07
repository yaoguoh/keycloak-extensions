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
    EMAIL("email", "邮箱登录", EmailAuthenticator.class),
    /**
     * Password login type.
     */
    PASSWORD("password", "密码登陆", PasswordAuthenticator.class),
    /**
     * Phone login type.
     */
    PHONE("phone", "手机号验证码登陆", PhoneAuthenticator.class),
    /**
     * Username login type.
     */
    USERNAME("username", "用户名登陆", UsernameAuthenticator.class),
    /**
     * WeChat login type.
     */
    WECHAT("wechat", "微信登陆", WechatAuthenticator.class);

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
