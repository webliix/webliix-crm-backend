package com.webliix.finance.util;

import java.time.Year;

public class PaymentNumberGenerator {

    public static String currentPrefix() {
        return "PAY-" + Year.now().getValue() + "-";
    }

    public static String next(String current) {
        if (current == null || !current.contains("-")) {
            return currentPrefix() + "000001";
        }
        String suffix = current.substring(current.lastIndexOf('-') + 1);
        try {
            int value = Integer.parseInt(suffix);
            return currentPrefix() + String.format("%06d", value + 1);
        } catch (NumberFormatException ex) {
            return currentPrefix() + "000001";
        }
    }
}
