package com.fiap.techChallenge3.listenerSQSWriteDynamo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import org.joda.time.Instant;

import java.util.HashMap;
import java.util.Map;

public class SQSListener implements RequestHandler<SQSEvent, Void> {

    private final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
    private String Dynamo_DB_TABLE_NAME = "Estacionamento";
    DynamoDB dynamoDB = new DynamoDB(client);
    @Override
    public Void handleRequest(SQSEvent sqsEvent, Context context) {
        Table table = dynamoDB.getTable(Dynamo_DB_TABLE_NAME);

        for (SQSEvent.SQSMessage msg : sqsEvent.getRecords()) {
            String messageBody = msg.getBody();
            System.out.println("Mensagem Recebida: " + messageBody);




           Item item = new Item()
                            .withPrimaryKey("TicketId", "34faslpwqeoirufasklj")
                            .withString("PagamentoRealizado", "N")
                            .withString("DataEntrada", Instant.now().toString()
                                    );


            PutItemOutcome outcome = table.putItem(item);


        }


        return null;
    }



}
