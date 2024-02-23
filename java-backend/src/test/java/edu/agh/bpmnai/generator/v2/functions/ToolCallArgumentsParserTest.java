package edu.agh.bpmnai.generator.v2.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.agh.bpmnai.generator.v2.UserDescriptionReasoningDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ToolCallArgumentsParserTest {

    @Test
    void correctly_parses_valid_arguments_json() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        var parser = new ToolCallArgumentsParser(mapper);
        String jsonToParse = """
                {
                  "background": "Some background",
                  "thoughts": [
                    {
                      "thoughtText": "We need to know what happens after the pizza is delivered. Is there any follow-up process or is the process complete at that point?",
                      "usefulnessScore": "5"
                    },
                    {
                      "thoughtText": "We need to know if the payment is made through a specific payment gateway or method, or if it is collected in cash on delivery.",
                      "usefulnessScore": "5"
                    },
                    {
                      "thoughtText": "We need to know if there are any additional steps involved in preparing the pizza, such as checking for allergies or preferences.",
                      "usefulnessScore": "5"
                    },
                    {
                      "thoughtText": "We need to know if there are any specific roles involved in the process, such as cooks or delivery drivers.",
                      "usefulnessScore": "5"
                    }
                  ],
                  "messageToTheUser": "Some message"
                }""";

        ArgumentsParsingResult<UserDescriptionReasoningDto> parsingResult = parser.parseArguments(jsonToParse, UserDescriptionReasoningDto.class);
        assertNotNull(parsingResult.result());
        var parsedDto = parsingResult.result();
        assertEquals("Some background", parsedDto.background());
        assertEquals("Some message", parsedDto.messageToTheUser());
    }
}