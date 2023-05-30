import os
from enum import Enum
from typing import Optional, List, Union

import bpmn_python.bpmn_diagram_layouter as layouter
import bpmn_python.bpmn_diagram_rep as bpmn_diagram
from pydantic import BaseModel


class TextDescription(BaseModel):
    description: str


class TaskType(Enum):
    USER_TASK = 'userTask'
    SERVICE_TASK = 'serviceTask'


class BPMNUserTask(BaseModel):
    id: str
    name: str
    type: TaskType
    assignee: str

    def _get_id_to_element_dict(self, id_to_element_dict):
        id_to_element_dict[self.id] = self


class BPMNServiceTask(BaseModel):
    id: str
    name: str
    type: TaskType
    service: str

    def _get_id_to_element_dict(self, id_to_element_dict):
        id_to_element_dict[self.id] = self


class GatewayType(Enum):
    EXCLUSIVE = 'exclusive'
    INCLUSIVE = 'inclusive'


class BPMNGateway(BaseModel):
    id: str
    name: str
    type: GatewayType

    def _get_id_to_element_dict(self, id_to_element_dict):
        id_to_element_dict[self.id] = self


class EventType(Enum):
    START = 'start'
    END = 'end'


class BPMNEvent(BaseModel):
    id: str
    name: str
    type: EventType

    def _get_id_to_element_dict(self, id_to_element_dict):
        id_to_element_dict[self.id] = self


class BPMNSequenceFlow(BaseModel):
    id: str
    sourceRef: str
    targetRef: str
    condition: Optional[str]

    def _get_id_to_element_dict(self, id_to_element_dict):
        id_to_element_dict[self.id] = self


class BPMNProcess(BaseModel):
    id: str
    name: str
    tasks: List[Union[BPMNUserTask, BPMNServiceTask]]
    gateways: List[BPMNGateway]
    events: List[BPMNEvent]
    sequenceFlows: List[BPMNSequenceFlow]

    def _get_id_to_element_dict(self, id_to_element_dict):
        id_to_element_dict[self.id] = self
        for elem in self.tasks + self.gateways + self.events + self.sequenceFlows:
            elem._get_id_to_element_dict(id_to_element_dict)

        return id_to_element_dict


class BPMNModel(BaseModel):
    process: BPMNProcess

    def get_id_to_element_dict(self):
        result = {}
        self.process._get_id_to_element_dict(result)
        return result


def convert_to_bpmn_model_dto(json_string: str):
    return BPMNModel.parse_raw(json_string)


def generate_bpmn_xml_from_model(model: BPMNModel):
    id_to_element_dict = model.get_id_to_element_dict()

    bpmn_graph = bpmn_diagram.BpmnDiagramGraph()
    bpmn_graph.create_new_diagram_graph(diagram_name="diagram")

    process_id = bpmn_graph.add_process_to_diagram(process_name=model.process.name)
    model.process.id = process_id

    for event in model.process.events:
        if event.type == EventType.START:
            [start_id, _] = bpmn_graph.add_start_event_to_diagram(process_id=process_id, start_event_name=event.name)
            event.id = start_id
        elif event.type == EventType.END:
            [end_id, _] = bpmn_graph.add_end_event_to_diagram(process_id=process_id, end_event_name=event.name)
            event.id = end_id

    for task in model.process.tasks:
        [task_id, _] = bpmn_graph.add_task_to_diagram(process_id=process_id, task_name=task.name)
        task.id = task_id

    for gateway in model.process.gateways:
        if gateway.type == GatewayType.EXCLUSIVE:
            [gateway_id, _] = bpmn_graph.add_exclusive_gateway_to_diagram(process_id=process_id,
                                                                          gateway_name=gateway.name)
            gateway.id = gateway_id
        elif gateway.type == GatewayType.INCLUSIVE:
            [gateway_id, _] = bpmn_graph.add_inclusive_gateway_to_diagram(process_id=process_id,
                                                                          gateway_name=gateway.name)
            gateway.id = gateway_id

    for sequence_flow in model.process.sequenceFlows:
        sequence_flow.sourceRef = id_to_element_dict[sequence_flow.sourceRef].id
        sequence_flow.targetRef = id_to_element_dict[sequence_flow.targetRef].id

        label = sequence_flow.condition or ''
        bpmn_graph.add_sequence_flow_to_diagram(process_id=process_id,
                                                sequence_flow_name=label,
                                                source_ref_id=sequence_flow.sourceRef,
                                                target_ref_id=sequence_flow.targetRef)

    layouter.generate_layout(bpmn_graph)
    bpmn_graph.export_xml_file(os.getcwd(), os.sep + 'result.bpmn')
    with open(os.path.join(os.getcwd(), 'result.bpmn'), 'r') as f:
        content = f.read()

    return content
