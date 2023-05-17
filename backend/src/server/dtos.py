import json

from dtos_generated import *


class TextDescription(BaseModel):
    description: str


class _Tasks:
    user_tasks: list[TUserTask]
    service_tasks: list[TServiceTask]

    def __init__(self) -> None:
        self.user_tasks = []
        self.service_tasks = []


class _Gateways:
    exclusive_gateways: list[TExclusiveGateway]

    def __init__(self) -> None:
        self.exclusive_gateways = []


def _parse_gateways(gateways_as_dicts: list[dict]):
    gateways = _Gateways()

    for gateways_as_dict in gateways_as_dicts:
        if gateways_as_dict['type'] is 'exclusive':
            gateway = TExclusiveGateway()
            gateway.id = gateways_as_dict['id']
            gateway.name = gateways_as_dict['name']
            gateways.exclusive_gateways.append(gateway)

    return gateways


def _parse_sequence_flows(sequence_flows_as_dicts: list[dict]) -> list[TSequenceFlow]:
    sequence_flows = []
    for sequence_flows_as_dict in sequence_flows_as_dicts:
        sequence_flow = TSequenceFlow()
        sequence_flow.id = sequence_flows_as_dict['id']
        sequence_flow.name = sequence_flows_as_dict['name']
        sequence_flow.sourceRef = sequence_flows_as_dict['sourceRef']
        sequence_flow.targetRef = sequence_flows_as_dict['targetRef']
        sequence_flow.conditionExpression = sequence_flows_as_dict['conditionExpression']

    return sequence_flows


def convert_to_bpmn_model_dto(model: str) -> BpmnJsonModel:
    model_dict = json.loads(model)
    process = TProcess()
    process.id = model_dict['process']['id']
    process.name = model_dict['process']['name']

    tasks = _parse_tasks(model_dict['process']['tasks'])
    process.userTask = tasks.user_tasks
    process.serviceTask = tasks.service_tasks

    gateways = _parse_gateways(model_dict['process']['gateways'])
    process.exclusiveGateway = gateways.exclusive_gateways

    sequence_flows = _parse_sequence_flows(model_dict['process']['sequenceFlows'])
    process.sequenceFlow = sequence_flows

    definition = TDefinitions()
    definition.process = process
    model = BpmnJsonModel()
    model.definitions = definition
    return model


def _parse_tasks(tasks_as_dicts: list[dict]) -> _Tasks:
    result_tasks = _Tasks()
    performers = {}

    for task_as_dict in tasks_as_dicts:
        task_type = task_as_dict['type']
        if task_type is 'userTask':
            task = TUserTask()

            if task_as_dict['assignee'] not in performers.keys():
                resource_role = TResourceRole()
                resource_role.name = task_as_dict['assignee']
                performer = THumanPerformer(__root__=TPerformer(__root__=resource_role))
                performers[task_as_dict['assignee']] = performer
            else:
                performer = performers[task_as_dict['assignee']]

            task.humanPerformer = performer
            task.id = task_as_dict['id']
            task.name = task_as_dict['name']

            result_tasks.user_tasks.append(task)
        elif task_type is 'serviceTask':
            task = TServiceTask()
            task.id = task_as_dict['id']
            task.name = task_as_dict['name']
            result_tasks.service_tasks.append(task)

    return result_tasks
