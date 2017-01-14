package org.jvnet.ixent.util;

import java.util.LinkedList;
import java.util.List;

/**
 * A tree with unlimited number of sons for each node
 *
 * @author Kirill Grouchnikov
 */
public final class MultisonTree <T> {
    /**
     * Single node in multi-son tree
     */
    public static final class MultisonTreeNode <T> {
        private T value;

        // each entry in list is MultisonTreeNode
        private List<MultisonTreeNode<T>> sons;

        /**
         * Create new node with exactly one son
         *
         * @param value new son value
         */
        public MultisonTreeNode(T value) {
            this.value = value;
            this.sons = new LinkedList<MultisonTreeNode<T>>();
        }

        /**
         * Returns value of this node
         *
         * @return value of this node
         */
        public T getValue() {
            return value;
        }

        /**
         * Add son with specified value
         *
         * @param value new son value
         */
        public void addSon(T value) {
            MultisonTreeNode<T> newSon = new MultisonTreeNode<T>(value);
            this.sons.add(newSon);
        }

        /**
         * Add specified son
         *
         * @param sonNode son to add
         */
        public void addSon(MultisonTreeNode<T> sonNode) {
            this.sons.add(sonNode);
        }

        /**
         * Return all sons of this node
         *
         * @return son collection of this node
         */
        public List<MultisonTreeNode<T>> getSons() {
            return sons;
        }
    }

    /**
     * Root node of this tree
     */
    private MultisonTreeNode<T> root;

    /**
     * Create new tree with specified value for the root
     *
     * @param rootValue value of the root node
     */
    public MultisonTree(T rootValue) {
        this.root = new MultisonTreeNode<T>(rootValue);
    }

    /**
     * Get the root node
     *
     * @return the root node
     */
    public MultisonTreeNode<T> getRoot() {
        return root;
    }
}

