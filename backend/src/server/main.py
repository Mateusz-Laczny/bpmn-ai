import json
import pkgutil

from fastapi import FastAPI
from pydantic import parse_obj_as

from ai_chat.chat import MockResponseProvider
from server.model import TextDescription, BPMNModel

response_provider = MockResponseProvider(pkgutil.get_data(__name__, 'resources/mock_response.txt').decode('utf-8'))

app = FastAPI()


@app.post('/generate/text')
def get_model(text_description: TextDescription) -> BPMNModel:
    response = response_provider.provide_response(text_description)
    response_as_dict = json.loads(response)
    return parse_obj_as(BPMNModel, response_as_dict)
