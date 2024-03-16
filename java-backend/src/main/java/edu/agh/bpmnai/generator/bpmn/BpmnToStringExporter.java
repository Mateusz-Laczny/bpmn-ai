package edu.agh.bpmnai.generator.bpmn;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.v2.Graph;
import edu.agh.bpmnai.generator.v2.GraphToStringExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BpmnToStringExporter {

    private final BpmnToGraphExporter bpmnToGraphExporter;

    private final GraphToStringExporter graphToStringExporter;

    @Autowired
    public BpmnToStringExporter(BpmnToGraphExporter bpmnToGraphExporter, GraphToStringExporter graphToStringExporter) {
        this.bpmnToGraphExporter = bpmnToGraphExporter;
        this.graphToStringExporter = graphToStringExporter;
    }

    public String export(BpmnModel model) {
        Graph modelGraph = bpmnToGraphExporter.export(model);
        return graphToStringExporter.export(modelGraph);
    }
}
