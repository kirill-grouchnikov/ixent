package org.jvnet.ixent.algorithms.graphics.segmentation.multiscale;

import java.util.*;

import org.jvnet.ixent.algorithms.graphics.segmentation.multiscale.structure.*;
import org.jvnet.ixent.math.MathConstants;
import org.jvnet.ixent.math.matrix.*;
import org.jvnet.ixent.util.DoubleHolder;

/**
 * The simplest coarsener, as outlined in <i>"Fast Multiscale Image
 * Segmentation"</i> by Sharon, Brandt and Basri in 2000 paper.
 *
 * @author Kirill Grouchnikov
 */
public class SimpleCoarsener extends Coarsener {
    /**
     * Fill additional information on single pixel
     *
     * @param col
     * @param row
     */
    protected void fillAdditionalInformation(int col, int row) {
    }

    /**
     * Fill additional information (such as brightness for example). Will be
     * called once for the initial graph
     */
    public void fillAdditionalInformation() {
    }

    /**
     * Returns an ordered array of integer indexes. Each index points to the
     * array of nodes in <b>prevGraphInfo</b>. The nodes are ordered by their
     * weights. The ordering is performed using the specified number of
     * buckets.
     *
     * @param bucketCount the number of buckets for node splitting
     * @return ordered array of node indexes
     */
    private int[] splitNodesInBuckets(int bucketCount) {
        // each row in the resulting jagged 2D array is an array of integer indexes
        //  pointing into the node array
        int graphSize = this.prevGraphInfo.getNodeCount();
        DoubleHolder<Double> minMax = this.prevGraphInfo.getMinMaxWeight();
        double minWeight = minMax.getValue1();
        double maxWeight = minMax.getValue2();
        boolean isTheSameWeight = (Math.abs(maxWeight - minWeight) <
                MathConstants.EPS_BIG);
        if ((this.prevGraphInfo.getIndex() < 2) || isTheSameWeight) {
            // put all in the same bucket
            int[] singleRow = new int[graphSize];
            for (int i = 0; i < graphSize; i++) {
                singleRow[i] = i;
            }
            return singleRow;
        }

        List<Integer>[] holder = new List[bucketCount];
        for (int i = 0; i < bucketCount; i++) {
            holder[i] = new LinkedList<Integer>();
        }

        // go over all nodes
        double denom = 1.0 / (maxWeight - minWeight);
        for (int i = 0; i < graphSize; i++) {
            double currWeight = this.prevGraphInfo.getNode(i).getVolume();
            // compute the corresponding bucket
            int bucketIndex = (int) (bucketCount * (currWeight - minWeight) *
                    denom);
            if (bucketIndex == bucketCount) {
                bucketIndex--;
            }
            holder[bucketIndex].add(i);
        }

        // at this point each list holds a collection of node indexes. Now we can
        // create a jagged array
        int[] result = new int[graphSize];
        int place = 0;
        for (int i = 0; i < bucketCount; i++) {
            int length = holder[i].size();
            if (length == 0) {
                continue;
            }
            for (int j : holder[i]) {
                result[place++] = j;
            }
        }

        return result;
    }

