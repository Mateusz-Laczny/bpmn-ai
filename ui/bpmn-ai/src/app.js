import pizzaDiagram from '../resources/pizza-example.bpmn';

import BpmnViewer from 'bpmn-js';
import "./css/style.css"

const MODEL_FETCH_URL = 'http://localhost:8000/generate/text'

async function fetchModel() {
  let response = await fetch(MODEL_FETCH_URL, {
    method: 'POST',
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      description: "Test"
    })
  });
  console.log(response);
  let data = await response.json();
  return data;
}

var viewer = new BpmnViewer({
  container: '#viewer-container'
});

fetchModel().then(model => {
  console.log(model);
  return viewer.importXML(pizzaDiagram);
}).then(function(result) {

  const { warnings } = result;

  console.log('success !', warnings);

  viewer.get('canvas').zoom('fit-viewport');
}).catch(function(err) {

  const { warnings, message } = err;

  console.log('something went wrong:', warnings, message);
});