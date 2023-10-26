package com.fiap.techChallenge3.listenerEventBridgeSendMessage.model;

public class APIGatewayResponse {

    private Boolean isBase64Encoded;

    private Integer statusCode;



    private String body;


    public Boolean getIsBase64Encoded() {
        return isBase64Encoded;
    }

    public void setIsBase64Encoded(Boolean isBase64Encoded) {
        this.isBase64Encoded = isBase64Encoded;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }



    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
