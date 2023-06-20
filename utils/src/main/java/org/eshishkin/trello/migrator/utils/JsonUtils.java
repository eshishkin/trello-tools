package org.eshishkin.trello.migrator.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        var sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sssX");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        MAPPER.setDateFormat(sdf);
    }

    @SneakyThrows
    public static <T> T parse(String data, Class<T> type) {
        return MAPPER.readValue(data, type);
    }

    @SneakyThrows
    public static String serialize(Object object) {
        return MAPPER.writeValueAsString(object);
    }
}
