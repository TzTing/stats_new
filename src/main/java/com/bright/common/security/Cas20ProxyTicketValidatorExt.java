package com.bright.common.security;

import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas20ProxyTicketValidator;
import org.jasig.cas.client.validation.TicketValidationException;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author: Tz
 * @Date: 2023/05/30 18:24
 */
public class Cas20ProxyTicketValidatorExt extends Cas20ProxyTicketValidator {
    public Cas20ProxyTicketValidatorExt(String casServerUrlPrefix) {
        super(casServerUrlPrefix);
    }

    public final Assertion validate2(String ticket, String service) throws TicketValidationException {
        String validationUrl = this.constructValidationUrl(ticket, service);
        this.logger.debug("Constructing validation url: {}", validationUrl);

        try {
            this.logger.debug("Retrieving response from server.");
            String serverResponse = CommonUtilsExt.getResponseFromServer(new URL(validationUrl), this.getURLConnectionFactory(), this.getEncoding());
            if (serverResponse == null) {
                throw new TicketValidationException("The CAS server returned no response.");
            } else {
                this.logger.debug("Server response: {}", serverResponse);
                return this.parseResponseFromServer(serverResponse);
            }
        } catch (MalformedURLException var5) {
            throw new TicketValidationException(var5);
        }
    }
}

