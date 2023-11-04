package com.fiap.techChallenge3.listenerSQSWriteDynamo.model;

public class RegistroPagamentoModel {

    private String id;

    private Double valoraPagar;

    public Double getValoraPagar() {
        return valoraPagar;
    }

    public void setValoraPagar(Double valoraPagar) {
        this.valoraPagar = valoraPagar;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
