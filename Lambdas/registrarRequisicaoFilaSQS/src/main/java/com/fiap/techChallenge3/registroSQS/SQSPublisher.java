package com.fiap.techChallenge3.registroSQS;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class SQSPublisher implements RequestHandler<Map<String,Object>, String> {

    @Override
    public String handleRequest(Map<String,Object> input, Context context) {



        final String queueUrl = System.getenv("SQS_QUEUE_URL");
        String messageBody = input.get("body").toString();
        LambdaLogger logger = context.getLogger();
        logger.log("1 stop");

        logger.log("msgBody: " + messageBody);

        AmazonSQS sqs = AmazonSQSClientBuilder.standard()
                .withRegion("us-east-1")
                .build();

        logger.log("2 stop");

        SendMessageRequest sendMessageRequest = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(messageBody)
                .withDelaySeconds(1);

        logger.log("3 stop");

        SendMessageResult sendMessageResult = sqs.sendMessage(sendMessageRequest);

        logger.log("4 stop");

        System.out.println("Message sent. Message ID: " + sendMessageResult.getMessageId());


        // Create a response map
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 200); // Status code (e.g., 200 for success)

        // Create headers
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json"); // JSON content type
        response.put("headers", headers);

        // Create a response body
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Hello, World!"); // Your response data
        response.put("body", responseBody);

        return "ok";





    }
}
