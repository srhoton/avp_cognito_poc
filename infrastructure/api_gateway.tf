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
  authorization = "NONE"
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
