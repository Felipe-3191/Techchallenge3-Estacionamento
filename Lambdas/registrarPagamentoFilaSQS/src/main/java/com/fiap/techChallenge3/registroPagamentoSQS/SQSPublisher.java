package com.fiap.techChallenge3.registroPagamentoSQS;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fiap.techChallenge3.registroPagamentoSQS.model.RegistroPagamentoModel;
import com.google.gson.Gson;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.UUID;

public class SQSPublisher implements RequestStreamHandler{


    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        final String queueUrl = System.getenv("SQS_QUEUE_URL");
        JSONParser parser = new JSONParser();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        JSONObject responseJson = new JSONObject();

        LambdaLogger logger = context.getLogger();
        logger.log("1 stop");

        try {
            JSONObject event = (JSONObject) parser.parse(reader);
            String messageBody = event.get("body").toString();

            logger.log("msgBody: " + messageBody);

            AmazonSQS sqs = AmazonSQSClientBuilder.standard()
                    .withRegion("us-east-1")
                    .build();

            logger.log("2 stop");


            Gson gson = new Gson();
            RegistroPagamentoModel registroEstacionamentoModel = gson.fromJson(messageBody, RegistroPagamentoModel.class);




            SendMessageRequest sendMessageRequest = new SendMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withMessageBody(gson.toJson(registroEstacionamentoModel))
                    .withDelaySeconds(1);

            logger.log("3 stop");

            SendMessageResult sendMessageResult = sqs.sendMessage(sendMessageRequest);

            logger.log("4 stop");

            System.out.println("Message sent. Message ID: " + sendMessageResult.getMessageId());


            JSONObject responseBody = new JSONObject();
            responseBody.put("id", registroEstacionamentoModel.getId());

            responseJson.put("statusCode", 200);
            responseJson.put("body", responseBody.toString());


        } catch (Exception e) {
            responseJson.put("statusCode", 400);
            responseJson.put("body", e.getMessage());

        } finally {
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
            writer.write(responseJson.toString());
            writer.close();
        }
    }
}
