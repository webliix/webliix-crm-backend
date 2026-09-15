package com.webliix.finance.util;

import java.time.Year;

public class InvoiceNumberGenerator {

    public static String currentPrefix() {
        return "INV-" + Year.now().getValue() + "-";
    }

    public static String next(String previousInvoiceNumber) {
        if (previousInvoiceNumber == null || previousInvoiceNumber.isBlank()) {
            return currentPrefix() + "000001";
        }
        String seq = previousInvoiceNumber.substring(previousInvoiceNumber.lastIndexOf('-') + 1);
        try {
            long value = Long.parseLong(seq);
            return currentPrefix() + String.format("%06d", value + 1);
        } catch (NumberFormatException ex) {
            return currentPrefix() + "000001";
        }
    }
}
