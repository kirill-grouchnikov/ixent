package org.jvnet.ixent.algorithms.geometry.delaunay.locator;

import java.util.*;

import org.jvnet.ixent.graphics.objects.Triangle;
import org.jvnet.ixent.math.coord.Rectangle2D;
import org.jvnet.ixent.math.coord.Vertex2D;
import org.jvnet.ixent.util.MultisonTree;

public final class PointLocatorHistoryDAG implements PointLocator {
    private Vertex2D[] originalVertices;

    private MultisonTree<Triangle> triangleTree;

    public PointLocatorHistoryDAG() {
    }

    // locatorHelperObject1 points to a leaf node in the tree that
    // points to this triangle
    public void init(Rectangle2D boundingRectangle,
                     Vertex2D[] originalVertices, Triangle superTriangle,
                     boolean isExact) {

        this.originalVertices = originalVertices;
        this.triangleTree = new MultisonTree<Triangle>(superTriangle);
        superTriangle.setLocatorHelperObject1(this.triangleTree.getRoot());
    }

    public Triangle locate(int vertexIndex) {
//    System.out.println("locating " + this.originalVertices[vertexIndex]);
        // start searching from root downwards (until leaf)
        MultisonTree.MultisonTreeNode currNode = this.triangleTree.getRoot();
        while (currNode.getSons().size() > 0) {
            // find son that contains the point
//            ObjectList.ObjectElement currSonNodeWrapper = currNode.sons.head;
//            while (currSonNodeWrapper != null) {
            Iterator it = currNode.getSons().iterator();
            boolean found = false;
            while (it.hasNext()) {
                MultisonTree.MultisonTreeNode currSonNode =
                        (MultisonTree.MultisonTreeNode) it.next();
                Triangle currSonTriangle = (Triangle) currSonNode.getValue();
//        System.out.println("Checking triangle " + currSonTriangle.id);
                if (currSonTriangle.isPointInside(
                        this.originalVertices[vertexIndex])) {
//          System.out.println("found - taking it");
                    currNode = currSonNode;
                    found = true;
                    break;
                }
//                currSonNodeWrapper = currSonNodeWrapper.next;
            }
            if (!found) {
                System.out.println("No triangle found");
                return null;
            }
        }
        return (Triangle) currNode.getValue();
    }

    public void onDeleteTriangles(List<Triangle> triangles) {
    }

    public void onInsertTriangle(Triangle triangle) {
    }

    public void onReplaceTriangles(List<Triangle> oldTriangles,
                                   List<Triangle> newTriangles) {
/*
    System.out.println("Replacing");
    ObjectList.ObjectElement curr = oldTriangles.head;
    while (curr != null) {
      ((Triangle)curr.value).dump();
      curr = curr.next;
    }
    System.out.println("with");
    curr = newTriangles.head;
    while (curr != null) {
      ((Triangle)curr.value).dump();
      curr = curr.next;
    }
*/
        // add each new triangle as a son of each old triangle
        // each old triangle points to a leaf node containing it

        // create a list of leafs for each new triangle
        List<MultisonTree.MultisonTreeNode<Triangle>> leafList =
                new LinkedList<MultisonTree.MultisonTreeNode<Triangle>>();
        for (Triangle currNewTriangle : newTriangles) {
            MultisonTree.MultisonTreeNode<Triangle> currNewLeaf =
                    new MultisonTree.MultisonTreeNode<Triangle>(
                            currNewTriangle);
//                    this.triangleTree.createNewLeaf(currNewTriangle);
            currNewTriangle.setLocatorHelperObject1(currNewLeaf);
            leafList.add(currNewLeaf);
        }

        for (Triangle currOldTriangle : oldTriangles) {
            Object obj1 = currOldTriangle.getLocatorHelperObject1();
            MultisonTree.MultisonTreeNode<Triangle> treeNode =
                    (MultisonTree.MultisonTreeNode<Triangle>) obj1;
            for (MultisonTree.MultisonTreeNode<Triangle> currNewLeaf : leafList) {
                treeNode.addSon(currNewLeaf);
            }
        }
    }

    public void dumpInfo() {
    }
}

