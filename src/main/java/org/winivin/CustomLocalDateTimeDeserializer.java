package org.winivin;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomLocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

    private final static Logger logger = LoggerFactory.getLogger(CustomLocalDateTimeDeserializer.class);

    public CustomLocalDateTimeDeserializer() {
        this(null);
    }

    private CustomLocalDateTimeDeserializer(Class<LocalDateTime> t) {
        super(t);
    }

    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String date = jsonParser.getText();
        try {
            return LocalDateTime.parse(date, DateTimeFormatter.ofPattern(Constants.format));
        } catch (Exception ex) {
            logger.debug("Error while parsing date: {} ", date, ex);
            throw new RuntimeException("Cannot Parse Date");
        }
    }
}