package com.bindord.financemanagement.config;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class OAuth2Authenticator {

    private static final String CLIENT_ID = "b33f4746-8cfc-41d4-a6bc-b13c92980040";
    private static final String CLIENT_SECRET = "i628Q~eqz-bQK9hqsEgluPbOoUEzMBGLEI1gXdgs";
    private static final String TENANT_ID = "2cf07475-10d6-48c7-b182-49c455f586d6"; // Use "common" for personal accounts, or your tenant ID.
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
                        Collections.singleton("https://graph.microsoft.com/.default"))
                .build();

        CompletableFuture<IAuthenticationResult> future = app.acquireToken(parameters);
        IAuthenticationResult result = future.join();

        return result.accessToken();
    }
}
