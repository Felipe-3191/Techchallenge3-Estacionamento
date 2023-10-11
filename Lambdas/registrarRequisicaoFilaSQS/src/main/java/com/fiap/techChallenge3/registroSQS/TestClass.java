package com.fiap.techChallenge3.registroSQS;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

public class TestClass {

     public static void main(String args[]) {
          final String queueUrl = "https://sqs.us-east-1.amazonaws.com/659214650186/example-queue";


          AmazonSQS sqs = AmazonSQSClientBuilder.standard()
                  .withRegion("us-east-1")
                  .build();


          SendMessageRequest sendMessageRequest = new SendMessageRequest()
                  .withQueueUrl(queueUrl)
                  .withMessageBody("message to sqs")
                  .withDelaySeconds(1);


          SendMessageResult sendMessageResult = sqs.sendMessage(sendMessageRequest);


          System.out.println("Message sent. Message ID: " + sendMessageResult.getMessageId());


     }
     }

