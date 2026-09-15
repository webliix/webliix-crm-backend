package com.webliix.automation.condition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SimpleConditionEvaluator implements ConditionEvaluator {

    private static final Pattern SIMPLE_COMPARISON = Pattern.compile("^(?<field>[a-zA-Z0-9_]+)\\s*(?<op>>=|<=|>|<|==|!=)\\s*(?<value>.+)$");
    private final ObjectMapper objectMapper;

    public SimpleConditionEvaluator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean evaluate(String conditionJson, Map<String, Object> context) {
        if (conditionJson == null || conditionJson.isBlank()) {
            return true;
        }

        try {
            JsonNode root = objectMapper.readTree(conditionJson);
            if (root.has("expression")) {
                String expression = root.get("expression").asText();
                return evaluateExpression(expression, context);
            }
        } catch (Exception ex) {
            return false;
        }

        return false;
    }

    private boolean evaluateExpression(String expression, Map<String, Object> context) {
        Matcher matcher = SIMPLE_COMPARISON.matcher(expression.trim());
        if (!matcher.matches()) {
            return false;
        }

        String field = matcher.group("field");
        String op = matcher.group("op");
        String valueString = matcher.group("value");

        Object fieldValue = context.get(field);
        if (fieldValue instanceof Number) {
            double actual = ((Number) fieldValue).doubleValue();
            try {
                double expected = Double.parseDouble(valueString);
                return compare(actual, expected, op);
            } catch (NumberFormatException ignore) {
                return false;
            }
        }

        if (fieldValue != null) {
            return compare(fieldValue.toString(), valueString.replaceAll("^\"|\"$", ""), op);
        }

        return false;
    }

    private boolean compare(double actual, double expected, String op) {
        return switch (op) {
            case ">" -> actual > expected;
            case "<" -> actual < expected;
            case ">=" -> actual >= expected;
            case "<=" -> actual <= expected;
            case "==" -> actual == expected;
            case "!=" -> actual != expected;
            default -> false;
        };
    }

    private boolean compare(String actual, String expected, String op) {
        return switch (op) {
            case "==" -> actual.equals(expected);
            case "!=" -> !actual.equals(expected);
            default -> false;
        };
    }
}
