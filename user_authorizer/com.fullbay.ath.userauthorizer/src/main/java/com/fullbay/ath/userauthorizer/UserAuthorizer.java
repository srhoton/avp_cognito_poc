package com.fullbay.ath.userauthorizer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponseV1;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponseV1.PolicyDocument;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponseV1.Statement;
import jakarta.inject.Named;
import java.util.Collections;

@Named("authorizer")
public class UserAuthorizer
    implements RequestHandler<TokenAuthorizerContext, IamPolicyResponseV1> {

    @Override
    public IamPolicyResponseV1 handleRequest(
        TokenAuthorizerContext event,
        Context context
    ) {
        String token = event.getAuthorizationToken();
        String methodArn = event.getMethodArn();
        String[] arnPartials = methodArn.split(":");
        String region = arnPartials[3];
        String awsAccountId = arnPartials[4];
        String[] apiGatewayArnPartials = arnPartials[5].split("/");
        String restApiId = apiGatewayArnPartials[0];
        String stage = apiGatewayArnPartials[1];
        String httpMethod = apiGatewayArnPartials[2];
        String resource = ""; // root resource
        if (apiGatewayArnPartials.length == 4) {
            resource = apiGatewayArnPartials[3];
        }
        LambdaLogger logger = context.getLogger();
        logger.log("Token: " + token);
        logger.log("Method ARN: " + methodArn);
        logger.log("Region: " + region);


        Statement allowStatement = IamPolicyResponseV1.allowStatement(
            "arn:aws:execute-api:" +
            region +
            ":" +
            awsAccountId +
            ":" +
            restApiId +
            "/" +
            stage +
            "/" +
            httpMethod +
            "/" +
            resource
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
}
