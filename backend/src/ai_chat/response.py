import openai

from ai_chat import example_messages
from ai_chat.message import Message, MessageType


class OpenAIResponseProvider:
    example_prompts: list[Message]

    def __init__(self, example_prompts: list[Message]):
        self.example_prompts = example_prompts.copy()

    def provide_response(self, task_text):
        messages_for_model = self.example_prompts.copy()
        messages_for_model.append(Message(MessageType.USER, prompt_text=task_text))

        response_object = openai.ChatCompletion.create(
            model="gpt-3.5-turbo",
            messages=[{'role': prompt.message_type.value, 'content': prompt.contents} for prompt in messages_for_model],
            temperature=0.2
        )

        return response_object.choices[0].message.content


class MockResponseProvider:
    mock_response: str

    def __init__(self):
        self.example_prompts = example_messages.copy()
        with open('../ai_chat/resources/happy_path_response.txt') as f:
            self.mock_response = f.read()

    def provide_response(self, _task_prompt):
        return self.mock_response
