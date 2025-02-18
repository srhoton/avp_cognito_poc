package com.fullbay.ath.userauthorizer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponseV1;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponseV1.PolicyDocument;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponseV1.Statement;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import jakarta.inject.Named;
import com.nimbusds.jose.*;
import com.nimbusds.jose.jwk.source.*;
import com.nimbusds.jwt.*;
import com.nimbusds.jwt.proc.*;
import com.nimbusds.jose.jwk.JWKSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Named("authorizer")
public class UserAuthorizer
    implements RequestHandler<APIGatewayProxyRequestEvent, IamPolicyResponseV1> {

    @Override
    public IamPolicyResponseV1 handleRequest(
        APIGatewayProxyRequestEvent event,
        Context context
    ) {
        LambdaLogger logger = context.getLogger();

        Map<String, String> headers = event.getHeaders();

        Statement allowStatement = IamPolicyResponseV1.allowStatement(
            "arn:aws:execute-api:"
        );
        PolicyDocument policyDocument = PolicyDocument.builder()
            .withVersion(IamPolicyResponseV1.VERSION_2012_10_17)
            .withStatement(Collections.singletonList(allowStatement))
            .build();

        IamPolicyResponseV1 iamPolicyResponse = IamPolicyResponseV1.builder()
            .withPrincipalId("user|a1b2c3d4")
            .withPolicyDocument(policyDocument)
            .withContext(Collections.singletonMap("key", "value"))
            .withUsageIdentifierKey("usage-key")
            .build();

        return iamPolicyResponse;
    }

    private boolean isValidToken(String token) {
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        
        return true;
    }
}
