package edu.agh.bpmnai.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.agh.bpmnai.generator.bpmn.model.BpmnFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/generate")
public class GenerationController {

    private final BpmnProvider bpmnProvider;

    @Autowired
    public GenerationController(BpmnProvider bpmnProvider) {
        this.bpmnProvider = bpmnProvider;
    }

    @PostMapping("/from/text")
    public BpmnFile generateFromTextPrompt(@RequestBody TextPrompt prompt) throws JsonProcessingException {
        //return bpmnProvider.provideForTextPrompt(prompt);
        return new BpmnFile("""
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                                
                <definitions xmlns:camunda="http://camunda.org/schema/1.0/bpmn" id="definitions_e130ac2a-8326-4e5a-8f52-0f5ee5928567"
                             targetNamespace="http://camunda.org/examples" xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL">
                    <process id="orderProcess" name="Order Process">
                        <startEvent id="startEvent">
                            <outgoing>startEvent-receiveOrder</outgoing>
                        </startEvent>
                        <userTask camunda:assignee="" id="receiveOrder" name="Receive Order">
                            <incoming>startEvent-receiveOrder</incoming>
                            <outgoing>receiveOrder-checkCredit</outgoing>
                        </userTask>
                        <userTask camunda:assignee="" id="checkCredit" name="Check Buyer's Credit">
                            <incoming>receiveOrder-checkCredit</incoming>
                            <outgoing>checkCredit-fulfillOrder</outgoing>
                        </userTask>
                        <userTask camunda:assignee="" id="fulfillOrder" name="Fulfill Order">
                            <incoming>checkCredit-fulfillOrder</incoming>
                            <outgoing>fulfillOrder-sendInvoice</outgoing>
                            <outgoing>fulfillOrder-problemGateway</outgoing>
                        </userTask>
                        <userTask camunda:assignee="" id="sendInvoice" name="Send Invoice">
                            <incoming>fulfillOrder-sendInvoice</incoming>
                            <incoming>handleProblem-sendInvoice</incoming>
                            <outgoing>sendInvoice-endEvent</outgoing>
                        </userTask>
                        <endEvent id="endEvent">
                            <incoming>sendInvoice-endEvent</incoming>
                            <incoming>cancelOrder-endEvent</incoming>
                        </endEvent>
                        <sequenceFlow id="startEvent-receiveOrder" name="" sourceRef="startEvent" targetRef="receiveOrder"/>
                        <sequenceFlow id="receiveOrder-checkCredit" name="" sourceRef="receiveOrder" targetRef="checkCredit"/>
                        <sequenceFlow id="checkCredit-fulfillOrder" name="" sourceRef="checkCredit" targetRef="fulfillOrder"/>
                        <sequenceFlow id="fulfillOrder-sendInvoice" name="" sourceRef="fulfillOrder" targetRef="sendInvoice"/>
                        <sequenceFlow id="sendInvoice-endEvent" name="" sourceRef="sendInvoice" targetRef="endEvent"/>
                        <exclusiveGateway id="problemGateway">
                            <incoming>fulfillOrder-problemGateway</incoming>
                            <outgoing>problemGateway-handleProblem</outgoing>
                            <outgoing>problemGateway-cancelOrder</outgoing>
                        </exclusiveGateway>
                        <userTask camunda:assignee="" id="handleProblem" name="Handle Problem">
                            <incoming>problemGateway-handleProblem</incoming>
                            <outgoing>handleProblem-sendInvoice</outgoing>
                        </userTask>
                        <userTask camunda:assignee="" id="cancelOrder" name="Cancel Order">
                            <incoming>problemGateway-cancelOrder</incoming>
                            <outgoing>cancelOrder-endEvent</outgoing>
                        </userTask>
                        <sequenceFlow id="fulfillOrder-problemGateway" name="" sourceRef="fulfillOrder" targetRef="problemGateway"/>
                        <sequenceFlow id="problemGateway-handleProblem" name="" sourceRef="problemGateway" targetRef="handleProblem"/>
                        <sequenceFlow id="problemGateway-cancelOrder" name="" sourceRef="problemGateway" targetRef="cancelOrder"/>
                        <sequenceFlow id="handleProblem-sendInvoice" name="" sourceRef="handleProblem" targetRef="sendInvoice"/>
                        <sequenceFlow id="cancelOrder-endEvent" name="" sourceRef="cancelOrder" targetRef="endEvent"/>
                    </process>
                </definitions>
                """);
    }
}
