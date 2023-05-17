import json

from dtos_generated import *


class TextDescription(BaseModel):
    description: str


def convert_to_bpmn_model_dto(model: str) -> BpmnJsonModel:
    model_dict = json.loads(model)
    process = TProcess()
    process.id = model_dict['process']['id']
    process.name = model_dict['process']['name']

    tasks = _parse_tasks(model_dict['process']['tasks'])
    process.task = tasks.tasks
    process.userTask = tasks.user_tasks
    process.serviceTask = tasks.service_tasks

    gateways = _parse_gateways(model_dict['process']['gateways'])
    process.exclusiveGateway = gateways.exclusive_gateways
    process.parallelGateway = gateways.parallel_gateways

    events = _parse_events(model_dict['process']['events'])
    process.startEvent = events.start_event
    process.endEvent = events.end_event
    process.intermediateCatchEvent = events.intermediate_catch_events
    process.intermediateThrowEvent = events.intermediate_throw_events

    sequence_flows = _parse_sequence_flows(model_dict['process']['sequenceFlows'])
    process.sequenceFlow = sequence_flows

    definition = TDefinitions()
    definition.process = process
    model = BpmnJsonModel()
    model.definitions = definition
    return model


class _Tasks:
    tasks: list[TTask]
    user_tasks: list[TUserTask]
    service_tasks: list[TServiceTask]

    def __init__(self) -> None:
        self.tasks = []
        self.user_tasks = []
        self.service_tasks = []


class _Gateways:
    exclusive_gateways: list[TExclusiveGateway]
    parallel_gateways: list[TParallelGateway]

    def __init__(self) -> None:
        self.parallel_gateways = []
        self.exclusive_gateways = []


class _Events:
    start_event: TStartEvent
    end_event = TEndEvent
    intermediate_catch_events: list[TIntermediateCatchEvent]
    intermediate_throw_events: list[TIntermediateThrowEvent]

    def __init__(self) -> None:
        self.intermediate_catch_events = []
        self.intermediate_throw_events = []


def _parse_gateways(gateways_as_dicts: list[dict]):
    gateways = _Gateways()

    for gateways_as_dict in gateways_as_dicts:
        gateway_type = gateways_as_dict['type']
        if gateway_type is 'exclusive':
            gateway = TExclusiveGateway()
            gateway.id = gateways_as_dict['id']
            gateway.name = gateways_as_dict['name']
            gateways.exclusive_gateways.append(gateway)
        elif gateway_type is 'parallel':
            gateway = TParallelGateway(id=gateways_as_dict['id'], name=gateways_as_dict['name'])
            gateways.parallel_gateways.append(gateway)

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


def _parse_events(events_as_dicts: list[dict]) -> _Events:
    events = _Events()
    for event_as_dict in events_as_dicts:
        event_type = event_as_dict['type']
        if event_type is 'start':
            events.start_event = TStartEvent(id=event_as_dict['id'], name=event_as_dict['name'])
        elif event_type is 'end':
            events.start_event = TEndEvent(id=event_as_dict['id'], name=event_as_dict['name'])
        elif event_type is 'intermediate':
            if bool(event_as_dict['catchEvent']):
                catch_event = TCatchEvent(id=event_as_dict['id'], name=event_as_dict['name'])
                catch_event.messageEventDefinition = \
                    TMessageEventDefinition(messageRef=event_as_dict['messageEventDefinition']['messageRef'])
                events.intermediate_catch_events.append(TIntermediateCatchEvent(__root__=catch_event))
            else:
                throw_event = TThrowEvent(id=event_as_dict['id'], name=event_as_dict['name'])
                throw_event.messageEventDefinition = \
                    TMessageEventDefinition(messageRef=event_as_dict['messageEventDefinition']['messageRef'])
                events.intermediate_throw_events.append(TIntermediateThrowEvent(__root__=throw_event))

    return events


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
        elif task_type is 'task':
            task = TTask(id=task_as_dict['id'], name=task_as_dict['name'])
            result_tasks.tasks.append(task)

    return result_tasks
