package edu.agh.bpmnai.generator.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class SequenceOfActivitiesDtoTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    void correctly_deserializes_from_json_string() throws JsonProcessingException {
        String jsonString = """
                {
                  "background": "The process of handling an order from a customer",
                  "predecessorActivity": "Start",
                  "newActivities": ["Receive order from customer", "Prepare pizza", "Deliver pizza to customer"]
                }""";

        SequenceOfActivitiesDto deserializedDto = mapper.readValue(jsonString, SequenceOfActivitiesDto.class);
    }
}