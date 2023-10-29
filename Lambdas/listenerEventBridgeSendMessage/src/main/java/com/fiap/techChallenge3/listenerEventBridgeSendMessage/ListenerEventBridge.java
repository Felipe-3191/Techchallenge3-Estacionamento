package com.fiap.techChallenge3.listenerEventBridgeSendMessage;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.*;

import java.util.List;
import java.util.Map;

public class ListenerEventBridge implements RequestHandler<Map<String, String>, Void>{


    @Override
    public Void handleRequest(Map<String, String> eventInput, Context context) {
        LambdaLogger logger = context.getLogger();

        Region region = Region.US_EAST_1;
        EventBridgeClient eventBridgeClient = EventBridgeClient.builder()
                .region(region)
                .build();

        String ruleName = eventInput.get("ruleName");


// deletar os targets
        ListTargetsByRuleRequest request = ListTargetsByRuleRequest.builder()
                .rule(ruleName)
                .build();

        ListTargetsByRuleResponse response = eventBridgeClient.listTargetsByRule(request);
        List<Target> allTargets = response.targets();

        for (Target myTarget:allTargets) {
            RemoveTargetsRequest removeTargetsRequest = RemoveTargetsRequest.builder()
                    .rule(ruleName)
                    .ids(myTarget.id())
                    .build();

            eventBridgeClient.removeTargets(removeTargetsRequest);

        }
//deletar a rule
        DeleteRuleRequest ruleRequest = DeleteRuleRequest.builder()
                        .name(ruleName)
                        .build();

        eventBridgeClient.deleteRule(ruleRequest);
        logger.log("Lambda chamada através do EventBridge");


        eventInput.forEach(
                (detailName, detailValue) -> {
                    logger.log(detailName + "-" + detailValue.toString());
                }

        );

        return null;
    }
}
