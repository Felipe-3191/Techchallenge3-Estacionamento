package com.fiap.techChallenge3.calcParkingFee;


import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TestClass {
     private static final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
     public static void main(String args[]) {

          DynamoDB dynamoDB = new DynamoDB(client);

          Table table = dynamoDB.getTable("Estacionamento");

          QuerySpec spec = new QuerySpec()
                  .withKeyConditionExpression("TicketId = :v_id")
                  .withValueMap(new ValueMap()
                          .withString(":v_id", "e2fb74b7-efaa-4181-964a-41a54e82e6d1"));

          ItemCollection<QueryOutcome> items = table.query(spec);


          Iterator<Item> iterator = items.iterator();
          Item item = null;
          while (iterator.hasNext()) {
               item = iterator.next();
               if (!item.getString("horariofixovar").equalsIgnoreCase("fixo")) {
                    Double valorEstacionamento = calcularValorEstacionamento(item.getString("DataEntrada"), 7.0);
                    item.withDouble("valorEstacionamento", valorEstacionamento);
                    PutItemOutcome outcome = table.putItem(item);
               }

               System.out.println(item.toJSONPretty());
          }
     }

     private static Double calcularValorEstacionamento(String dataInicialIso, Double valor) {

          Instant dataEntrada = Instant.parse(dataInicialIso);
          Instant dataAtual = Instant.now();
          Duration duration = Duration.between(dataAtual, dataEntrada);
          long qtdeHoras = duration.toHours();
          double valorEstacionamento = qtdeHoras > 0 ? valor * qtdeHoras : valor;


          return valorEstacionamento;
     }
     }

