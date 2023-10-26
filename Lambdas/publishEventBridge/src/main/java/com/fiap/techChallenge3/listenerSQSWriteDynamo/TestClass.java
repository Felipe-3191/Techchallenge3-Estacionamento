package com.fiap.techChallenge3.listenerSQSWriteDynamo;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class TestClass {

     public static void main(String args[]) {
          Instant horaInicioEstacionamento = Instant.now();
          Instant horarioAlerta = horaInicioEstacionamento.plus(5, ChronoUnit.MINUTES);
          LocalDateTime horarioLocal = LocalDateTime.ofInstant(horarioAlerta, ZoneId.of("America/Sao_Paulo"));
          String cronExpression = String.format("at(%s)", horarioLocal.toString());
          System.out.println(cronExpression);

     }
     }

