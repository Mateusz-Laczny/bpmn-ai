package edu.agh.bpmnai.generator.bpmn.layouting;

import edu.agh.bpmnai.generator.bpmn.model.BpmnModel;
import edu.agh.bpmnai.generator.bpmn.model.Dimensions;
import edu.agh.bpmnai.generator.bpmn.model.DirectedEdge;
import jakarta.annotation.Nullable;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TopologicalSortBpmnLayouting {

    private final GridElementToDiagramPositionMapping gridElementToDiagramPositionMapping;

    public TopologicalSortBpmnLayouting(
            GridElementToDiagramPositionMapping gridElementToDiagramPositionMapping
    ) {
        this.gridElementToDiagramPositionMapping = gridElementToDiagramPositionMapping;
    }

    private static Set<String> findPathsToRemove(
            Map<String, List<Integer>> elementIdToPathFromStartEvent, List<Integer> pathToCurrentElement
    ) {
        Set<String> pathsToRemove = new HashSet<>();
        for (Entry<String, List<Integer>> elementIdAndPathToIt : elementIdToPathFromStartEvent.entrySet()) {
            List<Integer> pathToElement = elementIdAndPathToIt.getValue();
            if (pathToElement.size() > pathToCurrentElement.size() && pathToElement.subList(
                    0,
                    pathToCurrentElement.size()
            ).equals(pathToCurrentElement)) {
                pathsToRemove.add(elementIdAndPathToIt.getKey());
            }
        }
        return pathsToRemove;
    }

    public BpmnModel layoutModel(BpmnModel model) {
        model = model.getCopy();
        Set<String> startEventsIds = Set.of(model.getStartEvent());
        Set<String> discovered = new HashSet<>();
        Set<String> visited = new HashSet<>();
        Set<DirectedEdge> backEdges = new HashSet<>();
        boolean reversedEdge = true;
        log.info("Finding edges to reverse");
        while (reversedEdge) {
            Set<DirectedEdge> currentIterationBackEdges = new HashSet<>();
            for (String startEventId : startEventsIds) {
                classifyEdges(startEventId, model, discovered, visited, currentIterationBackEdges);
            }

            for (DirectedEdge sequenceFlow : currentIterationBackEdges) {
                model.removeSequenceFlow(sequenceFlow.sourceId(), sequenceFlow.targetId());
                model.addUnlabelledSequenceFlow(sequenceFlow.targetId(), sequenceFlow.sourceId());
            }

            if (!currentIterationBackEdges.isEmpty()) {
                for (DirectedEdge edge : currentIterationBackEdges) {
                    if (backEdges.contains(edge)) {
                        backEdges.remove(edge);
                    } else {
                        backEdges.add(edge);
                    }
                }
            } else {
                reversedEdge = false;
            }
        }

        log.info("Reversed edges: '{}'", backEdges);

        List<String> sortedNodes = topologicalSort(model);

        log.info("Sorted nodes: '{}", sortedNodes.stream().map(model::getHumanReadableId).toList());

        return gridLayout(model, sortedNodes, backEdges);
    }

    private void layoutSequenceFlows(BpmnModel model, Map<DirectedEdge, Integer> flowsOffsets) {
        for (DirectedEdge sequenceFlow : model.getSequenceFlows()) {
            Dimensions sourceDimensions = model.getElementDimensions(sequenceFlow.sourceId());
            Dimensions targetDimensions = model.getElementDimensions(sequenceFlow.targetId());
            List<Point2d> flowWaypoints;
            double targetX = targetDimensions.x();
            double targetY = targetDimensions.y();
            double sourceX = sourceDimensions.x();
            double sourceY = sourceDimensions.y();
            double sourceWidth = sourceDimensions.width();
            double sourceHeight = sourceDimensions.height();
            if (flowsOffsets.containsKey(sequenceFlow)) {
                int sequenceFlowOffset = flowsOffsets.get(sequenceFlow);
                if (sequenceFlowOffset == 0) {
                    flowWaypoints = List.of(
                            new Point2d(sourceX + sourceWidth, sourceY + sourceHeight / 2),
                            new Point2d(targetX, targetY / 2)
                    );
                } else if (sequenceFlowOffset > 0) {
                    flowWaypoints = List.of(
                            new Point2d(sourceX + sourceWidth / 2, sourceY + sourceHeight),
                            new Point2d(
                                    sourceX + sourceWidth / 2,
                                    sourceY + sourceHeight / 2 + 150 * sequenceFlowOffset
                            ),
                            new Point2d(
                                    sourceX + sourceWidth / 2,
                                    sourceY + sourceHeight / 2 + 150 * sequenceFlowOffset
                            ),
                            new Point2d(sourceX + sourceWidth / 2, targetY)
                    );
                } else {
                    flowWaypoints = List.of(
                            new Point2d(sourceX + sourceWidth / 2, sourceY),
                            new Point2d(
                                    sourceX + sourceWidth / 2,
                                    sourceY + sourceHeight / 2 + 150 * sequenceFlowOffset
                            ),
                            new Point2d(
                                    sourceX + sourceWidth / 2,
                                    sourceY + sourceHeight / 2 + 150 * sequenceFlowOffset
                            ),
                            new Point2d(sourceX + sourceWidth / 2, targetY)
                    );
                }
            } else {
                double targetHeight = targetDimensions.height();
                if (Math.abs(sourceY + sourceHeight / 2 - (targetY + targetHeight / 2)) < 0.1) {
                    flowWaypoints = List.of(
                            new Point2d(sourceX + sourceWidth, sourceY + sourceHeight / 2),
                            new Point2d(targetX, targetY + targetHeight / 2)
                    );
                } else {
                    double targetWidth = targetDimensions.width();
                    if (sourceY < targetY) {
                        boolean sourceIsSplit = model.findSuccessors(sequenceFlow.sourceId()).size() > 1;
                        if (sourceIsSplit) {
                            flowWaypoints = List.of(
                                    new Point2d(sourceX + sourceWidth / 2, sourceY + sourceHeight),
                                    new Point2d(sourceX + sourceWidth / 2, targetY + targetHeight / 2),
                                    new Point2d(targetX, targetY + targetHeight / 2)
                            );
                        } else {
                            flowWaypoints = List.of(
                                    new Point2d(sourceX + sourceWidth, sourceY + sourceHeight / 2),
                                    new Point2d(targetX + targetWidth / 2, sourceY + sourceHeight / 2),
                                    new Point2d(targetX + targetWidth / 2, targetY)
                            );

                        }
                    } else {
                        boolean sourceIsSplit = model.findSuccessors(sequenceFlow.sourceId()).size() > 1;
                        if (sourceIsSplit) {
                            flowWaypoints = List.of(
                                    new Point2d(sourceX + sourceWidth / 2, sourceY),
                                    new Point2d(sourceX + sourceWidth / 2, targetY + targetHeight / 2),
                                    new Point2d(targetX, targetY + targetHeight / 2)
                            );
                        } else {
                            flowWaypoints = List.of(
                                    new Point2d(sourceX + sourceWidth, sourceY + sourceHeight / 2),
                                    new Point2d(targetX + targetWidth / 2, sourceY + sourceHeight / 2),
                                    new Point2d(targetX + targetWidth / 2, targetY + targetHeight)
                            );
                        }
                    }
                }
            }

            model.setWaypointsOfFlow(sequenceFlow.edgeId(), flowWaypoints);
        }
    }

    private BpmnModel gridLayout(BpmnModel model, List<String> sortedElements, Set<DirectedEdge> backEdges) {
        Map<String, SplitInfo> splitIdToInfo = new HashMap<>();
        Map<String, List<Integer>> pathsToElements = new HashMap<>();
        Map<DirectedEdge, Integer> flowsOffsets = new HashMap<>();
        model = model.getCopy();
        var grid = new Grid();
        for (String element : sortedElements) {
            placeElementInGrid(element, model, backEdges, grid, splitIdToInfo, pathsToElements, flowsOffsets);
        }

        for (Cell cell : grid.allCells()) {
            GridPosition cellPosition = cell.gridPosition();
            String elementId = cell.idOfElementInside();
            Point2d diagramPlanePosition = gridElementToDiagramPositionMapping.apply(
                    150,
                    150,
                    cellPosition,
                    model.getNodeType(elementId)
                         .orElseThrow()
            );
            log.info(
                    "Setting position of element '{}' at cell '{}' to '{}'",
                    cellPosition,
                    model.getHumanReadableId(elementId).get(),
                    diagramPlanePosition
            );
            model.setPositionOfElement(elementId, diagramPlanePosition);
        }

        layoutSequenceFlows(model, flowsOffsets);

        return model;
    }

    private void placeElementInGrid(
            String element,
            BpmnModel model,
            Set<DirectedEdge> backEdges,
            Grid grid,
            Map<String, SplitInfo> splitIdToInfo,
            Map<String, List<Integer>> pathsToElements,
            Map<DirectedEdge, Integer> flowsOffsets
    ) {
        log.info("Placing element '{}'", model.getHumanReadableId(element).get());
        int numberOfPredecessors = model.findPredecessors(element).size();
        GridPosition finalPosition;
        if (numberOfPredecessors == 0) {
            log.info("Element has no predecessors");
            finalPosition = new GridPosition(0, grid.getNumberOfRows());
            pathsToElements.put(element, new ArrayList<>(List.of(finalPosition.y())));
            grid.addCell(new Cell(finalPosition, element));
            log.info("Final position: '{}'", finalPosition);
        } else if (numberOfPredecessors == 1) {
            log.info("Element has a single predecessor");
            Set<String> backEdgesIds = backEdges.stream().map(DirectedEdge::targetId).collect(Collectors.toSet());
            insertElementWithSinglePredecessorIntoTheGrid(
                    element,
                    model,
                    grid,
                    splitIdToInfo,
                    backEdgesIds,
                    pathsToElements
            );
        } else {
            log.info("Element is a join");
            insertJoinElementIntoGrid(element, model, grid, flowsOffsets, splitIdToInfo, pathsToElements);
        }

        int numberOfSuccessors = model.findSuccessors(element).size();
        boolean elementIsSplit = numberOfSuccessors >= 2;
        if (elementIsSplit) {
            log.info("Element is a split");
            List<Integer> pathToCurrentElement = pathsToElements.get(element);
            int centerBranchNumber;
            if (numberOfSuccessors % 2 == 0 && !(pathToCurrentElement.get(pathToCurrentElement.size() - 1) == 0
                                                 && pathToCurrentElement.size() >= 2)) {
                centerBranchNumber = numberOfSuccessors / 2 - 1;
            } else {
                centerBranchNumber = numberOfSuccessors / 2;
            }

            String correspondingJoin = null;
            if (numberOfPredecessors != 0) {
                DirectedEdge firstIncomingFlow = model.getIncomingSequenceFlows(element).iterator().next();
                for (DirectedEdge outgoingEdge : model.getOutgoingSequenceFlows(element)) {
                    boolean successorIsJoin = model.findPredecessors(outgoingEdge.targetId()).size() >= 2;
                    if (successorIsJoin) {
                        boolean incomingEdgeAndOutgoingEdgeBothReversedOrNot =
                                backEdges.contains(firstIncomingFlow) == backEdges.contains(outgoingEdge);
                        if (incomingEdgeAndOutgoingEdgeBothReversedOrNot) {
                            correspondingJoin = outgoingEdge.targetId();
                            break;
                        }
                    }
                }
            }

            SplitInfo splitInfo = new SplitInfo(element, true, centerBranchNumber, correspondingJoin, 0);
            splitIdToInfo.put(element, splitInfo);
        }
    }

    private void insertJoinElementIntoGrid(
            String element,
            BpmnModel model,
            Grid grid,
            Map<DirectedEdge, Integer> flowToBranchOffset,
            Map<String, SplitInfo> splitIdToInfo,
            Map<String, List<Integer>> elementIdToPathFromStartEvent
    ) {
        var rightmostPredecessorPosition = new GridPosition(0, 0);
        int rightmostPredecessorX = -1;
        String rightmostPredecessor = null;
        LinkedHashSet<String> predecessors = model.findPredecessors(element);
        for (String predecessor : predecessors) {
            log.info(
                    "Checking if the predecessor: '{}' is the rightmost one'",
                    model.getHumanReadableId(predecessor).orElseThrow().asString()
            );
            Optional<Cell> predecessorCell = grid.findCellByIdOfElementInside(predecessor);
            if (predecessorCell.isPresent()) {
                GridPosition predecessorPosition = predecessorCell.get().gridPosition();
                if (predecessorPosition.x() > rightmostPredecessorX) {
                    rightmostPredecessorPosition = predecessorPosition;
                    rightmostPredecessorX = predecessorPosition.x();
                    rightmostPredecessor = predecessor;
                }
            }
        }

        log.info("Rightmost predecessor: '{}' at position '{}'", rightmostPredecessor, rightmostPredecessorPosition);

        int forks = 1;
        boolean found = true;
        String correspondingSplit = predecessors.iterator().next();
        do {
            Set<String> elementPredecessors = model.findPredecessors(correspondingSplit);
            Set<String> elementSuccessors = model.findSuccessors(correspondingSplit);

            if (elementPredecessors.size() >= 2) {
                forks += 1;
            }

            if (elementSuccessors.size() >= 2) {
                forks -= 1;
            }

            if (!elementPredecessors.isEmpty()) {
                correspondingSplit = elementPredecessors.iterator().next();
            } else {
                found = false;
                break;
            }
        } while (forks != 0);

        int finalRow;
        if (found) {
            log.info("Found corresponding split '{}'", model.getHumanReadableId(correspondingSplit).get());
            finalRow = grid.findCellByIdOfElementInside(correspondingSplit).orElseThrow().y();
        } else {
            log.info("no corresponding split");
            int rowNumberSum = 0;
            for (String predecessor : predecessors) {
                rowNumberSum += grid.findCellByIdOfElementInside(predecessor).orElseThrow().y();
            }

            finalRow = rowNumberSum / predecessors.size();
        }

        List<Integer> pathToCurrentElement = elementIdToPathFromStartEvent.get(correspondingSplit);
        elementIdToPathFromStartEvent.put(element, pathToCurrentElement);
        boolean addedCell = false;
        GridPosition finalPosition = new GridPosition(rightmostPredecessorPosition.x() + 1, finalRow);
        while (!addedCell) {
            if (grid.isCellOccupied(finalPosition)) {
                finalPosition = finalPosition.withYDifference(1);
            } else {
                grid.addCell(new Cell(finalPosition, element));
                addedCell = true;
                log.info("Final position for join element '{}'", finalPosition);
            }
        }

        for (DirectedEdge incomingSequenceFlow : model.getIncomingSequenceFlows(element)) {
            SplitInfo splitInfo = splitIdToInfo.get(correspondingSplit);
            if (incomingSequenceFlow.sourceId().equals(correspondingSplit)
                && splitInfo.getCorrespondingJoin() != null) {
                flowToBranchOffset.put(
                        incomingSequenceFlow,
                        splitInfo.getNextFreeBranch() - splitInfo.getCenterBranchNumber()
                );
                break;
            }
        }

        Set<String> pathsToRemove = findPathsToRemove(elementIdToPathFromStartEvent, pathToCurrentElement);

        for (String elementWitPath : pathsToRemove) {
            elementIdToPathFromStartEvent.remove(elementWitPath);
        }
    }

    private void insertElementWithSinglePredecessorIntoTheGrid(
            String element,
            BpmnModel model,
            Grid grid,
            Map<String, SplitInfo> splitInfos,
            Set<String> backEdgesIds,
            Map<String, List<Integer>> pathsToElements
    ) {
        String elementPredecessor = model.findPredecessors(element).iterator().next();
        GridPosition predecessorPosition =
                grid.findCellByIdOfElementInside(elementPredecessor).orElseThrow().gridPosition();
        Set<String> predecessorElementSuccessors = model.findSuccessors(elementPredecessor);
        boolean predecessorElementIsNotASplit = predecessorElementSuccessors.size() == 1;
        List<Integer> pathToPredecessor = pathsToElements.get(elementPredecessor);
        log.info("Path to predecessor: '{}'", pathToPredecessor);
        GridPosition finalPosition;
        if (predecessorElementIsNotASplit) {
            log.info("Predecessor element '{}' is not a split", model.getHumanReadableId(elementPredecessor).get());
            finalPosition = predecessorPosition.withX(predecessorPosition.x() + 1);
            pathsToElements.put(element, pathToPredecessor);

            grid.addCell(new Cell(finalPosition, element));
            log.info("Final position: '{}'", finalPosition);
        } else {
            log.info("Predecessor element '{}' is a split", model.getHumanReadableId(elementPredecessor).get());
            @Nullable DirectedEdge sequenceFlowBeforeSplit = null;
            Set<DirectedEdge> predecessorIncomingSequenceFlows = model.getIncomingSequenceFlows(elementPredecessor);
            if (!predecessorIncomingSequenceFlows.isEmpty()) {
                sequenceFlowBeforeSplit = predecessorIncomingSequenceFlows.iterator().next();
            }

            DirectedEdge elementsIncomingSequenceFlow = model.getIncomingSequenceFlows(element).iterator().next();
            SplitInfo splitInfo = splitInfos.get(elementPredecessor);
            log.info("Predecessor split info: '{}'", splitInfo);
            boolean flowBeforeSplitAndFlowBetweenSplitAndElementAreOfTheSameType = sequenceFlowBeforeSplit != null
                                                                                   && backEdgesIds.contains(
                    sequenceFlowBeforeSplit.edgeId()) == backEdgesIds.contains(elementsIncomingSequenceFlow.edgeId());
            if (splitInfo.isCenterBranchFree() && splitInfo.getCorrespondingJoin() == null
                && flowBeforeSplitAndFlowBetweenSplitAndElementAreOfTheSameType) {
                log.info("No corresponding join, center branch free, and flow before predecessor split and between "
                         + "predecessor and element are of the same type ");
                List<Integer> pathToCurrentElement = new ArrayList<>(pathToPredecessor);
                pathToCurrentElement.add(splitInfo.getCenterBranchNumber());
                pathsToElements.put(element, pathToCurrentElement);
                finalPosition = predecessorPosition.withX(predecessorPosition.x() + 1);
                grid.addCell(new Cell(finalPosition, element));
                log.info("Final position: '{}'", finalPosition);
                splitInfo.setCenterBranchFree(false);
                if (splitInfo.getNextFreeBranch() == splitInfo.getCenterBranchNumber()) {
                    splitInfo.setNextFreeBranch(splitInfo.getNextFreeBranch() + 1);
                }
            } else {
                if (splitInfo.getNextFreeBranch() == splitInfo.getCenterBranchNumber()) {
                    splitInfo.setNextFreeBranch(splitInfo.getNextFreeBranch() + 1);
                }

                List<Integer> pathToCurrentElement = new ArrayList<>(pathsToElements.get(elementPredecessor));
                pathToCurrentElement.add(splitInfo.getNextFreeBranch());
                pathsToElements.put(element, pathToCurrentElement);

                finalPosition = new GridPosition(
                        predecessorPosition.x() + 1,
                        predecessorPosition.y() + splitInfo.getNextFreeBranch()
                        - splitInfo.getCenterBranchNumber()
                );

                log.info("Final position: '{}'", finalPosition);

                grid.addCell(new Cell(finalPosition, element));

                splitInfo.setNextFreeBranch(splitInfo.getNextFreeBranch() + 1);
                shiftElements(pathToCurrentElement, splitInfo.getCenterBranchNumber(), pathsToElements, grid);
            }
        }
    }

    private void shiftElements(
            List<Integer> pathToElement, int centerBranchNumber, Map<String, List<Integer>> elementIdToPath, Grid grid
    ) {
        if (pathToElement.get(pathToElement.size() - 1) < centerBranchNumber) {
            for (int i = 1; i < pathToElement.size(); i++) {
                List<Integer> subpath = new ArrayList<>(pathToElement.subList(0, i));
                subpath.set(subpath.size() - 1, subpath.get(subpath.size() - 1) - 1);
                boolean found = true;
                while (found) {
                    found = false;
                    for (Entry<String, List<Integer>> elementIdAndPathFromStartEvent : elementIdToPath.entrySet()) {
                        List<Integer> path = elementIdAndPathFromStartEvent.getValue();
                        if (path.size() >= subpath.size() && path.subList(0, subpath.size()).equals(subpath)) {
                            found = true;
                            GridPosition elementGridPosition = grid.findCellByIdOfElementInside(
                                    elementIdAndPathFromStartEvent.getKey()).orElseThrow().gridPosition();
                            GridPosition updatedPosition = elementGridPosition.withYDifference(-1);
                            grid.moveCell(elementGridPosition, updatedPosition);
                        }
                    }
                    subpath.set(subpath.size() - 1, subpath.get(subpath.size() - 1) - 1);
                }
            }
        } else {
            for (int i = 1; i < pathToElement.size(); i++) {
                List<Integer> subpath = new ArrayList<>(pathToElement.subList(0, i));
                subpath.set(subpath.size() - 1, subpath.get(subpath.size() - 1) + 1);
                boolean found = true;
                while (found) {
                    found = false;
                    for (Entry<String, List<Integer>> elementIdAndPathFromStartEvent : elementIdToPath.entrySet()) {
                        List<Integer> path = elementIdAndPathFromStartEvent.getValue();
                        if (path.size() >= subpath.size() && path.subList(0, subpath.size()).equals(subpath)) {
                            found = true;
                            GridPosition elementGridPosition = grid.findCellByIdOfElementInside(
                                    elementIdAndPathFromStartEvent.getKey()).orElseThrow().gridPosition();
                            GridPosition updatedPosition = elementGridPosition.withYDifference(1);
                            grid.moveCell(elementGridPosition, updatedPosition);
                        }
                    }
                    subpath.set(subpath.size() - 1, subpath.get(subpath.size() - 1) + 1);
                }
            }
        }
    }

    private boolean classifyEdges(
            String elementId,
            BpmnModel model,
            Set<String> discoveredElements,
            Set<String> visitedElements,
            Set<DirectedEdge> reversedEdges
    ) {
        log.trace("Processing element '{}'", elementId);
        discoveredElements.add(elementId);
        var reversed = false;
        for (DirectedEdge sequenceFlow : model.getOutgoingSequenceFlows(elementId)) {
            log.trace("Processing edge: '{}'", sequenceFlow);
            String successor = sequenceFlow.targetId();
            Set<String> backEdgesId = reversedEdges.stream().map(DirectedEdge::edgeId).collect(Collectors.toSet());
            if (!discoveredElements.contains(successor)) {
                log.trace("Successor element '{}' was not discovered yet", successor);
                reversed = classifyEdges(successor, model, discoveredElements, visitedElements, reversedEdges);
                if (reversed) {
                    Set<String> outgoingSequenceFlowIds = model.getOutgoingSequenceFlows(elementId).stream().map(
                            DirectedEdge::edgeId).collect(Collectors.toSet());
                    long numberOfOutgoingSequenceFlows = outgoingSequenceFlowIds.size();
                    long outgoingSequenceFlowsMarkedAsBackEdges =
                            outgoingSequenceFlowIds.stream().filter(backEdgesId::contains).count();
                    reversed = numberOfOutgoingSequenceFlows - outgoingSequenceFlowsMarkedAsBackEdges < 2;
                    reversedEdges.add(sequenceFlow);
                }
            } else if (!visitedElements.contains(successor)) {
                log.trace("Successor element '{}' was not visited yet", successor);
                reversedEdges.add(sequenceFlow);
                Set<String> outgoingSequenceFlowIds = model.getOutgoingSequenceFlows(elementId).stream().map(
                        DirectedEdge::edgeId).collect(Collectors.toSet());
                long numberOfOutgoingSequenceFlows = outgoingSequenceFlowIds.size();
                long outgoingSequenceFlowsMarkedAsBackEdges =
                        outgoingSequenceFlowIds.stream().filter(backEdgesId::contains).count();
                reversed = numberOfOutgoingSequenceFlows - outgoingSequenceFlowsMarkedAsBackEdges == 0;
            }
        }

        visitedElements.add(elementId);
        return reversed;
    }

    private List<String> topologicalSort(BpmnModel model) {
        Set<String> nodesToSort = new HashSet<>(model.getFlowNodes());
        log.info("Nodes to sort: '{}'", nodesToSort);
        Map<String, Integer> joinNodes = new HashMap<>();
        for (String node : model.getFlowNodes()) {
            int numberOfPredecessors = model.findPredecessors(node).size();
            if (numberOfPredecessors > 1) {
                joinNodes.put(node, numberOfPredecessors);
            }
        }
        List<String> sortedNodes = new ArrayList<>();
        Set<String> reversedEdges = new HashSet<>();
        var modelCopy = model.getCopy();
        while (!nodesToSort.isEmpty()) {
            Set<String> nodesWithNoPredecessors = new HashSet<>();
            for (String nodeId : nodesToSort) {
                if (modelCopy.findPredecessors(nodeId).isEmpty()) {
                    nodesWithNoPredecessors.add(nodeId);
                }
            }

            if (!nodesWithNoPredecessors.isEmpty()) {
                for (String node : nodesWithNoPredecessors) {
                    nodesToSort.remove(node);
                    joinNodes.remove(node);
                    sortedNodes.add(node);
                    for (DirectedEdge outgoingSequenceFlow : modelCopy.getOutgoingSequenceFlows(node)) {
                        modelCopy.removeSequenceFlow(outgoingSequenceFlow.edgeId());
                    }
                }
            } else {
                String firstJoinNodeWithRemovedIncomingEdge = null;
                for (Entry<String, Integer> joinNodeWithOriginalPredecessorCount : joinNodes.entrySet()) {
                    int currentNumberOfPredecessors =
                            modelCopy.findPredecessors(joinNodeWithOriginalPredecessorCount.getKey()).size();
                    boolean anyIncomingEdgeWasRemoved =
                            currentNumberOfPredecessors != joinNodeWithOriginalPredecessorCount.getValue();
                    if (anyIncomingEdgeWasRemoved) {
                        firstJoinNodeWithRemovedIncomingEdge = joinNodeWithOriginalPredecessorCount.getKey();
                        break;
                    }
                }

                if (firstJoinNodeWithRemovedIncomingEdge != null) {
                    for (String joinPredecessor : modelCopy.findPredecessors(firstJoinNodeWithRemovedIncomingEdge)) {
                        modelCopy.removeSequenceFlow(joinPredecessor, firstJoinNodeWithRemovedIncomingEdge);
                        String reversedEdgeId =
                                modelCopy.addUnlabelledSequenceFlow(
                                        firstJoinNodeWithRemovedIncomingEdge,
                                        joinPredecessor
                                ).getValue();
                        reversedEdges.add(reversedEdgeId);
                    }
                }
            }
        }

        log.info("Sorted nodes: '{}'", sortedNodes);

        return sortedNodes;
    }

    @EqualsAndHashCode
    @AllArgsConstructor
    @Getter
    @Setter
    @ToString
    private static final class SplitInfo {

        private String elementId;
        private boolean centerBranchFree;
        private int centerBranchNumber;
        @Nullable
        private String correspondingJoin;
        private int nextFreeBranch;
    }
}
