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
