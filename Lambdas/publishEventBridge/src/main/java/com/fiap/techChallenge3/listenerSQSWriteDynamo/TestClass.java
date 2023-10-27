package com.fiap.techChallenge3.listenerSQSWriteDynamo;


import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.EventBridgeException;
import software.amazon.awssdk.services.eventbridge.model.PutRuleRequest;
import software.amazon.awssdk.services.eventbridge.model.PutRuleResponse;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;

public class TestClass {

     public static void main(String args[]) {
          DateTimeFormatter awsDateTime = (new DateTimeFormatterBuilder().parseCaseInsensitive().
                  append(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                  .appendLiteral('T')
                  .append(DateTimeFormatter.ofPattern("HH:mm:ss"))).toFormatter();



          Region region = Region.US_EAST_1;
          EventBridgeClient eventBridgeClient = EventBridgeClient.builder()
                  .region(region)
                  .build();


          Instant horaInicioEstacionamento = Instant.now();
          Instant horarioAlerta = horaInicioEstacionamento.plus(5, ChronoUnit.MINUTES);
          LocalDateTime horarioLocal = LocalDateTime.ofInstant(horarioAlerta, ZoneId.of("America/Sao_Paulo"));

//          String cronExpression = String.format("at(%s)", horarioLocal.format(awsDateTime));
          String cronExpression = String.format("cron(%s %s %s %s %s %s)",
                  horarioLocal.getMinute(),
                  horarioLocal.getHour(),
                  horarioLocal.getDayOfMonth(),
                  horarioLocal.getMonth().getValue(),
                  "?",
                  horarioLocal.getYear()
          );




          System.out.println(cronExpression);


          createEBRule(eventBridgeClient, "testerulename", cronExpression);


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



     }