    public GraphInfo getNextGraph(int origSize) {
        // Major steps:
        // 1. order segments (nodes) by weight using buckets (only if graph index > 2)
        // 2. select biggest segment
        // 3. for each biggest unprocessed segment:
        //    a. check degree of attachment to all its previously selected neighbours
        //    b. if strongly coupled - finish processing
        //    c. if loosely coupled - add to selected
        // 4. compute sparse interpolation matrix
        // 5. compute new graph

        // 1. order segments (nodes) by weight using buckets (only if graph index > 2)
        int[] orderedNodes = this.splitNodesInBuckets(10);


        int nodeCount = this.prevGraphInfo.getNodeCount();
        // this array will hold 'true' for all the nodes that are selected
        boolean[] isSelected = new boolean[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            isSelected[i] = false;
        }

        // a linked list of all selected nodes (nodes in the new graph)
        List<NodeInfo> selectedNodes = new LinkedList<NodeInfo>();

        // Each entry in this array will hold a link to the selected node
        // that 'captured' this old node. The link will point to entry in
        // 'selectedNodes'. If the link is null, this means that this node
        // is still not captured
        NodeInfo[] capturers = new NodeInfo[nodeCount];

        // 2. select biggest segment
        int biggestSegmentIndex = orderedNodes[0];
        isSelected[biggestSegmentIndex] = true;
        NodeInfo newNode = new NodeInfo(0,
                this.prevGraphInfo.getNode(biggestSegmentIndex).getVolume());

        selectedNodes.add(newNode);
        capturers[biggestSegmentIndex] = newNode;

        // 3. for each biggest unprocessed segment:
        for (int currIndex = 1; currIndex < nodeCount; currIndex++) {
            int currNodeIndex = orderedNodes[currIndex];
            //    a. check degree of attachment to all its previously selected neighbours

            NodeInfo currNode = this.prevGraphInfo.getNode(currNodeIndex);
            assert currNode != null : "Current node is null";

            // get the node neigbours
            int[] currNodeNeigbours = currNode.getAllNeighboursIndexes();
            int currNeighbourCount = currNodeNeigbours.length;

            // compute the total weight of all its edges
            double totalWeight = currNode.getTotalConnectivityWeight();

            // compute maximal weight to selected nodes
            double maximalWeightToSelected = 0.0;
            NodeInfo capturer = null;
            for (int neighIndex = 0;
                 neighIndex < currNeighbourCount; neighIndex++) {
                int currNeighbourIndex = currNodeNeigbours[neighIndex];

                // check if this neighbour is captured
                if (!isSelected[currNeighbourIndex]) {
                    continue;
                }

                // get weight to this neighbour
                double currWeight = currNode.getWeightToNode(
                        currNeighbourIndex);
                if (currWeight > maximalWeightToSelected) {
                    maximalWeightToSelected = currWeight;
                    capturer = capturers[currNeighbourIndex];
                }
            }
            // check if strongly attached. Take 0.1 constant from the paper
            if (maximalWeightToSelected > (0.1 * totalWeight)) {
                //    b. if strongly coupled - finish processing
                assert capturer != null: "Capturer is null for strong coupling";
                capturers[currNodeIndex] = capturer;

                // update the weight of the capturer
                capturer.incrementWeight(currNode.getVolume());
            }
            else {
                //    c. if loosely coupled - add to selected
                newNode = new NodeInfo(selectedNodes.size(),
                        this.prevGraphInfo.getNode(currNodeIndex).getVolume());

                capturers[currNodeIndex] = newNode;
                isSelected[currNodeIndex] = true;

                selectedNodes.add(newNode);
            }
        }

        // 4. compute sparse interpolation matrix

        int newNodeCount = selectedNodes.size();
        // column count - size of old graph
        // row count - size of new graph
        SparseMatrix interpolationMatrix = new SparseMatrix(newNodeCount,
                nodeCount);

        // setLocation all weights for selected nodes
        for (int i = 0; i < nodeCount; i++) {
            if (isSelected[i]) {
                interpolationMatrix.set(capturers[i].getIndex(), i, 1.0);
            }
        }

        // setLocation all weights for unselected nodes
        for (int i = 0; i < nodeCount; i++) {
            if (isSelected[i]) {
                continue;
            }
            NodeInfo currNode = this.prevGraphInfo.getNode(i);

            // go over all neighbours, keeping track of the selected nodes that
            // have captured them
            Map<Integer, Double> capturerWeights = new HashMap<Integer, Double>();

            // compute the total weight of all its edges
            double totalWeight = currNode.getTotalConnectivityWeight();

            for (EdgeInfo currNeighbourEdge : currNode.getEdges()) {
                int currNeighbourIndex = currNeighbourEdge.getOtherNodeIndex(
                        currNode.getIndex());

                // check if this neighbour is captured
                assert capturers[currNeighbourIndex] != null: "No capturer for the current neighbour";

                // update weight map
                double prevWeight = currNeighbourEdge.getWeight();

                int capturerIndex = capturers[currNeighbourIndex].getIndex();

                if (capturerWeights.containsKey(capturerIndex)) {
                    double currWeight = capturerWeights.get(capturerIndex);
                    double newWeight = currWeight + prevWeight;
                    capturerWeights.put(capturerIndex, newWeight);
                }
                else {
                    // insert new pair
                    capturerWeights.put(capturerIndex, prevWeight);
                }
            }

            // setLocation new weights proportionally to previous weights
            for (int capturerIndex : capturerWeights.keySet()) {
                double oldWeight = capturerWeights.get(capturerIndex);
                double newWeight = oldWeight / totalWeight;
                interpolationMatrix.set(capturerIndex, currNode.getIndex(),
                        newWeight);
            }
        }

        // 5. compute new graph

        GraphInfo result = createNewGraph(origSize, selectedNodes,
                capturers, interpolationMatrix);

        return result;
    }

