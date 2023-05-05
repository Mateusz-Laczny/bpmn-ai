from ai_chat.chat import MockResponseProvider

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
