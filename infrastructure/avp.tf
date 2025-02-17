resource "aws_verifiedpermissions_policy_store" "user_store" {
  description = "user_store"
  validation_settings {
    mode = "OFF"
  }
}

resource "aws_verifiedpermissions_identity_source" "user_identity_source" {
  policy_store_id = aws_verifiedpermissions_policy_store.user_store.id
  configuration {
    cognito_user_pool_configuration {
      user_pool_arn = aws_cognito_user_pool.user_pool.arn
      client_ids    = [aws_cognito_user_pool_client.user_pool_client.id]
    }
  }
}


resource "aws_verifiedpermissions_schema" "user_schema" {
  policy_store_id = aws_verifiedpermissions_policy_store.user_store.id
  definition {
    value = jsonencode({
      "USER_NAMESPACE" : {
        "actions" : {
          "get /" : {
            "appliesTo" : {
              "context" : {
                "attributes" : {},
                "type" : "Record"
              },
              "principalTypes" : [
                "User"
              ],
              "resourceTypes" : []
            },
            "memberOf" : []
          }
        },
        "commonTypes" : {
          "Users" : {
            "type" : "Record",
            "attributes" : {
              "cognito:username" : {
                "type" : "String",
                "required" : true
              },
              "email" : {
                "type" : "String",
                "required" : true
              },
              "tenant" : {
                "type" : "String",
                "required" : true
              }
            }
          }
        },
        "entityTypes" : {
          "User" : {
            "memberOfTypes" : [],
            "shape" : {
              "type" : "Users"
            }
          }
        }
      }
    })
  }
}

resource "aws_verifiedpermissions_policy" "user_policy" {
  policy_store_id = aws_verifiedpermissions_policy_store.user_store.id
  definition {
    static {
      statement = "permit(principal == USER_NAMESPACE::User::\"us-west-2_yPRxibyEB|f85183e0-1071-706f-54b0-6b1d69450fdd\", action in [USER_NAMESPACE::Action::\"get /\"], resource);"
    }
  }
}
