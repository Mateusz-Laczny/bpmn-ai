package edu.agh.bpmnai.generator.bpmn.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static edu.agh.bpmnai.generator.bpmn.model.ModificationType.ADD;
import static edu.agh.bpmnai.generator.bpmn.model.ModificationType.REMOVE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LoggingModelModificationChangelogTest {

    LoggingModelModificationChangelog subscriber;

    BpmnModel model;

    @BeforeEach
    void setUp() {
        model = new BpmnModel();
    }

    @Test
    void adds_log_on_task_added() {
        String taskId = model.addTask("task");
        assertEquals(
                Set.of(new NodeModificationLog(
                        1,
                        ADD,
                        new HumanReadableId("task", taskId),
                        BpmnNodeType.TASK.asString()
                )),
                model.getChangeLogSnapshot().nodeModificationLogs()
        );
    }

    @Test
    void adds_log_on_task_removed() {
        String taskId = model.addTask("task");
        model.removeFlowNode(taskId);
        assertEquals(
                Set.of(
                        new NodeModificationLog(
                                1,
                                ADD,
                                new HumanReadableId("task", taskId),
                                BpmnNodeType.TASK.asString()
                        ),
                        new NodeModificationLog(
                                2,
                                REMOVE,
                                new HumanReadableId("task", taskId),
                                BpmnNodeType.TASK.asString()
                        )
                ),
                model.getChangeLogSnapshot().nodeModificationLogs()
        );
    }

    @Test
    void adds_log_on_xor_gateway_added() {
        String gatewayId = model.addGateway(BpmnGatewayType.EXCLUSIVE, "gateway");
        assertEquals(
                Set.of(new NodeModificationLog(
                        1,
                        ADD,
                        new HumanReadableId("gateway", gatewayId),
                        BpmnNodeType.XOR_GATEWAY.asString()
                )),
                model.getChangeLogSnapshot().nodeModificationLogs()
        );
    }

    @Test
    void adds_log_on_xor_gateway_removed() {
        String gatewayId = model.addGateway(BpmnGatewayType.EXCLUSIVE, "gateway");
        model.removeFlowNode(gatewayId);
        assertEquals(Set.of(
                new NodeModificationLog(
                        1,
                        ADD,
                        new HumanReadableId("gateway", gatewayId),
                        BpmnNodeType.XOR_GATEWAY.asString()
                ),
                new NodeModificationLog(
                        2,
                        REMOVE,
                        new HumanReadableId("gateway", gatewayId),
                        BpmnNodeType.XOR_GATEWAY.asString()
                )
        ), model.getChangeLogSnapshot().nodeModificationLogs());
    }

    @Test
    void adds_log_on_parallel_gateway_added() {
        String gatewayId = model.addGateway(BpmnGatewayType.PARALLEL, "gateway");
        assertEquals(
                Set.of(new NodeModificationLog(
                        1,
                        ADD,
                        new HumanReadableId("gateway", gatewayId),
                        BpmnNodeType.PARALLEL_GATEWAY.asString()
                )),
                model.getChangeLogSnapshot().nodeModificationLogs()
        );
    }

    @Test
    void adds_log_on_parallel_gateway_removed() {
        String gatewayId = model.addGateway(BpmnGatewayType.PARALLEL, "gateway");
        model.removeFlowNode(gatewayId);
        assertEquals(Set.of(
                new NodeModificationLog(
                        1,
                        ADD,
                        new HumanReadableId("gateway", gatewayId),
                        BpmnNodeType.PARALLEL_GATEWAY.asString()
                ),
                new NodeModificationLog(
                        2,
                        REMOVE,
                        new HumanReadableId("gateway", gatewayId),
                        BpmnNodeType.PARALLEL_GATEWAY.asString()
                )
        ), model.getChangeLogSnapshot().nodeModificationLogs());
    }

    @Test
    void adds_log_on_start_event_added() {
        String eventId = model.addLabelledStartEvent("Start1");
        assertEquals(
                Set.of(new NodeModificationLog(
                        1,
                        ADD,
                        new HumanReadableId("Start1", eventId),
                        BpmnNodeType.START_EVENT.asString()
                )),
                model.getChangeLogSnapshot().nodeModificationLogs()
        );
    }

    @Test
    void adds_log_on_start_event_removed() {
        String eventId = model.addLabelledStartEvent("Start1");
        model.removeFlowNode(eventId);
        assertEquals(Set.of(
                new NodeModificationLog(
                        1,
                        ADD,
                        new HumanReadableId("Start1", eventId),
                        BpmnNodeType.START_EVENT.asString()
                ),
                new NodeModificationLog(
                        2,
                        REMOVE,
                        new HumanReadableId("Start1", eventId),
                        BpmnNodeType.START_EVENT.asString()
                )
        ), model.getChangeLogSnapshot().nodeModificationLogs());
    }

    @Test
    void adds_log_on_end_event_added() {
        String eventId = model.addEndEvent();
        assertEquals(
                Set.of(new NodeModificationLog(
                        1,
                        ADD,
                        new HumanReadableId("End", eventId),
                        BpmnNodeType.END_EVENT.asString()
                )),
                model.getChangeLogSnapshot().nodeModificationLogs()
        );
    }

    @Test
    void adds_log_on_end_event_removed() {
        String eventId = model.addEndEvent();
        model.removeFlowNode(eventId);
        assertEquals(
                Set.of(
                        new NodeModificationLog(
                                1,
                                ADD,
                                new HumanReadableId("End", eventId),
                                BpmnNodeType.END_EVENT.asString()
                        ),
                        new NodeModificationLog(
                                2,
                                REMOVE,
                                new HumanReadableId("End", eventId),
                                BpmnNodeType.END_EVENT.asString()
                        )
                ),
                model.getChangeLogSnapshot().nodeModificationLogs()
        );
    }

    @Test
    void adds_log_on_added_sequence_flow() {
        String sourceId = model.addTask("source");
        String targetId = model.addTask("target");
        model.addUnlabelledSequenceFlow(sourceId, targetId);
        assertEquals(
                Set.of(new FlowModificationLog(
                        3,
                        ADD,
                        new HumanReadableId("source", sourceId),
                        new HumanReadableId("target", targetId)
                )),
                model.getChangeLogSnapshot().flowModificationLogs()
        );
    }

    @Test
    void adds_log_on_removed_sequence_flow() {
        String sourceId = model.addTask("source");
        String targetId = model.addTask("target");
        model.addUnlabelledSequenceFlow(sourceId, targetId);
        model.removeSequenceFlow(sourceId, targetId);
        assertEquals(
                Set.of(new FlowModificationLog(
                        3,
                        ADD,
                        new HumanReadableId("source", sourceId),
                        new HumanReadableId("target", targetId)
                ), new FlowModificationLog(
                        4,
                        REMOVE,
                        new HumanReadableId("source", sourceId),
                        new HumanReadableId("target", targetId)
                )),
                model.getChangeLogSnapshot().flowModificationLogs()
        );
    }
}