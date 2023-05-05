from pydantic import BaseModel


class BPMNFlowElement(BaseModel):
    id: str
    name: str | None
    type: str


class BPMNSequenceFlow(BPMNFlowElement):
    sourceRef: str
    targetRef: str
    condition: str | None


class BPMNProcess(BaseModel):
    id: str
    name: str
    flowElements: list[BPMNFlowElement]


class BPMNModel(BaseModel):
    process: BPMNProcess
    links: list
