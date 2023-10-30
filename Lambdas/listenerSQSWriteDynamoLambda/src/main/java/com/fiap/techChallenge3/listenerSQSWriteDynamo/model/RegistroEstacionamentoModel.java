package com.fiap.techChallenge3.listenerSQSWriteDynamo.model;

public class RegistroEstacionamentoModel {


    private String id;

    private String horariofixovar;

    private String dthEntrada;

    private String condutor;

    private String placaDoCarro;

    private String formaPagamento;

    private String emailContato;

    private Integer tempoEstacionamentoFixo;


    private String pagamentoRealizado;

    public String getEmailContato() {
        return emailContato;
    }

    public void setEmailContato(String emailContato) {
        this.emailContato = emailContato;
    }

    public Integer getTempoEstacionamentoFixo() {
        return tempoEstacionamentoFixo;
    }

    public void setTempoEstacionamentoFixo(Integer tempoEstacionamentoFixo) {
        this.tempoEstacionamentoFixo = tempoEstacionamentoFixo;
    }

    public String getPagamentoRealizado() {
        return pagamentoRealizado;
    }

    public void setPagamentoRealizado(String pagamentoRealizado) {
        this.pagamentoRealizado = pagamentoRealizado;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHorariofixovar() {
        return horariofixovar;
    }

    public void setHorariofixovar(String horariofixovar) {
        this.horariofixovar = horariofixovar;
    }

    public String getDthEntrada() {
        return dthEntrada;
    }

    public void setDthEntrada(String dthEntrada) {
        this.dthEntrada = dthEntrada;
    }

    public String getCondutor() {
        return condutor;
    }

    public void setCondutor(String condutor) {
        this.condutor = condutor;
    }

    public String getPlacaDoCarro() {
        return placaDoCarro;
    }

    public void setPlacaDoCarro(String placaDoCarro) {
        this.placaDoCarro = placaDoCarro;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public Double calcularValorEstacionamento() {
        return this.getTempoEstacionamentoFixo() * 7.00;
    }
}

