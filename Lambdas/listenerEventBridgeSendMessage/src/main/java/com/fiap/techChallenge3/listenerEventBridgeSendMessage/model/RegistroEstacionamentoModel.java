package com.fiap.techChallenge3.listenerEventBridgeSendMessage.model;

public class RegistroEstacionamentoModel {

    private String id;

    private String horariofixovar;

    private String dthEntrada;

    private String condutor;

    private String placaDoCarro;

    private String formaPagamento;

    private String pagamentoRealizado;

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
}
