package com.fiap.techChallenge3.listenerEventBridgeSendMessage;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ListenerEventBridge implements RequestHandler<ScheduledEvent, Void>{


    @Override
    public Void handleRequest(ScheduledEvent event, Context context) {

        JSONParser parser = new JSONParser();
        JSONObject responseJson = new JSONObject();

        LambdaLogger logger = context.getLogger();

        logger.log("Lambda chamada através do EventBridge");
        logger.log(event.getId());
        logger.log(event.getDetailType());
        event.getDetail().forEach(
                (detailName, detailValue) -> {
                    logger.log(detailName + "-" + detailValue.toString());
                }

        );


        return null;
    }
}
