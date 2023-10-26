package com.fiap.techChallenge3.registroSQS;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fiap.techChallenge3.registroSQS.model.APIGatewayResponse;
import com.fiap.techChallenge3.registroSQS.model.RegistroEstacionamentoModel;
import com.google.gson.Gson;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
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
            RegistroEstacionamentoModel registroEstacionamentoModel = gson.fromJson(messageBody, RegistroEstacionamentoModel.class);
            registroEstacionamentoModel.setId(UUID.randomUUID().toString());


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

        } catch (ParseException  pex) {
            responseJson.put("statusCode", 400);
            responseJson.put("exception", pex);

        }

        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write(responseJson.toString());
        writer.close();
    }
}