    /**
     * Create weight matrix for the new graph based on 'capturer' info (which
     * node captured which node) and interpolation matrix
     *
     * @param origSize            the size of the original graph
     * @param selectedNodes       capturers
     * @param capturersArray      capturer info
     * @param interpolationMatrix interpolation matrix
     * @return new weight matrix
     */
    protected GraphInfo createNewGraph(int origSize,
                                       List<NodeInfo> selectedNodes,
                                       NodeInfo[] capturersArray,
                                       SparseMatrix interpolationMatrix) {

        // convert interpolation matrix from row-wise to column-wise
        SparseColumnMatrix interpolationColumnMatrix =
                interpolationMatrix.getAsColumnMatrix();

        SparseAccessibleMatrix newWeightMatrix =
                interpolationColumnMatrix.computeConvolution(
                        this.prevGraphInfo.getWeightMatrix());

        newWeightMatrix.normalize();

        // compute new node array - copy from selected nodes
        NodeInfo[] newNodes = new NodeInfo[selectedNodes.size()];
        int count = 0;
        for (NodeInfo node : selectedNodes) {
            newNodes[count++] = node;
        }

        // compute all edges. Each edge corresponds to a non-zero value
        // in the weight matrix
        int newSize = newNodes.length;
        for (int row = 0; row < newSize; row++) {
            // compute how many non-zero values there are in the corresponding column
            int nzCount = newWeightMatrix.getNzCountInRow(row);
            if (nzCount == 0) {
                continue;
            }

            for (int colIndex = 0; colIndex < nzCount; colIndex++) {
                int column = newWeightMatrix.getColumnByIndex(row, colIndex);
                double value = newWeightMatrix.getValueByIndex(row, colIndex);

                // this edge connects node number 'row' and node number 'column'
                EdgeInfo newEdge = new EdgeInfo(row, column, value);
                newNodes[row].addEdge(newEdge);
            }
        }

        // compute volume for each new node
        double[] newVolumes = new double[newSize];
        for (int i = 0; i < newSize; i++) {
            newVolumes[i] = newNodes[i].getVolume();
        }

        int nodeCount = this.prevGraphInfo.getNodeCount();
        for (int nodeIndex = 0; nodeIndex < nodeCount; nodeIndex++) {
            // who's the capturer?
            NodeInfo capturer = capturersArray[nodeIndex];
            int capturerIndex = capturer.getIndex();

            // go over all neighbours of the current node and sum up the
            // weights on edges that connect this node and nodes that were
            // captures by the same capturer
            NodeInfo currNode = this.prevGraphInfo.getNode(nodeIndex);
            double weightToCapturer = 0.0;
            for (EdgeInfo currNeighbourEdge : currNode.getEdges()) {
                int currNeighbourIndex =
                        currNeighbourEdge.getOtherNodeIndex(nodeIndex);

                // check if this neighbour is captured
                assert capturersArray[currNeighbourIndex] != null:
                        "No capturer for the current neighbour";

                if (capturersArray[currNeighbourIndex].getIndex() ==
                        capturerIndex) {
                    weightToCapturer += currNeighbourEdge.getWeight();
                }
            }
            newVolumes[capturerIndex] += currNode.getVolume() *
                    weightToCapturer;
        }

        // compute saliency for each new node
        double factor = (double) origSize / newSize;
        double[] newSaliencies = new double[newSize];
        for (NodeInfo currNewNode : newNodes) {
            int index = currNewNode.getIndex();
            double totalConnectivity = currNewNode.getTotalConnectivityWeight();
            newSaliencies[index] = factor * totalConnectivity /
                    newVolumes[index];
        }

        // update all nodes
        for (NodeInfo currNewNode : newNodes) {
            int index = currNewNode.getIndex();
            currNewNode.setVolume(newVolumes[index]);
            currNewNode.setSaliency(newSaliencies[index]);
        }

        GraphInfo result = new GraphInfo(this.prevGraphInfo.getIndex() + 1,
                newNodes, newWeightMatrix, interpolationMatrix);

        return result;

    }
}
