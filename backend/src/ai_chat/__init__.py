import os
import pkgutil

import openai

from ai_chat.message import Message, MessageType

openai.api_key = os.getenv("OPENAI_API_KEY")

example_messages = [
    Message(MessageType.SYSTEM, pkgutil.get_data(__name__, 'resources/system_prompt.txt').decode('utf-8')),
    Message(MessageType.USER, pkgutil.get_data(__name__, '/resources/happy_path.txt').decode('utf-8')),
    Message(MessageType.ASSISTANT, pkgutil.get_data(__name__, 'resources/happy_path_response.txt').decode('utf-8')),
    Message(MessageType.USER, pkgutil.get_data(__name__, '/resources/possible_problems.txt').decode('utf-8')),
    Message(MessageType.ASSISTANT,
            pkgutil.get_data(__name__, '/resources/possible_problems_response.txt').decode('utf-8'))
]
