import pizzaDiagram from '../resources/pizza-example.bpmn';

import BpmnViewer from 'bpmn-js';
import "./css/style.css"

var viewer = new BpmnViewer({
  container: '#viewer-container'
});


viewer.importXML(pizzaDiagram).then(function(result) {

  const { warnings } = result;

  console.log('success !', warnings);

  viewer.get('canvas').zoom('fit-viewport');
}).catch(function(err) {

  const { warnings, message } = err;

  console.log('something went wrong:', warnings, message);
});