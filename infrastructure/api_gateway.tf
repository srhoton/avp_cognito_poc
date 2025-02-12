resource "aws_apigatewayv2_api" "user_gateway" {
  name          = "avp-cognito-poc-user-gateway"
  protocol_type = "HTTP"
  cors_configuration {
    allow_headers  = ["*"]
    allow_methods  = ["*"]
    allow_origins  = ["*"]
    expose_headers = ["*"]
    max_age        = 300
  }
}

resource "aws_apigatewayv2_api" "machine_gateway" {
  name          = "avp-cognito-poc-machine-gateway"
  protocol_type = "HTTP"
  cors_configuration {
    allow_headers  = ["*"]
    allow_methods  = ["*"]
    allow_origins  = ["*"]
    expose_headers = ["*"]
    max_age        = 300
  }
}
