package com.pig4cloud.pigx.pay.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.OAuthTokenCredential;
import com.paypal.base.rest.PayPalRESTException;

@Configuration
public class PaypalConfig {
	
	//@Value("${paypal.client.app}")
    private static String clientId = "AZkov2VzBok6K-9FELUwpUxgWCfsSFDSf7xVCLfHoMSh2QgkR01l8MD3vmCrpnSGiggKJjZBXQEHg4SN";
    //@Value("${paypal.client.secret}")
    private static String clientSecret = "EOejFgEFpMu8-WXSvywE52LsLt2wlw0mIwSuulM4lD2VKEqBEcMYUC3nXVdYxhBi9jyprELq9TrxUI0i";
    //@Value("${paypal.mode}")
    private static String mode = "sandbox";

    @Bean
    public Map<String, String> paypalSdkConfig(){
        Map<String, String> sdkConfig = new HashMap<>();
        sdkConfig.put("mode", mode);
        return sdkConfig;
    }

    @Bean
    public OAuthTokenCredential authTokenCredential(){
        return new OAuthTokenCredential(clientId, clientSecret, paypalSdkConfig());
    }

    @Bean
    public APIContext apiContext() throws PayPalRESTException{
        APIContext apiContext = new APIContext(authTokenCredential().getAccessToken());
        apiContext.setConfigurationMap(paypalSdkConfig());
        return apiContext;
    }

}
