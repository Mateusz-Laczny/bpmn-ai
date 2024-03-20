package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.RemoveElementFunction;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.RemoveElementDto;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RemoveElementCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    private final SessionStateStore sessionStateStore;

    @Autowired
    public RemoveElementCallExecutor(ToolCallArgumentsParser callArgumentsParser, SessionStateStore sessionStateStore) {
        this.callArgumentsParser = callArgumentsParser;
        this.sessionStateStore = sessionStateStore;
    }

    @Override
    public String getFunctionName() {
        return RemoveElementFunction.FUNCTION_NAME;
    }

    @Override
    public Result<String, List<String>> executeCall(String callArgumentsJson) {
        Result<RemoveElementDto, List<String>> argumentsParsingResult = callArgumentsParser.parseArguments(callArgumentsJson, RemoveElementDto.class);
        if (argumentsParsingResult.isError()) {
            return Result.error(argumentsParsingResult.getError());
        }

        RemoveElementDto callArguments = argumentsParsingResult.getValue();

        BpmnModel model = sessionStateStore.model();
        Optional<String> elementToCutOutId = model.findElementByModelFriendlyId(callArguments.elementToRemove());

        elementToCutOutId.ifPresent(model::cutOutElement);
        return Result.ok("");
    }
}
