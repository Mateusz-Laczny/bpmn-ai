import os
from enum import Enum

import openai
from pydantic import BaseModel

openai.api_key = os.getenv("OPENAI_API_KEY")


class PromptType(Enum):
    SYSTEM = 'system'
    USER = 'user'
    ASSISTANT = 'assistant'


class Prompt:
    prompt_type: PromptType
    text: str

    def __init__(self, prompt_type: PromptType, *, path_to_prompt=None, prompt_text=None):
        if path_to_prompt is not None:
            with open(path_to_prompt, 'r') as file:
                self.text = file.read()

        self.prompt_type = prompt_type

        if prompt_text is not None:
            self.text = prompt_text


class OpenAIResponseProvider:
    example_prompts: list[Prompt]

    def __init__(self, example_prompts: list[Prompt]):
        self.example_prompts = example_prompts.copy()

    def provide_response(self, task_text):
        messages_for_model = self.example_prompts.copy()
        messages_for_model.append(Prompt(PromptType.USER, prompt_text=task_text))

        response_object = openai.ChatCompletion.create(
            model="gpt-3.5-turbo",
            messages=[{'role': prompt.prompt_type.value, 'content': prompt.text} for prompt in messages_for_model],
            temperature=0.2
        )

        return response_object.choices[0].message.content


class MockResponseProvider:
    mock_response: str

    def __init__(self):
        self.example_prompts = example_prompts.copy()
        with open('../resources/happy_path_response.txt') as f:
            self.mock_response = f.read()

    def provide_response(self, _task_prompt):
        return self.mock_response


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


example_prompts = [
    Prompt(PromptType.SYSTEM, path_to_prompt='../resources/system_prompt.txt'),
    Prompt(PromptType.USER, path_to_prompt='../resources/happy_path.txt'),
    Prompt(PromptType.ASSISTANT, path_to_prompt='../resources/happy_path_response.txt'),
    Prompt(PromptType.USER, path_to_prompt='../resources/possible_problems.txt'),
    Prompt(PromptType.ASSISTANT, path_to_prompt='../resources/possible_problems_response.txt')
]

response_provider = MockResponseProvider()


def extract_json_from_response(model_response: str) -> str:
    json_start_index = model_response.find('{')
    json_end_index = model_response.rfind('}')
    return model_response[json_start_index:json_end_index + 1]


def get_model(text_description: str):
    response = response_provider.provide_response(text_description)
    return response


if __name__ == '__main__':
    example_prompt = """
    The description is: Adding a feature to the plan of a wireless carrier is supposed to take
    no longer than one hour. If it takes longer, we want to notify
    the customer with the expected completion time.
    After successful adding a plan, the customer account should be updated.
    First, create a BPMN model in JSON format for the process happy path:
    """
    print(get_model(example_prompt))
