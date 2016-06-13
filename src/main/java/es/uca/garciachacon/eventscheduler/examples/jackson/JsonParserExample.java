package es.uca.garciachacon.eventscheduler.examples.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

public class JsonParserExample {
    public static void main(String[] args) throws IOException {
        String carJson = "{\"brand\":\"Mercedes\",\"doors\":5}";

        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(carJson);

        while (!parser.isClosed()) {
            JsonToken token = parser.nextToken();

            //System.out.println("jsonToken = " + token);

            if (JsonToken.FIELD_NAME.equals(token)) {
                String fieldName = parser.getCurrentName();
                System.out.println("fieldName = " + fieldName);

                token = parser.nextToken();

                if ("brand".equals(fieldName))
                    System.out.println("value = " + parser.getValueAsString());
                else
                    System.out.println("value = " + parser.getValueAsInt());
            }
        }
    }
}