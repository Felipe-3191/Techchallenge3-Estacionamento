package com.fiap.techChallenge3.listenerSQSWriteDynamo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
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
import com.fiap.techChallenge3.listenerSQSWriteDynamo.model.RegistroEstacionamentoModel;
import com.fiap.techChallenge3.listenerSQSWriteDynamo.model.RegistroPagamentoModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.joda.time.Instant;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class SQSListener implements RequestHandler<SQSEvent, Void> {

    private final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
    private String Dynamo_DB_TABLE_NAME = "Estacionamento";
    DynamoDB dynamoDB = new DynamoDB(client);
    @Override
    public Void handleRequest(SQSEvent sqsEvent, Context context) {

        LambdaLogger logger = context.getLogger();

        Table table = dynamoDB.getTable(Dynamo_DB_TABLE_NAME);

        for (SQSEvent.SQSMessage msg : sqsEvent.getRecords()) {
            String messageBody = msg.getBody();

            Gson gson = new Gson();
            RegistroPagamentoModel registroPagamentoModel = gson.fromJson(messageBody, RegistroPagamentoModel.class);

            QuerySpec spec = new QuerySpec()
                    .withKeyConditionExpression("TicketId = :v_id")
                    .withValueMap(new ValueMap()
                            .withString(":v_id", registroPagamentoModel.getId()));

            ItemCollection<QueryOutcome> items = table.query(spec);


            JsonObject responseBody = new JsonObject();
            Iterator<Item> iterator = items.iterator();
            if (!iterator.hasNext()) {

                try {
                    throw new Exception("Registro referente ao id: " + registroPagamentoModel.getId() + " não encontrado");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            Item item = null;
            while (iterator.hasNext()) {
                item = iterator.next();
                item.withString("PagamentoRealizado", "S");
                PutItemOutcome outcome = table.putItem(item);
                logger.log("Pagamento do item " + registroPagamentoModel.getId() + " no valor de: " + registroPagamentoModel.getValoraPagar() +" realizado com sucesso!");
            }


        }


        return null;
    }



}
