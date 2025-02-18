resource "aws_api_gateway_rest_api" "avp_user_gateway" {
  name        = "avp-user-gateway"
  description = "API Gateway for AVP User Service"
  endpoint_configuration {
    types = ["REGIONAL"]
  }
}
resource "aws_api_gateway_rest_api" "avp_machine_gateway" {
  name        = "avp-machine-gateway"
  description = "API Gateway for AVP Machine Service"
  endpoint_configuration {
    types = ["REGIONAL"]
  }
}

resource "aws_api_gateway_resource" "avp_user_resource" {
  rest_api_id = aws_api_gateway_rest_api.avp_user_gateway.id
  parent_id   = aws_api_gateway_rest_api.avp_user_gateway.root_resource_id
  path_part   = "avp-user"
}

resource "aws_api_gateway_method" "avp_user_method" {
  rest_api_id   = aws_api_gateway_rest_api.avp_user_gateway.id
  resource_id   = aws_api_gateway_resource.avp_user_resource.id
  http_method   = "GET"
  authorization = "CUSTOM"
  authorizer_id = aws_api_gateway_authorizer.avp_user_authorizer.id
}

resource "aws_api_gateway_integration" "user_integration" {
  rest_api_id = aws_api_gateway_rest_api.avp_user_gateway.id
  resource_id = aws_api_gateway_resource.avp_user_resource.id
  http_method = aws_api_gateway_method.avp_user_method.http_method
  type        = "MOCK"
}

resource "aws_api_gateway_deployment" "user_deployment" {
  rest_api_id = aws_api_gateway_rest_api.avp_user_gateway.id

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_api_gateway_stage" "user_production_stage" {
  deployment_id = aws_api_gateway_deployment.user_deployment.id
  stage_name    = "prod"
  rest_api_id   = aws_api_gateway_rest_api.avp_user_gateway.id
}

resource "aws_api_gateway_resource" "avp_machine_resource" {
  rest_api_id = aws_api_gateway_rest_api.avp_machine_gateway.id
  parent_id   = aws_api_gateway_rest_api.avp_machine_gateway.root_resource_id
  path_part   = "avp-machine"
}

resource "aws_api_gateway_method" "avp_machine_method" {
  rest_api_id   = aws_api_gateway_rest_api.avp_machine_gateway.id
  resource_id   = aws_api_gateway_resource.avp_machine_resource.id
  http_method   = "GET"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "machine_integration" {
  rest_api_id = aws_api_gateway_rest_api.avp_machine_gateway.id
  resource_id = aws_api_gateway_resource.avp_machine_resource.id
  http_method = aws_api_gateway_method.avp_machine_method.http_method
  type        = "MOCK"
}

resource "aws_api_gateway_authorizer" "avp_user_authorizer" {
  rest_api_id            = aws_api_gateway_rest_api.avp_user_gateway.id
  name                   = "avp_user_authorizer"
  authorizer_uri         = aws_lambda_function.user_lambda.invoke_arn
  authorizer_credentials = aws_iam_role.user_lambda_invocation_role.arn
  type                   = "REQUEST"
}

resource "aws_api_gateway_deployment" "machine_deployment" {
  rest_api_id = aws_api_gateway_rest_api.avp_machine_gateway.id

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_api_gateway_stage" "machine_production_stage" {
  deployment_id = aws_api_gateway_deployment.machine_deployment.id
  stage_name    = "prod"
  rest_api_id   = aws_api_gateway_rest_api.avp_machine_gateway.id
}
