resource "aws_cloudwatch_log_group" "user_lambda" {
  name              = "/aws/lambda/user-lambda"
  retention_in_days = 14
}

resource "aws_lambda_function" "user_lambda" {
  function_name = "user-lambda"
  role          = aws_iam_role.user_lambda_role.arn
  handler       = "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest"
  runtime       = "java21"
  filename      = "../user-authorizer/build/function.zip"
  timeout       = 900
  memory_size   = 512
  logging_config {
    log_group  = aws_cloudwatch_log_group.user_lambda.name
    log_format = "Text"
  }
  environment {
    variables = {
      jwks_uri = "https://cognito-idp.${data.aws_region.current.name}.amazonaws.com/${aws_cognito_user_pool.user_pool.id}/.well-known/jwks.json"
    }
  }
  depends_on = [aws_cloudwatch_log_group.user_lambda]
}
