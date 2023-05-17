import pkgutil

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from ai_chat.chat import MockResponseProvider
from server.dtos import TextDescription, BpmnJsonModel, convert_to_bpmn_model_dto
from server.logs import log_info

response_provider = MockResponseProvider(pkgutil.get_data(__name__, 'resources/mock_response.txt').decode('utf-8'))

app = FastAPI()
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.post('/generate/text')
def get_model(text_description: TextDescription) -> BpmnJsonModel:
    log_info('Received request')
    response = response_provider.provide_response(text_description)
    model_dto = convert_to_bpmn_model_dto(response)
    return model_dto
