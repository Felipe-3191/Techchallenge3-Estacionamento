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
import org.json.simple.JSONObject;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

public class PublishEventBridge implements RequestHandler<DynamodbEvent, Void> {

    private final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
    private String Dynamo_DB_TABLE_NAME = "Estacionamento";
    DynamoDB dynamoDB = new DynamoDB(client);

    private final String lambdaARN = System.getenv("LAMBDA_EVCALL_URL");;
    private final String lambdaRoleARN = System.getenv("LAMBDA_ROLE_ARN");
    private final String targetId = UUID.randomUUID().toString();


    @Override
    public Void handleRequest(DynamodbEvent dynamodbEvent, Context context) {

        Region region = Region.US_EAST_1;
        EventBridgeClient eventBridgeClient = EventBridgeClient.builder()
                .region(region)
                .build();



        LambdaLogger logger = context.getLogger();
        logger.log("lambdaARN: " + lambdaARN);
        logger.log("lambdaRoleARN: " + lambdaRoleARN);

        Table table = dynamoDB.getTable(Dynamo_DB_TABLE_NAME);

        for (DynamodbEvent.DynamodbStreamRecord record : dynamodbEvent.getRecords()) {

            Map<String, AttributeValue> dadosInseridos = record.getDynamodb().getNewImage();

            //montar nome da regra (removendo whitespaces que podem vir a existir)
            String ruleName = dadosInseridos.get("condutor").getS().replaceAll("\\s", "") + "-"
                    + dadosInseridos.get("placaDoCarro").getS().replaceAll("\\s", "")
                    + "-" + dadosInseridos.get("TicketId").getS().substring(0,6);

            Instant horaInicioEstacionamento = Instant.parse(dadosInseridos.get("DataEntrada").getS());
            Instant horarioAlerta = horaInicioEstacionamento.plus(5, ChronoUnit.MINUTES);
            LocalDateTime horarioLocal = LocalDateTime.ofInstant(horarioAlerta, ZoneId.of("UTC"));


            String cronExpression = String.format("cron(%s %s %s %s %s %s)",
                    horarioLocal.getMinute(),
                    horarioLocal.getHour(),
                    horarioLocal.getDayOfMonth(),
                    horarioLocal.getMonth().getValue(),
                    "?",
                    horarioLocal.getYear()
            );




            JSONObject json = new JSONObject();
            json.put("ruleName", ruleName);
            json.put("TickedId", dadosInseridos.get("TicketId").getS());
            json.put("horariofixovar", dadosInseridos.get("horariofixovar").getS());
            json.put("condutor", dadosInseridos.get("condutor").getS());
            json.put("placaDoCarro", dadosInseridos.get("placaDoCarro").getS());

            createEBRule(eventBridgeClient, ruleName, cronExpression);
            putRuleTarget(eventBridgeClient, ruleName, lambdaARN, lambdaRoleARN, json.toJSONString(), targetId);

            //            logger.log(dadosInseridos.get("TicketId").getS());
//            logger.log(dadosInseridos.get("PagamentoRealizado").getS());
//            logger.log(dadosInseridos.get("DataEntrada").getS());
//            logger.log(dadosInseridos.get("horariofixovar").getS());
//            logger.log(dadosInseridos.get("condutor").getS());
//            logger.log(dadosInseridos.get("placaDoCarro").getS());
//            logger.log(dadosInseridos.get("formaPagamento").getS());

        }


        return null;
    }


    private static void createEBRule (EventBridgeClient eventBridgeClient, String ruleName, String cronExpression) {
        try {
            PutRuleRequest ruleRequest = PutRuleRequest.builder()
                    .name(ruleName)
                    .eventBusName("default")
                    .scheduleExpression(cronExpression)
                    .state("ENABLED")
                    .description("Lambda que avisará o fim do período de estacionamento")
                    .build();


            PutRuleResponse ruleResponse = eventBridgeClient.putRule(ruleRequest);
            System.out.println("o ARN da nova regra será  "+ ruleResponse.ruleArn());
        } catch (EventBridgeException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }


    private static void putRuleTarget(EventBridgeClient eventBrClient, String ruleName, String lambdaARN, String lambdaRoleArn, String json, String targetId ) {
            try {

                Target lambdaTarget = Target.builder()
                        .arn(lambdaARN)
                        .id(targetId)
                        .input(json)
                        .build();


                PutTargetsRequest targetsRequest = PutTargetsRequest.builder()
                        .eventBusName("default")
                        .rule(ruleName)
                        .targets(lambdaTarget)
                        .build();

                eventBrClient.putTargets(targetsRequest);
                System.out.println("The "+lambdaARN + " was successfully used as a target");


            } catch (EventBridgeException e) {
                System.err.println(e.awsErrorDetails().errorMessage());
                System.exit(1);
            }

    }

}
