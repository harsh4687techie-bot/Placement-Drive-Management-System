package com.placement.util;

import java.util.List;

public class CsvHelper {

    public static String escapeCsvField(Object field) {
        if (field == null) {
            return "";
        }
        String str = field.toString();
        boolean containsSpecialChar = str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r");
        if (containsSpecialChar) {
            // Double up internal quotes and enclose in quotes
            String escaped = str.replace("\"", "\"\"");
            return "\"" + escaped + "\"";
        }
        return str;
    }

    public static String toCsvRow(List<?> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            sb.append(escapeCsvField(values.get(i)));
            if (i < values.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
}
