import json
import pkgutil

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import parse_obj_as

from ai_chat.chat import MockResponseProvider
from server.logs import log_info
from server.model import TextDescription, BPMNModel

response_provider = MockResponseProvider(pkgutil.get_data(__name__, 'resources/mock_response.txt').decode('utf-8'))

app = FastAPI()

origins = ["*"]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.post('/generate/text')
def get_model(text_description: TextDescription) -> BPMNModel:
    log_info('Received request')
    response = response_provider.provide_response(text_description)
    response_as_dict = json.loads(response)
    return parse_obj_as(BPMNModel, response_as_dict)
