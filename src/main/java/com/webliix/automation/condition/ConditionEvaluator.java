package com.webliix.automation.condition;

import java.util.Map;

public interface ConditionEvaluator {
    boolean evaluate(String conditionJson, Map<String, Object> context);
}
