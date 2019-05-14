package com.dfn.watchdog.commons.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Format numbers, date, etc.. for viewing.
 */
public class Formatters {
    private static final Logger logger = LoggerFactory.getLogger(Formatters.class);
    private static final ObjectMapper jsonMapper = new ObjectMapper();

    private Formatters() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    private static final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("HH:mm:ss");
    private static final DecimalFormat decimalFormatter = new DecimalFormat("###,###.##");
    private static final DecimalFormat decimalFormatterNoFractions = new DecimalFormat("###,###");

    public static String format(long date) {
        return dateFormatter.print(date);
    }

    public static String format(double number) {
        return decimalFormatter.format(number);
    }

    public static String formatWithoutFractions(double number) {
        return decimalFormatterNoFractions.format(number);
    }

    public static ObjectMapper getJsonMapper() {
        return jsonMapper;
    }

    public static String toJsonString(Object object) {
        String jsonString;
        try {
            jsonString = jsonMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse object into json", e);
            jsonString = "{}";
        }
        return jsonString;
    }

    public static Map<String, Object> jsonStringToMap(String jsonString) {
        Map<String, Object> jsonMap;
        try {
            jsonMap = jsonMapper.readValue(jsonString, Map.class);
        } catch (IOException e) {
            logger.error("Failed to parse json string to map", e);
            jsonMap = new HashMap<>(1);
        }
        return jsonMap;
    }
}
