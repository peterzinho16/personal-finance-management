package com.bindord.financemanagement.config;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class OAuth2Authenticator {

    private static final String CLIENT_ID = "e729a32b-1783-4866-9b42-93bb7043d97b";
    private static final String CLIENT_SECRET = ".lH8Q~tgnuT2gg-MJTk8DFYiDT1qNfiaGP53VcVh";
    private static final String TENANT_ID = "c0b8ff38-2c8e-4c6e-bbbe-1daa8a20687a"; // Use "common" for personal accounts, or your tenant ID.
    private static final String AUTHORITY = "https://login.microsoftonline.com/" + TENANT_ID;

    public static String getAccessToken() throws Exception {
        // Create confidential client application
        ConfidentialClientApplication app = ConfidentialClientApplication.builder(
                        CLIENT_ID,
                        ClientCredentialFactory.createFromSecret(CLIENT_SECRET))
                .authority(AUTHORITY)
                .build();

        // Request token for IMAP access
        ClientCredentialParameters parameters = ClientCredentialParameters.builder(
                        Collections.singleton("https://outlook.office365.com/.default"))
                .build();

        CompletableFuture<IAuthenticationResult> future = app.acquireToken(parameters);
        IAuthenticationResult result = future.join();

        return result.accessToken();
    }
}
