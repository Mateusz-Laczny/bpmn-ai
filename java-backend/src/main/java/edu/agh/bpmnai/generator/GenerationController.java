package edu.agh.bpmnai.generator;

import edu.agh.bpmnai.generator.v2.FileExporter;
import edu.agh.bpmnai.generator.v2.LlmService;
import edu.agh.bpmnai.generator.v2.UserRequestResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/generate")
@Slf4j
public class GenerationController {

    private final LlmService llmService;

    private final FileExporter fileExporter;

    private final Path bpmnLogFilepath;

    @Autowired
    public GenerationController(
            LlmService llmService,
            FileExporter fileExporter,
            @Value("${logging.apiResponses.bpmnLogFilepath}") Path bpmnLogFilepath
    ) {
        this.llmService = llmService;
        this.fileExporter = fileExporter;
        this.bpmnLogFilepath = bpmnLogFilepath;
    }

    @PostMapping("v2/send/message")
    public UserRequestResponse sendMessage(@RequestBody TextPrompt newMessage) {
        log.info("Received request on endpoint 'v2/send/message': requestBody: {}", newMessage);
        UserRequestResponse response = llmService.getResponse(newMessage.content());
        fileExporter.exportToFile(bpmnLogFilepath, response.bpmnXml());
        return response;
    }

    @PostMapping("v2/start")
    public void startNewConversation() {
        log.info("New conversation started");
        llmService.startNewConversation();
    }
}
