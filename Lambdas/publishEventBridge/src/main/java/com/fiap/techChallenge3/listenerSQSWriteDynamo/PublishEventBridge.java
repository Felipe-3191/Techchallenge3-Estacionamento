package com.fiap.techChallenge3.listenerSQSWriteDynamo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;

import java.util.Map;

public class PublishEventBridge implements RequestHandler<DynamodbEvent, Void> {

    private final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
    private String Dynamo_DB_TABLE_NAME = "Estacionamento";
    DynamoDB dynamoDB = new DynamoDB(client);
    @Override
    public Void handleRequest(DynamodbEvent dynamodbEvent, Context context) {

        LambdaLogger logger = context.getLogger();

        Table table = dynamoDB.getTable(Dynamo_DB_TABLE_NAME);

        for (DynamodbEvent.DynamodbStreamRecord record : dynamodbEvent.getRecords()) {

            Map<String, AttributeValue> dadosInseridos = record.getDynamodb().getNewImage();

            logger.log(dadosInseridos.get("TicketId").toString());
            logger.log(dadosInseridos.get("PagamentoRealizado").toString());
            logger.log(dadosInseridos.get("DataEntrada").toString());
            logger.log(dadosInseridos.get("horariofixovar").toString());
            logger.log(dadosInseridos.get("condutor").toString());
            logger.log(dadosInseridos.get("placaDoCarro").toString());
            logger.log(dadosInseridos.get("formaPagamento").toString());

        }


        return null;
    }



}
