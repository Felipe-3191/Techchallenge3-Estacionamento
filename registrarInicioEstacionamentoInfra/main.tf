provider "aws" {
  region = "us-east-1" # Replace with your desired AWS region
}


resource "aws_iam_role_policy" "lambdasqs_policy" {
  name = "lambdasqs_policy"
  role = aws_iam_role.lambda_role_write_to_sqs.id
  policy = jsonencode({
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "sqs:SendMessage",
                "sqs:ReceiveMessage",
                "sqs:DeleteMessage",
                "sqs:GetQueueAttributes",
                "logs:CreateLogGroup",
                "logs:CreateLogStream",
                "logs:PutLogEvents"
            ],
            "Resource": "*"
        }
    ]
})
}



# Create an IAM role for the first Lambda function
resource "aws_iam_role" "lambda_role_write_to_sqs" {
  name = "lambda-role-write-to-sqs"

  assume_role_policy = jsonencode({
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "sts:AssumeRole"
            ],
            "Principal": {
                "Service": [
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
  filename      = "/home/felipe/estudos/fiap/registrarPagamentoLambdas/registrarRequisicaoFilaSQS/target/registroSQS-1.0-SNAPSHOT.jar" # Replace with the path to your Lambda deployment package
  memory_size = 256
  timeout = 30
  environment {
    variables = {
      SQS_QUEUE_URL = aws_sqs_queue.example_queue.id
    }
  }
}


resource "aws_sqs_queue" "example_queue" {
  name                       = "example-queue"
  delay_seconds              = 0
  max_message_size           = 256000
  message_retention_seconds  = 345600 # 4 days
  visibility_timeout_seconds = 30
  fifo_queue                 = false
}


