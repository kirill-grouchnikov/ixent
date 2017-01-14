package org.jvnet.ixent.algorithms.graphics.segmentation.multiscale.structure;

/**
 * This class holds info on edge that connects two nodes. The nodes are
 * represented by their indexes, i.e. an edge can't exist on its own (without
 * the <a href="GraphInfo.html"> GraphInfo</a>)
 *
 * @author Kirill Grouchnikov
 * @see GraphInfo
 */
public class EdgeInfo {
    private int node1;
    private int node2;
    private double weight;

    /**
     * @param _node1  index of the first node
     * @param _node2  index of the second node
     * @param _weight edge weight
     */
    public EdgeInfo(int _node1, int _node2, double _weight) {
        this.node1 = _node1;
        this.node2 = _node2;
        this.weight = _weight;
    }

    /**
     * @return the index of the first node
     */
    public int getNode1() {
        return this.node1;
    }

    /**
     * @return the index of the second node
     */
    public int getNode2() {
        return this.node2;
    }

    /**
     * @return the edge weight
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Return the index of another node that belongs to this edge
     *
     * @param index index of node
     * @return index of neighbour node
     */
    public int getOtherNodeIndex(int index) {
        if (this.node1 == index) {
            return this.node2;
        }
        else {
            return this.node1;
        }
    }

    /**
     * Returns the <code>String</code> representation of this object
     *
     * @return a <code>String</code> representing this object
     */
    public String toString() {
        return "edge " + this.node1 + "-" + this.node2 + ", weight " +
                this.weight;
    }
}
