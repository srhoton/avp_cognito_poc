data "aws_iam_policy_document" "user_lambda_logging" {
  statement {
    actions = [
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:PutLogEvents",
    ]

    resources = [
      "arn:aws:logs:*:*:*",
    ]
  }
}

data "aws_iam_policy_document" "user_lambda_assume_role" {
  statement {
    actions = [
      "sts:AssumeRole",
    ]

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

data "aws_iam_policy_document" "invocation_policy" {
  statement {

    effect = "Allow"

    actions = [
      "lambda:InvokeFunction",
    ]

    resources = [
      aws_lambda_function.user_lambda.arn
    ]
  }
}

data "aws_iam_policy_document" "invocation_assume_role_policy" {
  statement {
    effect = "Allow"

    actions = [
      "sts:AssumeRole",
    ]

    principals {
      type        = "Service"
      identifiers = ["apigateway.amazonaws.com"]
    }
  }
}

resource "aws_iam_policy" "user_lambda_logging" {
  policy      = data.aws_iam_policy_document.user_lambda_logging.json
  name        = "user_lambda_logging"
  path        = "/"
  description = "IAM policy for user lambda logging"
}

resource "aws_iam_policy" "user_lambda_invocation" {
  policy      = data.aws_iam_policy_document.invocation_policy.json
  name        = "user_lambda_invocation"
  path        = "/"
  description = "IAM policy for user lambda invocation"
}

resource "aws_iam_role" "user_lambda_role" {
  name               = "user_lambda_role"
  assume_role_policy = data.aws_iam_policy_document.user_lambda_assume_role.json
}

resource "aws_iam_role" "user_lambda_invocation_role" {
  name               = "user_lambda_invocation_role"
  assume_role_policy = data.aws_iam_policy_document.invocation_assume_role_policy.json
}

resource "aws_iam_role_policy_attachment" "user_lambda_logging_attachment" {
  role       = aws_iam_role.user_lambda_role.name
  policy_arn = aws_iam_policy.user_lambda_logging.arn
}

resource "aws_iam_role_policy_attachment" "user_lambda_basic_execution" {
  role       = aws_iam_role.user_lambda_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_role_policy_attachment" "user_lambda_invocation_policy_attachment" {
  role       = aws_iam_role.user_lambda_invocation_role.name
  policy_arn = aws_iam_policy.user_lambda_invocation.arn
}
