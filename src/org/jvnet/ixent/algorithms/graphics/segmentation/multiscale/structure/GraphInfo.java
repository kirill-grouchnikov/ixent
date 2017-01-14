package org.jvnet.ixent.algorithms.graphics.segmentation.multiscale.structure;

import org.jvnet.ixent.math.matrix.Matrix;
import org.jvnet.ixent.math.matrix.SparseMatrix;
import org.jvnet.ixent.util.DoubleHolder;

/**
 * This class holds a collection of nodes (in array), accessed by index, a
 * virtual collection of edges, where each edge points to two nodes and each
 * node holds a linked list of all its edges and a virtual weight matrix where
 * each weight is associated with the corresponding edge
 *
 * @author Kirill Grouchnikov
 */
public class GraphInfo {
    private NodeInfo[] nodes;

    private int index;

    /**
     * This (sparse) matrix holds all the weights (it might be considered as
     * double storage as all the weights are stored in edges)
     */
    private Matrix weightMatrix;

    /**
     * This (sparse) matrix holds info on how to compute state vector
     */
    private Matrix interpolationMatrix;

    /**
     * Constructor
     *
     * @param _index graph index (level)
     * @param _nodes array of nodes
     */
    public GraphInfo(int _index, NodeInfo[] _nodes) {
        this.index = _index;
        this.nodes = _nodes;
        // compute weight matrix
        int nodeCount = this.nodes.length;
        this.weightMatrix = new SparseMatrix(nodeCount, nodeCount);
        for (int i = 0; i < nodeCount; i++) {
            NodeInfo currNode = this.nodes[i];
            int currNodeIndex = currNode.getIndex();
            for (EdgeInfo currEdge : currNode.getEdges()) {
                int anotherNodeIndex = currEdge.getOtherNodeIndex(
                        currNodeIndex);
                this.weightMatrix.set(currNodeIndex, anotherNodeIndex,
                        currEdge.getWeight());
            }
        }

        this.interpolationMatrix = null;
    }

    /**
     * Constructor
     *
     * @param _index        graph index (level)
     * @param _nodes        array of nodes
     * @param _weightMatrix weight matrix
     */
    public GraphInfo(int _index, NodeInfo[] _nodes, Matrix _weightMatrix,
                     Matrix _interpolationMatrix) {
        this.index = _index;
        this.nodes = _nodes;
        this.weightMatrix = _weightMatrix;
        this.interpolationMatrix = _interpolationMatrix;
    }

    /**
     * @return graph index (level)
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * @return the count of nodes in this graph
     */
    public int getNodeCount() {
        if (this.nodes == null) {
            return 0;
        }
        return this.nodes.length;
    }

    /**
     * Returns node info
     *
     * @param index node index
     * @return node info
     */
    public NodeInfo getNode(int index) {
        return this.nodes[index];
    }

    /**
     * Returns all nodes (allows more compact code in Java 5)
     *
     * @return node array
     */
    public NodeInfo[] getNodes() {
        return this.nodes;
    }

    /**
     * @return graph weight matrix
     */
    public Matrix getWeightMatrix() {
        return this.weightMatrix;
    }

    /**
     * @return interpolation matrix for computing the state vector
     */
    public Matrix getInterpolationMatrix() {
        return interpolationMatrix;
    }

    /**
     * Returns minimal and maximal weight of nodes in this graph
     *
     * @return a 2-double value holder, <b>value1</b> holds the min,
     *         <b>value2</b> holds the max
     */
    public DoubleHolder<Double> getMinMaxWeight() {
        double minWeight = this.nodes[0].getVolume();
        double maxWeight = minWeight;

        int len = this.nodes.length;
        for (int i = 1; i < len; i++) {
            double currWeight = this.nodes[i].getVolume();
            if (currWeight < minWeight) {
                minWeight = currWeight;
            }
            if (currWeight > maxWeight) {
                maxWeight = currWeight;
            }
        }
        return new DoubleHolder<Double>(minWeight, maxWeight);
    }

    /**
     * Dumps info on this graph to System.out
     */
    public void dump() {
        System.out.println("Graph with " + this.nodes.length + " nodes");
        for (int i = 0; i < this.nodes.length; i++) {
            NodeInfo currNode = this.nodes[i];
            System.out.print(" node " + currNode.getIndex() + " (v " +
                    currNode.getVolume() + ", s " +
                    currNode.getSaliency() +
                    "):");
            for (EdgeInfo currEdge : currNode.getEdges()) {
                System.out.print(" to " +
                        currEdge.getOtherNodeIndex(currNode.getIndex()) +
                        " (" + currEdge.getWeight() + "), ");
            }
            System.out.println();
        }
    }

    /**
     * Returns the <code>String</code> representation of this object
     *
     * @return a <code>String</code> representing this object
     */
    public String toString() {
        return "graph (" + this.nodes.length + " nodes), index" + this.index;
    }
}
