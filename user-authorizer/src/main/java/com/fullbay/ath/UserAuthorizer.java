package com.fullbay.ath;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse.Statement.StatementBuilder;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponseV1;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponseV1.PolicyDocument;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponseV1.Statement;
import com.fasterxml.jackson.core.util.RecyclerPool.WithPool;
import com.nimbusds.jose.*;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.*;
import com.nimbusds.jwt.*;
import com.nimbusds.jwt.proc.*;
import jakarta.inject.Named;
import java.security.Policy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Named("authorizer")
public class UserAuthorizer
    implements
        RequestHandler<APIGatewayProxyRequestEvent, IamPolicyResponseV1> {

    @Override
    public IamPolicyResponseV1 handleRequest(
        APIGatewayProxyRequestEvent event,
        Context context
    ) {
        LambdaLogger logger = context.getLogger();
        logger.log("Received event: " + event);
        logger.log("Received context: " + context);

        Map<String, String> headers = event.getHeaders();

        Statement statement = IamPolicyResponseV1.allowStatement(
            "arn:aws:iam::123456789012:user/example-user"
        );
        PolicyDocument policyDocument = PolicyDocument.builder()
            .withStatement(Collections.singletonList(statement))
            .build();

        IamPolicyResponseV1 response = IamPolicyResponseV1.builder()
            .withPrincipalId("example-user")
            .withPolicyDocument(policyDocument)
            .build();

        return response;
    }

    private Boolean isTokenValid(String token) {
        try {
            JWSObject jwsObject = JWSObject.parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
