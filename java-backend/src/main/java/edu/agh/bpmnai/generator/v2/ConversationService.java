package edu.agh.bpmnai.generator.v2;

import edu.agh.bpmnai.generator.TextPrompt;
import edu.agh.bpmnai.generator.v2.functions.ChatFunctionDto;
import edu.agh.bpmnai.generator.v2.session.SessionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

import static edu.agh.bpmnai.generator.openai.OpenAIFunctionParametersSchemaFactory.getSchemaForParametersDto;
import static edu.agh.bpmnai.generator.v2.session.SessionStatus.IN_PROGRESS;
import static edu.agh.bpmnai.generator.v2.session.SessionStatus.NEW;

@Service
@Slf4j
public class ConversationService {

    public static final ChatFunctionDto IS_REQUEST_DESCRIPTION_DETAILED_ENOUGH = ChatFunctionDto.builder()
            .name("is_request_description_detailed_enough")
            .description("Checks if the user's request is detailed enough and asks for more details if necessary.")
            .parameters(getSchemaForParametersDto(UserDescriptionReasoningDto.class))
            .build();
    private static final Set<ChatFunctionDto> chatFunctions = Set.of(
            IS_REQUEST_DESCRIPTION_DETAILED_ENOUGH,
            ChatFunctionDto.builder()
                    .name("add_sequence_of_activities")
                    .description("Adds a sequence of activities to the model, executed in a linear fashion (one after the other).")
                    .parameters(getSchemaForParametersDto(SequenceOfActivitiesDto.class))
                    .build(),
            ChatFunctionDto.builder()
                    .name("add_single_choice_fork_between_activities")
                    .description("Adds a fork to the model, where one path has to be chosen from several alternatives. After the fork, the paths converge on a single point, from which the process is continued.")
                    .parameters(getSchemaForParametersDto(SingleChoiceForkDto.class))
                    .build(),
            ChatFunctionDto.builder()
                    .name("add_parallel_activities_fork")
                    .description("Adds a fork to the model, where two or more activities have to be executed, which can be executed at the same time. After the fork, the paths converge on a single point, from which the process is continued.")
                    .parameters(getSchemaForParametersDto(ParallelForkDto.class))
                    .build(),
            ChatFunctionDto.builder()
                    .name("add_while_loop")
                    .description("Adds a while loop to the model, where one or more activities can be executed multiple times, based on a condition")
                    .parameters(getSchemaForParametersDto(WhileLoopDto.class))
                    .build(),
            ChatFunctionDto.builder()
                    .name("add_if_else_branching")
                    .description("Adds an if-else branching to the model. If the condition is true, one branch is executed, if not another branch will be executed")
                    .parameters(getSchemaForParametersDto(IfElseBranchingDto.class))
                    .build(),
            ChatFunctionDto.builder()
                    .name("remove_activity")
                    .description("Removes an activity from the model. All activity predecessors will be connected to the activity successor")
                    .parameters(getSchemaForParametersDto(RemoveActivityDto.class))
                    .build()
    );
    private final LlmService llmService;
    private final SessionStateStorage sessionStateStorage;

    public ConversationService(LlmService llmService, SessionStateStorage sessionStateStorage) {
        this.llmService = llmService;
        this.sessionStateStorage = sessionStateStorage;
    }

    public SessionState newMessageReceived(TextPrompt newMessage) {
        SessionState currentSessionState = sessionStateStorage.getCurrentState();
        currentSessionState.appendUserMessage(newMessage.content());
        if (currentSessionState.sessionStatus() == NEW) {
            llmService.getResponse(currentSessionState, Set.of(IS_REQUEST_DESCRIPTION_DETAILED_ENOUGH));
            currentSessionState.setSessionStatus(IN_PROGRESS);
        } else {
            llmService.getResponse(currentSessionState, chatFunctions);
        }

        return currentSessionState;
    }
}
