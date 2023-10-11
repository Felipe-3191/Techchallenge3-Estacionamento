provider "aws" {
  region = "us-east-1" 
}


resource "aws_iam_role_policy" "lambdasqs_policy" {
  name = "lambdasqs_policy"
  role = aws_iam_role.lambda_role_write_to_sqs.id
  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Effect" : "Allow",
        "Action" : [
          "sqs:SendMessage",
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes",
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ],
        "Resource" : "*"
      },    
      {
            "Sid": "ReadWriteTable",
            "Effect": "Allow",
            "Action": [
                "dynamodb:BatchGetItem",
                "dynamodb:GetItem",
                "dynamodb:Query",
                "dynamodb:Scan",
                "dynamodb:BatchWriteItem",
                "dynamodb:PutItem",
                "dynamodb:UpdateItem"
            ],
            "Resource": "*"
        },
        {
            "Sid": "GetStreamRecords",
            "Effect": "Allow",
            "Action": "dynamodb:GetRecords",
            "Resource": "*"
        }
        ]
})
}

# Create an IAM role for the first Lambda function
resource "aws_iam_role" "lambda_role_write_to_sqs" {
  name = "lambda-role-write-to-sqs"

  assume_role_policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Effect" : "Allow",
        "Action" : [
          "sts:AssumeRole"
        ],
        "Principal" : {
          "Service" : [
            "lambda.amazonaws.com"
          ]
        }
      }
    ]
  })
}


resource "aws_lambda_function" "lambda_write_to_sqs" {
  function_name = "lambda-write-to-sqs"
  handler       = "com.fiap.techChallenge3.registroSQS.SQSPublisher::handleRequest"
  runtime       = "java17"
  role          = aws_iam_role.lambda_role_write_to_sqs.arn
  filename      = "" # trocar pelo caminho do jar da lambda
  memory_size   = 256
  timeout       = 30
  environment {
    variables = {
      SQS_QUEUE_URL = aws_sqs_queue.park_register_queue.id
    }
  }
}


resource "aws_sqs_queue" "park_register_queue" {
  name                       = "example-queue"
  delay_seconds              = 90
  max_message_size           = 4096
  message_retention_seconds  = 86400 # 1 dia
  visibility_timeout_seconds = 30
  fifo_queue                 = false
}

variable "myregion" {
  type    = string
  default = "us-east-1"
}

variable "accountId" {
  type    = string
  default = "659214650186"
}

resource "aws_api_gateway_rest_api" "parquimetro_api" {
  name = "techchallenge3"
}

resource "aws_api_gateway_resource" "pagamento_resource" {
  path_part   = "pagamento"
  parent_id   = aws_api_gateway_rest_api.parquimetro_api.root_resource_id
  rest_api_id = aws_api_gateway_rest_api.parquimetro_api.id
}

resource "aws_api_gateway_method" "method" {
  rest_api_id   = aws_api_gateway_rest_api.parquimetro_api.id
  resource_id   = aws_api_gateway_resource.pagamento_resource.id
  http_method   = "GET"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "integration" {
  rest_api_id             = aws_api_gateway_rest_api.parquimetro_api.id
  resource_id             = aws_api_gateway_resource.pagamento_resource.id
  http_method             = aws_api_gateway_method.method.http_method
  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.lambda_write_to_sqs.invoke_arn
}

# Lambda
resource "aws_lambda_permission" "apigw_lambda" {
  statement_id  = "AllowExecutionFromAPIGateway"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.lambda_write_to_sqs.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn = "arn:aws:execute-api:${var.myregion}:${var.accountId}:${aws_api_gateway_rest_api.parquimetro_api.id}/*/${aws_api_gateway_method.method.http_method}${aws_api_gateway_resource.pagamento_resource.path}"
}

resource "aws_dynamodb_table" "registro-estacionamento-table" {
  name           = "Estacionamento"
  billing_mode   = "PROVISIONED"
  read_capacity  = 10
  write_capacity = 10
  hash_key       = "TicketId"
  range_key      = "PagamentoRealizado"

  attribute {
    name = "TicketId"
    type = "S"
  }

  attribute {
    name = "PagamentoRealizado"
    type = "S"
  }

  attribute {
    name = "DataEntrada"
    type = "S"
  }

  ttl {
    attribute_name = "TimeToExist"
    enabled        = false
  }

global_secondary_index {
  name               = "estacionamento-por-status-data-indice"
  hash_key           = "PagamentoRealizado"
  range_key          = "DataEntrada"
  write_capacity     = 10
  read_capacity      = 10
  projection_type    = "INCLUDE"
  non_key_attributes = ["UserId"]
}
}


resource "aws_lambda_function" "lambda_read_from_sqs" {
  function_name = "lambda-read-from-sqs"
  handler       = "com.fiap.techChallenge3.listenerSQSWriteDynamo.SQSListener::handleRequest"
  runtime       = "java17"
  role          = aws_iam_role.lambda_role_write_to_sqs.arn
  filename      = "" #trocar pelo caminho do jar da lambda correspondente
  memory_size   = 256
  timeout       = 30
}

resource "aws_lambda_event_source_mapping" "event_source_mapping" {
  event_source_arn = aws_sqs_queue.park_register_queue.arn
  enabled          = true
  function_name    = aws_lambda_function.lambda_read_from_sqs.arn
  batch_size       = 1
}








