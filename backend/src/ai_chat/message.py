from enum import Enum


class MessageType(Enum):
    SYSTEM = 'system'
    USER = 'user'
    ASSISTANT = 'assistant'


class Message:
    message_type: MessageType
    contents: str

    def __init__(self, message_type: MessageType, contents: str):
        self.message_type = message_type
        self.contents = contents
