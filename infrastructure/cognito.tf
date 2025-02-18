resource "aws_cognito_user_pool" "user_pool" {
  name                     = "avp-cognito-poc-user-pool"
  username_attributes      = ["email"]
  auto_verified_attributes = ["email"]
  password_policy {
    minimum_length    = 8
    require_lowercase = true
    require_numbers   = true
    require_symbols   = true
    require_uppercase = true
  }
  mfa_configuration = "OFF"
  username_configuration {
    case_sensitive = false
  }
  admin_create_user_config {
    allow_admin_create_user_only = false
  }
  email_configuration {
    reply_to_email_address = ""
  }
  schema {
    attribute_data_type      = "String"
    developer_only_attribute = false
    mutable                  = true
    name                     = "Role"
    required                 = false
    string_attribute_constraints {}
  }
}

resource "aws_cognito_user_pool" "machine_pool" {
  name                     = "avp-cognito-poc-machine-pool"
  username_attributes      = ["email"]
  auto_verified_attributes = ["email"]
  password_policy {
    minimum_length    = 8
    require_lowercase = true
    require_numbers   = true
    require_symbols   = true
    require_uppercase = true
  }
  mfa_configuration = "OFF"
  username_configuration {
    case_sensitive = false
  }
  admin_create_user_config {
    allow_admin_create_user_only = false
  }
  email_configuration {
    reply_to_email_address = ""
  }
  schema {
    attribute_data_type      = "String"
    developer_only_attribute = false
    mutable                  = true
    name                     = "Role"
    required                 = false
    string_attribute_constraints {}
  }
}

resource "aws_cognito_user_pool_domain" "user_pool_domain" {
  domain       = "avp-poc-user-pool"
  user_pool_id = aws_cognito_user_pool.user_pool.id
}

resource "aws_cognito_user_pool_domain" "machine_pool_domain" {
  domain       = "avp-poc-machine-pool"
  user_pool_id = aws_cognito_user_pool.machine_pool.id
}

resource "aws_cognito_user_pool_client" "user_pool_client" {
  name                                 = "avp-cognito-poc-user-pool-client"
  user_pool_id                         = aws_cognito_user_pool.user_pool.id
  generate_secret                      = false
  allowed_oauth_flows                  = ["implicit"]
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_scopes                 = ["email", "openid", "profile"]
  callback_urls                        = ["https://example.com"]
  logout_urls                          = ["https://example.com"]
  supported_identity_providers         = ["COGNITO"]
  explicit_auth_flows                  = ["ALLOW_USER_PASSWORD_AUTH", "ALLOW_REFRESH_TOKEN_AUTH", "ALLOW_USER_SRP_AUTH"]
}

resource "aws_cognito_user_pool_client" "machine_pool_client" {
  name                                 = "avp-cognito-poc-machine-pool-client"
  user_pool_id                         = aws_cognito_user_pool.machine_pool.id
  generate_secret                      = false
  allowed_oauth_flows                  = ["implicit"]
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_scopes                 = ["email", "openid", "profile"]
  callback_urls                        = ["https://example.com"]
  logout_urls                          = ["https://example.com"]
  supported_identity_providers         = ["COGNITO"]
  explicit_auth_flows                  = ["ALLOW_USER_PASSWORD_AUTH", "ALLOW_REFRESH_TOKEN_AUTH", "ALLOW_USER_SRP_AUTH"]
}
