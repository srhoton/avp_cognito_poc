package com.fullbay.ath;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent;
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
import java.util.HashSet;
import java.util.Map;
import software.amazon.awssdk.services.verifiedpermissions.VerifiedPermissionsClient;
import software.amazon.awssdk.services.verifiedpermissions.model.ActionIdentifier;
import software.amazon.awssdk.services.verifiedpermissions.model.Decision;
import software.amazon.awssdk.services.verifiedpermissions.model.EntityIdentifier;
import software.amazon.awssdk.services.verifiedpermissions.model.IsAuthorizedWithTokenRequest;
import software.amazon.awssdk.services.verifiedpermissions.model.IsAuthorizedWithTokenResponse;

@Named("authorizer")
public class UserAuthorizer
    implements RequestHandler<APIGatewayCustomAuthorizerEvent, AuthPolicy> {

    @Override
    public AuthPolicy handleRequest(
        APIGatewayCustomAuthorizerEvent event,
        Context context
    ) {
        LambdaLogger logger = context.getLogger();
        logger.log("Received event: " + event.toString());
        logger.log("Received context: " + context.toString());

        Map<String, String> headers = event.getHeaders();
        APIGatewayCustomAuthorizerEvent.RequestContext requestContext =
            event.getRequestContext();
        logger.log("Received request context: " + requestContext.toString());
        String token = headers.get("authorization");
        logger.log("Received token: " + token);
        String path = requestContext.getPath();
        logger.log("Received path: " + path);
        Boolean tokenValid = isTokenValid(token, logger);
        logger.log("Token is valid: " + tokenValid);
        Boolean permissionValid = false;
        if (tokenValid) {
            permissionValid = isPermissionValid(token, path, logger);
            logger.log("Permission is valid: " + permissionValid);
        }
        if (tokenValid && permissionValid) {
            logger.log("Allow Policy document generated");
            String methodArn = event.getMethodArn();
            String[] arnPartials = methodArn.split(":");
            String region = arnPartials[3];
            String awsAccountId = arnPartials[4];
            String[] apiGatewayArnPartials = arnPartials[5].split("/");
            String restApiId = apiGatewayArnPartials[0];
            String stage = apiGatewayArnPartials[1];
            String httpMethod = apiGatewayArnPartials[2];
            String resource = ""; // root resource
            String principalId = "f85183e0-1071-706f-54b0-6b1d69450fdd";
            if (apiGatewayArnPartials.length == 4) {
                resource = apiGatewayArnPartials[3];
            }
            AuthPolicy authPolicy = new AuthPolicy(
                principalId,
                AuthPolicy.PolicyDocument.getAllowAllPolicy(
                    region,
                    awsAccountId,
                    restApiId,
                    stage
                )
            );
            logger.log(authPolicy.toString());
            return authPolicy;
        } else {
            String methodArn = event.getMethodArn();
            String[] arnPartials = methodArn.split(":");
            String region = arnPartials[3];
            String awsAccountId = arnPartials[4];
            String[] apiGatewayArnPartials = arnPartials[5].split("/");
            String restApiId = apiGatewayArnPartials[0];
            String stage = apiGatewayArnPartials[1];
            String httpMethod = apiGatewayArnPartials[2];
            String resource = ""; // root resource
            String principalId = "f85183e0-1071-706f-54b0-6b1d69450fdd";
            if (apiGatewayArnPartials.length == 4) {
                resource = apiGatewayArnPartials[3];
            }
            AuthPolicy authPolicy = new AuthPolicy(
                principalId,
                AuthPolicy.PolicyDocument.getAllowAllPolicy(
                    region,
                    awsAccountId,
                    restApiId,
                    stage
                )
            );
            logger.log(authPolicy.toString());
            return authPolicy;
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
            logger.log("Received claims set: " + claimsSet.toString());
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

    private Boolean isPermissionValid(
        String token,
        String path,
        LambdaLogger logger
    ) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        logger.log("Instantiating VerifiedPermissionsClient");
        try {
            VerifiedPermissionsClient client =
                VerifiedPermissionsClient.builder().build();

            ActionIdentifier action = ActionIdentifier.builder()
                .actionId("get /avp-user")
                .actionType("avpusergateway::Action")
                .build();
            EntityIdentifier entity = EntityIdentifier.builder()
                .entityId("avpusergateway")
                .entityType("avpusergateway::Application")
                .build();
            logger.log(entity.toString());
            logger.log(entity.entityId());
            logger.log(entity.entityType());
            IsAuthorizedWithTokenRequest request =
                IsAuthorizedWithTokenRequest.builder()
                    .policyStoreId("KjTXQVR2Wv2rrhtwMh2akc")
                    .identityToken(token)
                    .action(action)
                    .resource(entity)
                    .build();

            logger.log("Finished request");
            logger.log("Dumping request");
            logger.log(request.toString());
            logger.log("Finished dumping request");

            IsAuthorizedWithTokenResponse response =
                client.isAuthorizedWithToken(request);
            if (response.decision().equals(Decision.ALLOW)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.log("Error occurred while checking permission");
            logger.log("Error message: " + e.getMessage());
            e.printStackTrace(pw);
            logger.log("Stack trace: " + sw.toString());
            return false;
        }
    }
}
