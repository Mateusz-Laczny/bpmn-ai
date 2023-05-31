import BpmnModeler from 'bpmn-js/lib/Modeler';
import 'bpmn-js/dist/assets/diagram-js.css';
import 'bpmn-js/dist/assets/bpmn-js.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css';
import './style.css';

const MODEL_FETCH_URL = 'http://localhost:8000/generate/text';

const modeler = new BpmnModeler({
  container: '#canvas',
  keyboard: {
    bindTo: window,
  },
});

async function fetchModel() {
  let response = await fetch(MODEL_FETCH_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      description: 'Test',
    }),
  });
  console.log(response);
  return await response.text();
}

fetchModel()
  .then((bpmnXML) => modeler.importXML(bpmnXML))
  .then(() => modeler.get('canvas').zoom('fit-viewport'));
