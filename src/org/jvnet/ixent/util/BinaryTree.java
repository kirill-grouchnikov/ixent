package org.jvnet.ixent.util;


/**
 * A tree with unlimited number of sons for each node
 *
 * @author Kirill Grouchnikov
 */
public final class BinaryTree <T> {
    /**
     * Single node in multi-son tree
     */
    public static final class BinaryTreeNode <T> {
        private T value;

        private BinaryTreeNode<T> left;
        private BinaryTreeNode<T> right;

        /**
         * Create new node with exactly one son
         *
         * @param value new son value
         */
        public BinaryTreeNode(T value) {
            this.value = value;
            this.left = null;
            this.right = null;
        }

        /**
         * Set left son with specified value
         *
         * @param value new left son value
         */
        public void setLeft(T value) {
            this.left = new BinaryTreeNode<T>(value);
        }

        /**
         * Set left son with specified value
         *
         * @param value new left son value
         */
        public void setRight(T value) {
            this.right = new BinaryTreeNode<T>(value);
        }

        public T getValue() {
            return value;
        }

        public BinaryTreeNode<T> getLeft() {
            return left;
        }

        public BinaryTreeNode<T> getRight() {
            return right;
        }
    }

    /**
     * Root node of this tree
     */
    private BinaryTreeNode<T> root;

    /**
     * Create new tree with specified value for the root
     *
     * @param rootValue value of the root node
     */
    public BinaryTree(T rootValue) {
        this.root = new BinaryTreeNode<T>(rootValue);
    }

    /**
     * Get the root node
     *
     * @return the root node
     */
    public BinaryTreeNode<T> getRoot() {
        return root;
    }
}

