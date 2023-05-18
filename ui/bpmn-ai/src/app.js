import pizzaDiagram from 'bundle-text:./pizza-example.bpmn';
import { BpmnVisualization } from "bpmn-visualization";
import "./style.css"

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

fetchModel().then(model => {
  console.log(model);
});

try{
  const viewer = new BpmnVisualization({
  container: 'viewer-container'
});
  console.log(viewer);
  viewer.load(pizzaDiagram, { fit: { type: "Center", margin: 50 } });
} catch(e) {
  console.log(e);
}