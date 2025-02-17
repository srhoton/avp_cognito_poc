package com.fullbay.ath;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponseV1;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponseV1.PolicyDocument;
//import com.amazonaws.services.lambda.runtime.events.IamPolicyResponseV1.Statement;
import com.nimbusds.jose.*;
import com.nimbusds.jose.jwk.source.*;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.*;
import com.nimbusds.jwt.proc.*;
import jakarta.inject.Named;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
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
        logger.log("Received event: " + event.toString());
        logger.log("Received context: " + context.toString());

        Map<String, String> headers = event.getHeaders();
        APIGatewayProxyRequestEvent.ProxyRequestContext requestContext =
            event.getRequestContext();
        logger.log("Received request context: " + requestContext.toString());
        String token = headers.get("Authorization");
        logger.log("Received token: " + token);
        Boolean tokenValid = isTokenValid(token, logger);
        logger.log("Token is valid: " + tokenValid);
        if (tokenValid) {
            IamPolicyResponseV1.Statement allowStatement =
                IamPolicyResponseV1.Statement.builder()
                    .withEffect(IamPolicyResponseV1.ALLOW)
                    .withResource(
                        Collections.singletonList(
                            context.getInvokedFunctionArn()
                        )
                    )
                    .withAction(IamPolicyResponseV1.EXECUTE_API_INVOKE)
                    .withCondition(
                        Collections.singletonMap(
                            "StringEquals",
                            Collections.singletonMap(
                                "aws:username",
                                requestContext.getIdentity().getCaller()
                            )
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
        } else {
            IamPolicyResponseV1.Statement denyStatement =
                IamPolicyResponseV1.Statement.builder()
                    .withEffect(IamPolicyResponseV1.DENY)
                    .withResource(
                        Collections.singletonList(
                            context.getInvokedFunctionArn()
                        )
                    )
                    .withAction(IamPolicyResponseV1.EXECUTE_API_INVOKE)
                    .withCondition(
                        Collections.singletonMap(
                            "StringEquals",
                            Collections.singletonMap(
                                "aws:username",
                                requestContext.getIdentity().getCaller()
                            )
                        )
                    )
                    .build();
            PolicyDocument policyDocument = PolicyDocument.builder()
                .withStatement(Collections.singletonList(denyStatement))
                .withVersion("2012-10-17")
                .build();

            IamPolicyResponseV1 response = IamPolicyResponseV1.builder()
                .withPrincipalId("example-user")
                .withPolicyDocument(policyDocument)
                .build();

            return response;
        }
    }

    private Boolean isTokenValid(String accessToken, LambdaLogger logger) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        logger.log(accessToken);
        try {
            ConfigurableJWTProcessor<SecurityContext> jwtProcessor =
                new DefaultJWTProcessor<>();
            jwtProcessor.setJWSTypeVerifier(
                new DefaultJOSEObjectTypeVerifier(JOSEObjectType.JWT, null)
            );
            JWKSource<SecurityContext> jwkSource = JWKSourceBuilder.create(
                new URL(System.getenv("jwks_uri"))
            )
                .retrying(true)
                .build();
            JWSAlgorithm jwsAlgorithm = JWSAlgorithm.RS256;
            JWSKeySelector<SecurityContext> keySelector =
                new JWSVerificationKeySelector<>(jwsAlgorithm, jwkSource);
            jwtProcessor.setJWSKeySelector(keySelector);
            jwtProcessor.setJWTClaimsSetVerifier(
                new DefaultJWTClaimsVerifier<>(
                    new JWTClaimsSet.Builder()
                        .issuer(
                            "https://cognito-idp.us-west-2.amazonaws.com/us-west-2_yPRxibyEB"
                        )
                        .build(),
                    new HashSet<>(
                        Arrays.asList(
                            JWTClaimNames.SUBJECT,
                            JWTClaimNames.ISSUED_AT,
                            JWTClaimNames.EXPIRATION_TIME
                        )
                    )
                )
            );
            JWTClaimsSet claimsSet;
            SecurityContext ctx = null;
            claimsSet = jwtProcessor.process(accessToken, ctx);
            //JWT jwt = JWTParser.parse(accessToken);
        } catch (ParseException e) {
            logger.log("Invalid JWT encoding");
            logger.log("Error message: " + e.getMessage());
            e.printStackTrace(pw);
            logger.log("Stack trace: " + sw.toString());
        } catch (MalformedURLException e) {
            logger.log("Invalid URL");
            logger.log("Error message: " + e.getMessage());
            e.printStackTrace(pw);
            logger.log("Stack trace: " + sw.toString());
        } catch (JOSEException e) {
            logger.log("Invalid JWT signature");
            logger.log("Error message: " + e.getMessage());
            e.printStackTrace(pw);
            logger.log("Stack trace: " + sw.toString());
        } catch (BadJOSEException e) {
            logger.log("Invalid JWT signature");
            e.printStackTrace(pw);
            logger.log("Stack trace: " + sw.toString());
            logger.log("Stack trace: " + sw.toString());
        }
        logger.log("Token is valid");
        return true;
    }
}
