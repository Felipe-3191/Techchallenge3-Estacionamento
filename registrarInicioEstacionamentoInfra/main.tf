provider "aws" {
  region = "us-east-1" 
}


resource "aws_iam_role_policy" "lambdaevent_policy" {
  name = "lambdaevent_policy"
  role = aws_iam_role.lambda_role_write_to_eventbridge.id
  policy = jsonencode({
    "Version" : "2012-10-17"
    "Statement" : [
     { 
       "Effect" : "Allow",
       "Action" : [ 
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
            "Effect": "Allow",
            "Action": [
                "dynamodb:DescribeStream",
                "dynamodb:GetRecords",
                "dynamodb:GetShardIterator",
                "dynamodb:ListStreams",
                "logs:CreateLogGroup",
                "logs:CreateLogStream",
                "logs:PutLogEvents"
            ],
            "Resource": "*"
        },
        {
            "Sid": "EventBridgeActions",
            "Effect": "Allow",
            "Action": [
                "events:*",
                "schemas:*",
                "scheduler:*",
                "pipes:*"
            ],
            "Resource": "*"
        },
        {
            "Sid": "IAMPassRoleAccessForEventBridge",
            "Effect": "Allow",
            "Action": "iam:PassRole",
            "Resource": "*",
        }
    ]
  })
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

resource "aws_iam_role" "lambda_role_write_to_eventbridge" {
  name = "lambda-role-write-to-eventbridge"

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
  filename      = "/home/felipe/estudos/fiap/TechChallenge3/TechChallenge3-RegistrarInicioEstacionamento/Lambdas/registrarRequisicaoFilaSQS/target/registroSQS-1.0-SNAPSHOT.jar" # trocar pelo caminho do jar da lambda
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

resource "aws_api_gateway_rest_api" "parquimetro_api" {
  name = "techchallenge3"
}

resource "aws_api_gateway_resource" "pagamento_resource" {
  path_part   = "estacionamento"
  parent_id   = aws_api_gateway_rest_api.parquimetro_api.root_resource_id
  rest_api_id = aws_api_gateway_rest_api.parquimetro_api.id
}

resource "aws_api_gateway_model" "parking_registration_model" {
  rest_api_id  = aws_api_gateway_rest_api.parquimetro_api.id
  name         = "registroDeEntrada"
  description  = "a JSON schema"
  content_type = "application/json"

  schema = jsonencode({
    "type":"object",
    "required": ["condutor","placaDoCarro","horariofixovar","formaPagamento"],
    "properties":{
        "condutor":{"type":"string"},
        "placaDoCarro":{"type":"string"},
        "horariofixovar":{"type":"string"},
        "formaPagamento":{"type":"string"},
        "emailContato":{"type":"string"},
        "tempoEstacionamentoFixo":{"type":"integer"} 
    },
    "required":["condutor","placaDoCarro","horariofixovar","formaPagamento"],
    "title":"registroDeEntrada"
})
}

resource "aws_api_gateway_method" "method" {
  rest_api_id   = aws_api_gateway_rest_api.parquimetro_api.id
  resource_id   = aws_api_gateway_resource.pagamento_resource.id
  http_method   = "POST"
  authorization = "NONE"
  request_models = {
    "application/json" = aws_api_gateway_model.parking_registration_model.name
  }
 request_validator_id = aws_api_gateway_request_validator.validate_post.id
}

resource "aws_api_gateway_request_validator" "validate_post" {
  name                        = "validate_post"
  rest_api_id                 = aws_api_gateway_rest_api.parquimetro_api.id
  validate_request_body       = true
  validate_request_parameters = false
}

resource "aws_api_gateway_integration" "integration" {
  rest_api_id             = aws_api_gateway_rest_api.parquimetro_api.id
  resource_id             = aws_api_gateway_resource.pagamento_resource.id
  http_method             = aws_api_gateway_method.method.http_method
  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.lambda_write_to_sqs.invoke_arn
}

resource "aws_lambda_permission" "apigw_lambda" {
  statement_id  = "AllowExecutionFromAPIGateway"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.lambda_write_to_sqs.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn = "${aws_api_gateway_rest_api.parquimetro_api.execution_arn}/*/*"
}

resource "aws_lambda_permission" "event_lambda" {
   statement_id = "AllowExecutionFromEventBridge"
   action = "lambda:InvokeFunction"
   function_name = aws_lambda_function.lambda_read_from_eventbridge.function_name
   principal     = "events.amazonaws.com"
   source_arn = "arn:aws:events:us-east-1:659214650186:rule/*"
  

}
resource "aws_dynamodb_table" "registro-estacionamento-table" {
  name           = "Estacionamento"
  billing_mode   = "PROVISIONED"
  read_capacity  = 10
  write_capacity = 10
  hash_key       = "TicketId"
  range_key      = "PagamentoRealizado"
  stream_enabled = true
  stream_view_type = "NEW_IMAGE"

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
  filename      = "/home/felipe/estudos/fiap/TechChallenge3/TechChallenge3-RegistrarInicioEstacionamento/Lambdas/listenerSQSWriteDynamoLambda/target/ListenerSQSWriteDynamo-1.0-SNAPSHOT.jar" # trocar pelo caminho do jar da lambda
  memory_size   = 256
  timeout       = 30
}

resource "aws_lambda_event_source_mapping" "sqs_read_event_source_mapping" {
  event_source_arn = aws_sqs_queue.park_register_queue.arn
  enabled          = true
  function_name    = aws_lambda_function.lambda_read_from_sqs.arn
  batch_size       = 1
}


resource "aws_lambda_function" "lambda_write_to_eventbridge" {
  function_name = "lambda-write-to-eventbridge"
  handler       = "com.fiap.techChallenge3.listenerSQSWriteDynamo.PublishEventBridge::handleRequest"
  runtime       = "java17"
  role          = aws_iam_role.lambda_role_write_to_eventbridge.arn
  filename      = "/home/felipe/estudos/fiap/TechChallenge3/TechChallenge3-RegistrarInicioEstacionamento/Lambdas/publishEventBridge/target/publishEventBridge-1.0-SNAPSHOT.jar" # trocar pelo caminho do jar da lambda
  memory_size   = 256
  timeout       = 30
  environment {
    variables = {
      LAMBDA_EVCALL_URL = aws_lambda_function.lambda_read_from_eventbridge.arn,
      LAMBDA_ROLE_ARN = aws_iam_role.lambda_role_write_to_eventbridge.arn
    }
  }

}

resource "aws_lambda_event_source_mapping" "dynamo_react_mapping" {
  event_source_arn = aws_dynamodb_table.registro-estacionamento-table.stream_arn
  function_name    = aws_lambda_function.lambda_write_to_eventbridge.function_name
  batch_size       = 10  # Adjust batch size as needed
  starting_position = "LATEST"
  enabled = true
}

resource "aws_lambda_function" "lambda_read_from_eventbridge" {
  function_name = "lambda-read-from-eventbridge"
  handler       = "com.fiap.techChallenge3.listenerEventBridgeSendMessage.ListenerEventBridge::handleRequest"
  runtime       = "java17"
  role          = aws_iam_role.lambda_role_write_to_eventbridge.arn 
  filename      = "/home/felipe/estudos/fiap/TechChallenge3/TechChallenge3-RegistrarInicioEstacionamento/Lambdas/listenerEventBridgeSendMessage/target/listenerEventBridge-1.0-SNAPSHOT.jar" # trocar pelo caminho do jar da lambda
  memory_size   = 256
  timeout       = 30
}

#========================================================================================================================================================================

resource "aws_api_gateway_resource" "calc_pagamento_resource" {
  path_part   = "calcular"
  parent_id   = aws_api_gateway_resource.pagamento_resource.id
  rest_api_id = aws_api_gateway_rest_api.parquimetro_api.id
}


resource "aws_api_gateway_model" "parking_fee_model" {
  rest_api_id  = aws_api_gateway_rest_api.parquimetro_api.id
  name         = "calculoValor"
  description  = "a JSON schema"
  content_type = "application/json"

  schema = jsonencode({
    "type":"object",
    "required": ["id"],
    "properties":{
        "id":{"type":"string"}
    },
    "title":"calculoValor"
})
}

resource "aws_api_gateway_request_validator" "validate_fee_post" {
  name                        = "validate_fee_post"
  rest_api_id                 = aws_api_gateway_rest_api.parquimetro_api.id
  validate_request_body       = true
  validate_request_parameters = false
}


resource "aws_api_gateway_method" "parking_fee_post_method" {
  rest_api_id   = aws_api_gateway_rest_api.parquimetro_api.id
  resource_id   = aws_api_gateway_resource.calc_pagamento_resource.id
  http_method   = "POST"
  authorization = "NONE"
  request_models = {
    "application/json" = aws_api_gateway_model.parking_fee_model.name
  }
 request_validator_id = aws_api_gateway_request_validator.validate_fee_post.id
}


resource "aws_api_gateway_integration" "integration_fee" {
  rest_api_id             = aws_api_gateway_rest_api.parquimetro_api.id
  resource_id             = aws_api_gateway_resource.calc_pagamento_resource.id
  http_method             = aws_api_gateway_method.parking_fee_post_method.http_method
  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.lambda_calc_parking_fee.invoke_arn
}

resource "aws_lambda_permission" "apigw_lambda_fee" {
  statement_id  = "AllowExecutionFromAPIGateway"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.lambda_calc_parking_fee.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn = "${aws_api_gateway_rest_api.parquimetro_api.execution_arn}/*/*"
}

resource "aws_lambda_function" "lambda_calc_parking_fee" {
  function_name = "lambda-calc-parking-fee"
  handler       = "com.fiap.techChallenge3.calcParkingFee.ParkingCalculator::handleRequest"
  runtime       = "java17"
  role          = aws_iam_role.lambda_role_write_to_eventbridge.arn
  filename      = "/home/felipe/estudos/fiap/TechChallenge3/TechChallenge3-RegistrarInicioEstacionamento/Lambdas/calcParkingFee/target/calcParkingFee-1.0-SNAPSHOT.jar" # trocar pelo caminho do jar da lambda
  memory_size   = 256
  timeout       = 30
}

