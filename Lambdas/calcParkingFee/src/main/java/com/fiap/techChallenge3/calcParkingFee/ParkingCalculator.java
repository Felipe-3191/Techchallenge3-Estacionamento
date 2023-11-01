package com.fiap.techChallenge3.calcParkingFee;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import com.fiap.techChallenge3.calcParkingFee.model.RegistroEstacionamentoModel;
import com.fiap.techChallenge3.calcParkingFee.model.RegistroEstacionamentoModelMin;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ParkingCalculator implements RequestStreamHandler{

    private static final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();


    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        JSONParser parser = new JSONParser();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        JSONObject responseJson = new JSONObject();

        LambdaLogger logger = context.getLogger();
        logger.log("1 stop");

        try {
            JSONObject event = (JSONObject) parser.parse(reader);
            String messageBody = event.get("body").toString();

            logger.log("msgBody: " + messageBody);

            Gson gson = new Gson();
            RegistroEstacionamentoModelMin registroEstacionamentoModel = gson.fromJson(messageBody, RegistroEstacionamentoModelMin.class);


            //aqui preciso inserir a busca no banco

            DynamoDB dynamoDB = new DynamoDB(client);

            Table table = dynamoDB.getTable("Estacionamento");

            QuerySpec spec = new QuerySpec()
                    .withKeyConditionExpression("TicketId = :v_id")
                    .withValueMap(new ValueMap()
                            .withString(":v_id", "a493495d-1874-4b14-ac17-47dc5062da16"));

            ItemCollection<QueryOutcome> items = table.query(spec);

            if (items.getAccumulatedItemCount() <=0) {
                throw new Exception("Registro referente ao id: " + registroEstacionamentoModel.getId() + " não encontrado");
            }

            JsonObject responseBody = new JsonObject();
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

            responseJson.put("statusCode", 200);
            responseJson.put("body", responseBody.toString());

        } catch (ParseException pex) {
            responseJson.put("statusCode", 400);
            responseJson.put("body", pex.getMessage());

        } catch (Exception e) {
            responseJson.put("statusCode", 400);
            responseJson.put("body", e.getMessage());

        } finally {
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
            writer.write(responseJson.toString());
            writer.close();
        }
    }


    private Double calcularValorEstacionamento(String dataInicialIso, Double valor) {

        Instant dataEntrada = Instant.parse(dataInicialIso);
        Instant dataAtual = Instant.now();
        Duration duration = Duration.between(dataAtual, dataEntrada);
        long qtdeHoras = duration.get(ChronoUnit.HOURS);
        double valorEstacionamento = qtdeHoras > 0 ? valor * qtdeHoras : valor;


        return valorEstacionamento;
    }
}
