import BpmnModeler from 'bpmn-js/lib/Modeler';
import 'bpmn-js/dist/assets/diagram-js.css';
import 'bpmn-js/dist/assets/bpmn-js.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css';
import './style.css';
import { layoutBPMN } from './layouting';

const MODEL_FETCH_URL = 'http://localhost:8080/generate/from/text';

const modeler = new BpmnModeler({
  container: '#canvas',
  keyboard: {
    bindTo: window,
  },
});

const promptTextarea = document.getElementById('prompt-input-textarea');
const overlay = document.getElementById('overlay');

async function fetchModel(prompt) {
  let response = await fetch(MODEL_FETCH_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      content: prompt,
    }),
  });

  return await response.json();
}

function generateModel() {
  const prompt = promptTextarea.value;
  fetchModel(prompt)
    .then((response) => response.xml)
    .then((bpmnXML) => {
      const bpmnXMLAfterLayout = layoutBPMN(bpmnXML);
      return modeler.importXML(bpmnXMLAfterLayout);
    })
    .then(() => modeler.get('canvas').zoom('fit-viewport'));
}

const promptInputButton = document.getElementById('prompt-input-button');

function onPromptInputButtonClick() {
  showOverlay();
  generateModel();
  hideOverlay();
}

function showOverlay() {
  overlay.style.display = 'block';
}

function hideOverlay() {
  overlay.style.display = 'none';
}

promptInputButton.addEventListener('click', onPromptInputButtonClick);
