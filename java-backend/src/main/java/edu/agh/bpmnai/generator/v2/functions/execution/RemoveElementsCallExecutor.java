package edu.agh.bpmnai.generator.v2.functions.execution;

import edu.agh.bpmnai.generator.bpmn.BpmnManagedReference;
import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.HumanReadableId;
import edu.agh.bpmnai.generator.bpmn.model.RemoveActivityError;
import edu.agh.bpmnai.generator.datatype.Result;
import edu.agh.bpmnai.generator.v2.functions.RemoveElementsFunction;
import edu.agh.bpmnai.generator.v2.functions.ToolCallArgumentsParser;
import edu.agh.bpmnai.generator.v2.functions.parameter.RemoveElementsFunctionCallDto;
import edu.agh.bpmnai.generator.v2.session.SessionStateStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RemoveElementsCallExecutor implements FunctionCallExecutor {

    private final ToolCallArgumentsParser callArgumentsParser;

    private final SessionStateStore sessionStateStore;

    @Autowired
    public RemoveElementsCallExecutor(
            ToolCallArgumentsParser callArgumentsParser,
            SessionStateStore sessionStateStore
    ) {
        this.callArgumentsParser = callArgumentsParser;
        this.sessionStateStore = sessionStateStore;
    }

    @Override
    public String getFunctionName() {
        return RemoveElementsFunction.FUNCTION_NAME;
    }

    @Override
    public Result<String, String> executeCall(String callArgumentsJson, BpmnManagedReference modelReference) {
        Result<RemoveElementsFunctionCallDto, String> argumentsParsingResult = callArgumentsParser.parseArguments(
                callArgumentsJson, RemoveElementsFunctionCallDto.class);
        if (argumentsParsingResult.isError()) {
            return Result.error(argumentsParsingResult.getError());
        }

        RemoveElementsFunctionCallDto callArguments = argumentsParsingResult.getValue();

        BpmnModel model = modelReference.getCurrentValue();
        StringBuilder removedElementsMessageBuilder = new StringBuilder("Following elements were removed:\n");
        StringBuilder missingElementsMessageBuilder = new StringBuilder(
                "Following elements are not present in the diagram:\n");
        for (HumanReadableId elementToRemove : callArguments.elementsToRemove()) {
            String elementToRemoveId = elementToRemove.id();
            if (!model.doesIdExist(elementToRemoveId)) {
                missingElementsMessageBuilder.append(elementToRemove).append(", ");
            } else {
                Result<Void, RemoveActivityError> removeFlowNodeResult =
                        model.removeFlowNode(elementToRemoveId);
                if (removeFlowNodeResult.isOk()) {
                    removedElementsMessageBuilder.append(elementToRemove).append(", ");
                } else {
                    log.warn(
                            "Unexpected error '{}' when removing element with model ID '{}'",
                            removeFlowNodeResult.getError(),
                            elementToRemove
                    );
                }
            }
        }

        modelReference.setValue(model);

        return Result.ok(removedElementsMessageBuilder.append('\n').append(missingElementsMessageBuilder).toString());
    }
}
