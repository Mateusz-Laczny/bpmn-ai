import Graph from 'graphology';
import random from 'graphology-layout/random';
import noverlap from 'graphology-layout-noverlap';
import { v4 as uuidv4 } from 'uuid';

const graphOptions = {
  allowSelfLoops: true,
  multi: false,
  type: 'directed',
};

const xmlParser = new DOMParser();

export function layoutBPMN(bpmnXML) {
  const graph = new Graph(graphOptions);
  const xmlDoc = xmlParser.parseFromString(bpmnXML, 'text/xml');
  const processElement = xmlDoc.querySelector('process');

  const processElementChildren = processElement.children;
  for (let index = 0; index < processElementChildren.length; index++) {
    const childNode = processElementChildren[index];
    switch (childNode.nodeName) {
      case 'startEvent':
        graph.addNode(childNode.getAttribute('id'), { type: 'startEvent' });
        break;
      case 'userTask':
        graph.addNode(childNode.getAttribute('id'), { type: 'userTask' });
        break;
      case 'exclusiveGateway':
        graph.addNode(childNode.getAttribute('id'), {
          type: 'exclusiveGateway',
        });
        break;
      case 'endEvent':
        graph.addNode(childNode.getAttribute('id'), { type: 'endEvent' });
        break;
      case 'sequenceFlow':
        graph.addDirectedEdge(
          childNode.getAttribute('sourceRef'),
          childNode.getAttribute('targetRef'),
          { bpmnElementId: childNode.getAttribute('id') }
        );
    }
  }

  // We first need to generate some positions for nodes, for the noverlap layouter to work
  random.assign(graph);
  noverlap.assign(graph, {
    settings: {
      ratio: 2,
      margin: 150,
    },
  });

  const diagramElement = xmlDoc.createElement('bpmndi:BPMNDiagram');
  const bpmnPlaneElement = xmlDoc.createElement('bpmndi:BPMNPlane');
  bpmnPlaneElement.setAttribute(
    'bpmnElement',
    processElement.getAttribute('id')
  );
  bpmnPlaneElement.setAttribute('id', 'id-' + generateUUID());
  diagramElement.appendChild(bpmnPlaneElement);

  graph.forEachNode((node, attributes) => {
    console.log(node, attributes);
    let bpmnShapeElement;
    let boundsElement;
    switch (attributes.type) {
      case 'startEvent':
        bpmnShapeElement = xmlDoc.createElement('bpmndi:BPMNShape');
        bpmnShapeElement.setAttribute('bpmnElement', node);
        bpmnShapeElement.setAttribute('id', 'id-' + generateUUID());

        boundsElement = xmlDoc.createElement('dc:Bounds');
        boundsElement.setAttribute('height', 30.0);
        boundsElement.setAttribute('width', 30.0);
        boundsElement.setAttribute('x', attributes.x);
        boundsElement.setAttribute('y', attributes.y);

        bpmnShapeElement.appendChild(boundsElement);
        bpmnPlaneElement.appendChild(bpmnShapeElement);
        break;
      case 'userTask':
        bpmnShapeElement = xmlDoc.createElement('bpmndi:BPMNShape');
        bpmnShapeElement.setAttribute('bpmnElement', node);
        bpmnShapeElement.setAttribute('id', 'id-' + generateUUID());

        boundsElement = xmlDoc.createElement('dc:Bounds');
        boundsElement.setAttribute('height', 68.0);
        boundsElement.setAttribute('width', 83.0);
        boundsElement.setAttribute('x', attributes.x);
        boundsElement.setAttribute('y', attributes.y);

        bpmnShapeElement.appendChild(boundsElement);
        bpmnPlaneElement.appendChild(bpmnShapeElement);
        break;
      case 'exclusiveGateway':
        bpmnShapeElement = xmlDoc.createElement('bpmndi:BPMNShape');
        bpmnShapeElement.setAttribute('bpmnElement', node);
        bpmnShapeElement.setAttribute('id', 'id-' + generateUUID());

        boundsElement = xmlDoc.createElement('dc:Bounds');
        boundsElement.setAttribute('height', 42.0);
        boundsElement.setAttribute('width', 42.0);
        boundsElement.setAttribute('x', attributes.x);
        boundsElement.setAttribute('y', attributes.y);

        bpmnShapeElement.appendChild(boundsElement);
        bpmnPlaneElement.appendChild(bpmnShapeElement);
        break;
      case 'endEvent':
        bpmnShapeElement = xmlDoc.createElement('bpmndi:BPMNShape');
        bpmnShapeElement.setAttribute('bpmnElement', node);
        bpmnShapeElement.setAttribute('id', 'id-' + generateUUID());

        boundsElement = xmlDoc.createElement('dc:Bounds');
        boundsElement.setAttribute('height', 30.0);
        boundsElement.setAttribute('width', 30.0);
        boundsElement.setAttribute('x', attributes.x);
        boundsElement.setAttribute('y', attributes.y);

        bpmnShapeElement.appendChild(boundsElement);
        bpmnPlaneElement.appendChild(bpmnShapeElement);
        break;
    }

    const bpmnLabelElement = xmlDoc.createElement('bpmndi:BPMNLabel');
    bpmnShapeElement.appendChild(bpmnLabelElement);
  });

  graph.forEachEdge(
    (edge, attributes, source, target, sourceAttributes, targetAttributes) => {
      const bpmnEdgeElement = xmlDoc.createElement('bpmndi:BPMNEdge');
      bpmnEdgeElement.setAttribute('bpmnElement', attributes.bpmnElementId);
      bpmnEdgeElement.setAttribute('id', 'id-' + generateUUID());

      const sourceDIWaypointElement = xmlDoc.createElement('di:waypoint');
      sourceDIWaypointElement.setAttribute('x', sourceAttributes.x);
      sourceDIWaypointElement.setAttribute('y', sourceAttributes.y);

      const targetDIWaypointElement = xmlDoc.createElement('di:waypoint');
      targetDIWaypointElement.setAttribute('x', targetAttributes.x);
      targetDIWaypointElement.setAttribute('y', targetAttributes.y);

      bpmnEdgeElement.appendChild(sourceDIWaypointElement);
      bpmnEdgeElement.appendChild(targetDIWaypointElement);

      const bpmnLabelElement = xmlDoc.createElement('bpmndi:BPMNLabel');
      bpmnEdgeElement.appendChild(bpmnLabelElement);

      bpmnPlaneElement.appendChild(bpmnEdgeElement);
    }
  );

  const definitionsElement = xmlDoc.querySelector('definitions');
  definitionsElement.appendChild(diagramElement);

  const xmlSerializer = new XMLSerializer();
  const xmlString = xmlSerializer.serializeToString(xmlDoc);
  console.log(xmlString);
  return xmlString;
}

function generateUUID() {
  return uuidv4();
}
