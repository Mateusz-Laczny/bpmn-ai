from enum import Enum


class MessageType(Enum):
    SYSTEM = 'system'
    USER = 'user'
    ASSISTANT = 'assistant'


def from_filepath(prompt_type: MessageType, path: str):
    with open(path, 'r') as file:
        contents_from_file = file.read()
        return Message(prompt_type, contents_from_file)


class Message:
    message_type: MessageType
    contents: str

    def __init__(self, message_type: MessageType, contents: str):
        self.message_type = message_type
        self.contents = contents
