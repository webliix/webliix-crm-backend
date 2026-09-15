package com.webliix.automation.action;

import java.util.Map;

public interface AutomationAction {
    void execute(Map<String, Object> context, Map<String, Object> parameters);
}
