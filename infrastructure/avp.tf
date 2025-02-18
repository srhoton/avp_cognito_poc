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
      "avpusergateway" : {
        "entityTypes" : {
          "User" : {
            "shape" : {
              "type" : "Record",
              "attributes" : {
                "profile" : {
                  "type" : "String",
                  "required" : false
                },
                "address" : {
                  "type" : "String",
                  "required" : false
                },
                "birthdate" : {
                  "type" : "String",
                  "required" : false
                },
                "gender" : {
                  "type" : "String",
                  "required" : false
                },
                "preferred_username" : {
                  "type" : "String",
                  "required" : false
                },
                "updated_at" : {
                  "type" : "Long",
                  "required" : false
                },
                "website" : {
                  "type" : "String",
                  "required" : false
                },
                "picture" : {
                  "type" : "String",
                  "required" : false
                },
                "identities" : {
                  "type" : "String",
                  "required" : false
                },
                "sub" : {
                  "type" : "String",
                  "required" : true
                },
                "phone_number" : {
                  "type" : "String",
                  "required" : false
                },
                "phone_number_verified" : {
                  "type" : "Boolean",
                  "required" : false
                },
                "zoneinfo" : {
                  "type" : "String",
                  "required" : false
                },
                "custom:Role" : {
                  "type" : "String",
                  "required" : false
                },
                "locale" : {
                  "type" : "String",
                  "required" : false
                },
                "email" : {
                  "type" : "String",
                  "required" : false
                },
                "email_verified" : {
                  "type" : "Boolean",
                  "required" : false
                },
                "given_name" : {
                  "type" : "String",
                  "required" : false
                },
                "family_name" : {
                  "type" : "String",
                  "required" : false
                },
                "middle_name" : {
                  "type" : "String",
                  "required" : false
                },
                "name" : {
                  "type" : "String",
                  "required" : false
                },
                "nickname" : {
                  "type" : "String",
                  "required" : false
                }
              }
            },
            "memberOfTypes" : [
              "UserGroup"
            ]
          },
          "UserGroup" : {
            "shape" : {
              "attributes" : {},
              "type" : "Record"
            }
          },
          "Application" : {
            "shape" : {
              "attributes" : {},
              "type" : "Record"
            }
          }
        },
        "actions" : {
          "get /avp-user" : {
            "appliesTo" : {
              "context" : {
                "type" : "Record",
                "attributes" : {}
              },
              "principalTypes" : [
                "User"
              ],
              "resourceTypes" : [
                "Application"
              ]
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
      statement = "permit( principal in avpusergateway::UserGroup::\"us-west-2_yPRxibyEB|ui_pool\", action in [ avpusergateway::Action::\"get /avp-user\" ], resource );"
    }
  }
}
