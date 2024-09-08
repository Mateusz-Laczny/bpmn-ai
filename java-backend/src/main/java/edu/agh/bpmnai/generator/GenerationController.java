package edu.agh.bpmnai.generator;

import edu.agh.bpmnai.generator.v2.FileExporter;
import edu.agh.bpmnai.generator.v2.LlmService;
import edu.agh.bpmnai.generator.v2.UserRequestResponse;
import edu.agh.bpmnai.generator.v2.session.ImmutableSessionState;
import edu.agh.bpmnai.generator.v2.session.NewSessionInfo;
import edu.agh.bpmnai.generator.v2.session.SessionService;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/llm2bpmn")
@Slf4j
public class GenerationController {

    private final LlmService llmService;

    private final FileExporter fileExporter;

    private final Path bpmnLogFilepath;

    private final SessionService sessionService;

    private final SessionStateStore sessionStateStore;

    @Autowired
    public GenerationController(
            LlmService llmService,
            FileExporter fileExporter,
            @Value("${logging.apiResponses.bpmnLogFilepath}") Path bpmnLogFilepath,
            SessionService sessionService, SessionStateStore sessionStateStore
    ) {
        this.llmService = llmService;
        this.fileExporter = fileExporter;
        this.bpmnLogFilepath = bpmnLogFilepath;
        this.sessionService = sessionService;
        this.sessionStateStore = sessionStateStore;
    }

    @PostMapping("sessions/create")
    public NewSessionInfo createNewSession(@RequestBody NewSessionRequest request) {
        ImmutableSessionState newSessionState = sessionService.initializeNewSession(request);
        sessionStateStore.saveSessionState(newSessionState);
        return new NewSessionInfo(newSessionState.sessionId());
    }

    @PostMapping("sessions/{sessionId}/prompts/add")
    public void addPrompt(@PathVariable String sessionId, @RequestBody TextPrompt newPrompt) {
        Optional<ImmutableSessionState> sessionState = sessionStateStore.getSessionState(sessionId);
        if (sessionState.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "session not found"
            );
        }

        sessionStateStore.saveSessionState(sessionService.addPromptToContext(newPrompt.content(), sessionState.get()));
    }

    @PostMapping("sessions/{sessionId}/completions/generate")
    public UserRequestResponse sendMessage(
            @PathVariable String sessionId
    ) {
        UserRequestResponse response = llmService.getResponse(sessionId);
        fileExporter.exportToFile(bpmnLogFilepath, response.bpmnXml());
        return response;
    }

}
