package org.jvnet.ixent.algorithms.graphics.segmentation.multiscale.structure;

import java.util.*;

/**
 * This class holds info on a single node. Each node has additional information
 * associated with it (like volume). Each node has an unordered list of its
 * edges.
 *
 * @author Kirill Grouchnikov
 * @see GraphInfo
 */

public class NodeInfo implements Comparable<NodeInfo> {
    private int index;
    private double volume;
    private double saliency;
    private List<EdgeInfo> edges;
    private AdditionalNodeInfo additionalInfo;

    /**
     * @param _index  node index
     * @param _volume node volume
     */
    public NodeInfo(int _index, double _volume) {
        this.index = _index;
        this.volume = _volume;
        this.edges = new LinkedList<EdgeInfo>();
        this.saliency = -1.0;
    }

    /**
     * Add this edge to a list of all edges of this node. No check is performed
     * that this node actually belongs to the edge
     *
     * @param edge edge structure
     */
    public void addEdge(EdgeInfo edge) {
        this.edges.add(edge);
    }

    /**
     * Get an iterator over this node's edges
     *
     * @return iterator
     */
    public Iterator<EdgeInfo> getEdgeIterator() {
        return this.edges.iterator();
    }

    /**
     * @return edge list
     */
    public List<EdgeInfo> getEdges() {
        return edges;
    }

    /**
     * @return node index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return node volume
     */
    public double getVolume() {
        return this.volume;
    }

    /**
     * @param volume new volume
     */
    public void setVolume(double volume) {
        this.volume = volume;
    }

    /**
     * Get index array of all neighbours of this node
     *
     * @return index array
     */
    public int[] getAllNeighboursIndexes() {
        int count = 0;
        int[] result = new int[this.edges.size()];
        for (EdgeInfo currEdge : this.edges) {
            result[count++] = currEdge.getOtherNodeIndex(this.index);
        }
        return result;
    }

    /**
     * Compute the volume on all edges coming out of this node
     *
     * @return the total volume
     */
    public double getTotalConnectivityWeight() {
        double result = 0.0;
        for (EdgeInfo currEdge : this.edges) {
            result += currEdge.getWeight();
        }
        return result;
    }

    /**
     * Returns the volume of edge connecting this node to a given node
     *
     * @param nodeIndex second node index
     * @return volume on the corresponding edge
     */
    public double getWeightToNode(int nodeIndex) {
        for (EdgeInfo currEdge : this.edges) {
            if (currEdge.getOtherNodeIndex(this.index) == nodeIndex) {
                return currEdge.getWeight();
            }
        }
        return 0.0;
    }

    /**
     * Increment node volume by delta
     *
     * @param weightDelta volume delta
     */
    public void incrementWeight(double weightDelta) {
        this.volume += weightDelta;
    }

    /**
     * @return node saliency
     */
    public double getSaliency() {
        return saliency;
    }

    /**
     * @param saliency new saliency
     */
    public void setSaliency(double saliency) {
        this.saliency = saliency;
    }

    /**
     * Comparator function for ordering nodes by decreasing saliency
     *
     * @param node2 second node
     * @return -1/0/1 if this node has bigger/the same/smaller saliency
     */
    public int compareTo(NodeInfo node2) {
        if (this == node2) {
            return 0;
        }
        if (this.saliency < node2.saliency) {
            return 1;
        }
        if (this.saliency > node2.saliency) {
            return -1;
        }
        return 0;
    }

    /**
     * @return additional information
     */
    public AdditionalNodeInfo getAdditionalInfo() {
        return additionalInfo;
    }

    /**
     * @param additionalInfo new additional information
     */
    public void setAdditionalInfo(AdditionalNodeInfo additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    /**
     * Return the number of neighbours
     *
     * @return neighbours number
     */
    public int getNeighbourCount() {
        return this.edges.size();
    }

    /**
     * Returns the <code>String</code> representation of this object
     *
     * @return a <code>String</code> representing this object
     */
    public String toString() {
        return "node " + this.index + " (" + this.edges.size() +
                " edges), vol " +
                this.volume;
    }
}
