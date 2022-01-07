package org.keycloak.spi.authenticator.base;

import org.keycloak.Config.Scope;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.spi.authenticator.enums.LoginTypeEnum;

/**
 * The type Base authenticator factory.
 *
 * @author yaoguoh
 */
public abstract class BaseAuthenticatorFactory implements AuthenticatorFactory {

    /**
     * The constant REQUIREMENT_CHOICES.
     */
    protected static final Requirement[] REQUIREMENT_CHOICES = {Requirement.REQUIRED, Requirement.ALTERNATIVE, Requirement.DISABLED};

    /**
     * Gets login type.
     *
     * @return the login type
     */
    protected abstract LoginTypeEnum getLoginType();

    @Override
    public String getId() {
        return getLoginType().getCode() + "-login-authenticator";
    }

    @Override
    public Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public String getDisplayType() {
        return getLoginType().getCode() + " login authenticator";
    }

    @Override
    public String getReferenceCategory() {
        return getLoginType().getCode() + " login authenticator";
    }

    @Override
    public String getHelpText() {
        return getLoginType().getName() + " verifier";
    }

    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public void init(Scope scope) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }
}
