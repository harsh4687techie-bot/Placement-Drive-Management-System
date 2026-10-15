package com.placement.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;
    private static final DateTimeFormatter READABLE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String nowIso() {
        return ISO_FORMATTER.format(Instant.now());
    }

    public static String formatReadable(String isoString) {
        if (isoString == null || isoString.isBlank()) {
            return "-";
        }
        try {
            Instant instant = Instant.parse(isoString);
            LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            return READABLE_FORMATTER.format(ldt);
        } catch (Exception e) {
            return isoString;
        }
    }
}
