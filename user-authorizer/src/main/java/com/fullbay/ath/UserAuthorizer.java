package com.fullbay.ath;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponseV1;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponseV1.PolicyDocument;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponseV1.Statement;
import com.fasterxml.jackson.core.util.RecyclerPool.WithPool;
import com.nimbusds.jose.*;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.*;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.*;
import com.nimbusds.jwt.proc.*;
import jakarta.inject.Named;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.security.*;
import java.security.Policy;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
        String token = headers.get("Authorization");
        logger.log("Received token: " + token);
        logger.log("Token is valid: " + isTokenValid(token, logger));
        IamPolicyResponseV1.Statement allowStatement =
            IamPolicyResponseV1.Statement.builder()
                .withEffect(IamPolicyResponseV1.ALLOW)
                .withResource(Collections.singletonList("your-resource"))
                .withAction(IamPolicyResponseV1.EXECUTE_API_INVOKE)
                .withCondition(
                    Collections.singletonMap(
                        "StringEquals",
                        Collections.singletonMap("aws:username", "exampleUser")
                    )
                )
                .build();
        PolicyDocument policyDocument = PolicyDocument.builder()
            .withStatement(Collections.singletonList(allowStatement))
            .withVersion("2012-10-17")
            .build();

        IamPolicyResponseV1 response = IamPolicyResponseV1.builder()
            .withPrincipalId("example-user")
            .withPolicyDocument(policyDocument)
            .build();

        return response;
    }

    private Boolean isTokenValid(String token, LambdaLogger logger) {
        try {
            URI keyUri = new URI(
                "https://" + System.getenv("key_domain") + "/jwks.json"
            );
            URL keyUrl = keyUri.toURL();
            ConfigurableJWTProcessor<SecurityContext> jwtProcessor =
                new DefaultJWTProcessor<>();
            jwtProcessor.setJWSTypeVerifier(
                new DefaultJOSEObjectTypeVerifier<>(
                    new JOSEObjectType("at+jwt")
                )
            );
            JWKSource<SecurityContext> keySource = JWKSourceBuilder.create(
                keyUrl
            )
                .retrying(true)
                .build();
            JWSAlgorithm expectedJWSAlgorithm = JWSAlgorithm.RS256;
            JWSKeySelector<SecurityContext> keySelector =
                new JWSVerificationKeySelector<>(
                    expectedJWSAlgorithm,
                    keySource
                );

            jwtProcessor.setJWSKeySelector(keySelector);
            jwtProcessor.setJWTClaimsSetVerifier(
                new DefaultJWTClaimsVerifier<>(
                    new JWTClaimsSet.Builder()
                        .issuer("https://" + System.getenv("key_domain"))
                        .build(),
                    new HashSet<>(
                        Arrays.asList(
                            JWTClaimNames.SUBJECT,
                            JWTClaimNames.ISSUED_AT,
                            JWTClaimNames.EXPIRATION_TIME,
                            "scp",
                            "cid",
                            JWTClaimNames.JWT_ID
                        )
                    )
                )
            );

            SecurityContext ctx = null;
            JWTClaimsSet claimsSet;
            try {
                claimsSet = jwtProcessor.process(token, ctx);
            } catch (ParseException | BadJOSEException e) {
                logger.log("Error processing JWT token");
                return false;
            } catch (JOSEException e) {
                logger.log("Error processing JWT token");
                return false;
            }
            logger.log("JWT token processed successfully");
            logger.log("JWT claims set: " + claimsSet.toJSONObject());
            logger.log("JWT claims set: " + claimsSet.toJSONObject());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
