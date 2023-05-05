import pkgutil

import openai

from ai_chat import example_messages
from ai_chat.message import Message, MessageType


class Chat:
    response_provider = None
    message_history: list[Message]

    def __init__(self, response_provider) -> None:
        self.response_provider = response_provider
        self.message_history = example_messages

    def send_message(self, message_text: str):
        response = self.response_provider.provide_response(message_text)
        self.message_history.append(Message(MessageType.USER, message_text))
        self.message_history.append(Message(MessageType.ASSISTANT, response))
        return response


class OpenAIResponseProvider:
    example_prompts: list[Message]

    def __init__(self, example_prompts: list[Message]):
        self.example_prompts = example_prompts.copy()

    def provide_response(self, task_text):
        messages_for_model = self.example_prompts.copy()
        messages_for_model.append(Message(MessageType.USER, task_text))

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
        self.mock_response = pkgutil.get_data(__name__, 'resources/system_prompt.txt').decode('utf-8')

    def provide_response(self, _task_prompt):
        return self.mock_response
