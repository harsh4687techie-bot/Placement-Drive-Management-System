package com.placement.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvHelperTest {

    @Test
    @DisplayName("Plain field without special characters should remain as-is")
    void testPlainField() {
        assertEquals("CSE", CsvHelper.escapeCsvField("CSE"));
        assertEquals("9.5", CsvHelper.escapeCsvField(9.5));
    }

    @Test
    @DisplayName("Field with commas should be wrapped in double quotes")
    void testFieldWithComma() {
        assertEquals("\"Java, Python, C++\"", CsvHelper.escapeCsvField("Java, Python, C++"));
    }

    @Test
    @DisplayName("Field with double quotes should double up internal quotes and wrap in quotes")
    void testFieldWithDoubleQuotes() {
        assertEquals("\"Engineer \"\"Full Stack\"\"\"", CsvHelper.escapeCsvField("Engineer \"Full Stack\""));
    }

    @Test
    @DisplayName("Field with newline should be enclosed in quotes")
    void testFieldWithNewline() {
        assertEquals("\"Line 1\nLine 2\"", CsvHelper.escapeCsvField("Line 1\nLine 2"));
    }

    @Test
    @DisplayName("Null field should be rendered as empty string")
    void testNullField() {
        assertEquals("", CsvHelper.escapeCsvField(null));
    }

    @Test
    @DisplayName("Row generation should escape each element and join with commas")
    void testRowGeneration() {
        List<Object> values = Arrays.asList("21CS001", "Aarav, Sharma", 9.42, "Java \"Expert\"");
        String row = CsvHelper.toCsvRow(values);
        assertEquals("21CS001,\"Aarav, Sharma\",9.42,\"Java \"\"Expert\"\"\"", row);
    }
}
