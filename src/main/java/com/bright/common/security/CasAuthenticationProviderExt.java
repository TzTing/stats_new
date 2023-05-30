package com.bright.common.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.*;
import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.*;
import org.springframework.util.Assert;

/**
 * @author: Tz
 * @Date: 2023/05/30 18:24
 */
public class CasAuthenticationProviderExt extends CasAuthenticationProvider {
    private static final Log logger = LogFactory.getLog(CasAuthenticationProvider.class);
    private final UserDetailsChecker userDetailsChecker = new AccountStatusUserDetailsChecker();
    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();
    private Cas20ProxyTicketValidatorExt ticketValidator;

    private ServiceProperties serviceProperties;

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(this.ticketValidator, "A ticketValidator must be set");
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!this.supports(authentication.getClass())) {
            return null;
        } else if (authentication instanceof UsernamePasswordAuthenticationToken && !"_cas_stateful_".equals(authentication.getPrincipal().toString()) && !"_cas_stateless_".equals(authentication.getPrincipal().toString())) {
            return null;
        } else if (authentication instanceof CasAuthenticationToken) {
            if (getKey().hashCode() == ((CasAuthenticationToken) authentication).getKeyHash()) {
                return authentication;
            } else {
                throw new BadCredentialsException(this.messages.getMessage("CasAuthenticationProviderExt.incorrectKey", "The presented CasAuthenticationToken does not contain the expected key"));
            }
        } else if (authentication.getCredentials() != null && !"".equals(authentication.getCredentials())) {
            boolean stateless = false;
            if (authentication instanceof UsernamePasswordAuthenticationToken && "_cas_stateless_".equals(authentication.getPrincipal())) {
                stateless = true;
            }

            CasAuthenticationToken result = null;
            if (stateless) {
                result = getStatelessTicketCache().getByTicketId(authentication.getCredentials().toString());
            }

            if (result == null) {
                result = this.authenticateNow(authentication);
                result.setDetails(authentication.getDetails());
            }

            if (stateless) {
                getStatelessTicketCache().putTicketInCache(result);
            }

            return result;
        } else {
            throw new BadCredentialsException(this.messages.getMessage("CasAuthenticationProviderExt.noServiceTicket", "Failed to provide a CAS service ticket to validate"));
        }
    }

    private CasAuthenticationToken authenticateNow(Authentication authentication) throws AuthenticationException {
        try {
            Assertion assertion = this.ticketValidator.validate2(authentication.getCredentials().toString(), this.getServiceUrl(authentication));
            UserDetails userDetails = this.loadUserByAssertion(assertion);
            userDetailsChecker.check(userDetails);
            return new CasAuthenticationToken(getKey(), userDetails, authentication.getCredentials(), this.authoritiesMapper.mapAuthorities(userDetails.getAuthorities()), userDetails, assertion);
        } catch (TicketValidationException var4) {
            throw new BadCredentialsException(var4.getMessage(), var4);
        }
    }

    private String getServiceUrl(Authentication authentication) {
        String serviceUrl;
        if (authentication.getDetails() instanceof ServiceAuthenticationDetails) {
            serviceUrl = ((ServiceAuthenticationDetails) authentication.getDetails()).getServiceUrl();
        } else {
            if (this.serviceProperties == null) {
                throw new IllegalStateException("serviceProperties cannot be null unless Authentication.getDetails() implements ServiceAuthenticationDetails.");
            }

            if (this.serviceProperties.getService() == null) {
                throw new IllegalStateException("serviceProperties.getService() cannot be null unless Authentication.getDetails() implements ServiceAuthenticationDetails.");
            }

            serviceUrl = this.serviceProperties.getService();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("serviceUrl = " + serviceUrl);
        }

        return serviceUrl;
    }

    @Override
    public void setAuthoritiesMapper(GrantedAuthoritiesMapper authoritiesMapper) {
        this.authoritiesMapper = authoritiesMapper;
    }

    @Override
    public void setServiceProperties(ServiceProperties serviceProperties) {
        this.serviceProperties = serviceProperties;
    }

    @Override
    protected Cas20ProxyTicketValidatorExt getTicketValidator() {
        return this.ticketValidator;
    }

    public void setTicketValidator(Cas20ProxyTicketValidatorExt ticketValidator) {
        this.ticketValidator = ticketValidator;
    }
}
