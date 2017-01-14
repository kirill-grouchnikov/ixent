package org.jvnet.ixent.util;

import java.awt.geom.Point2D;
import java.util.LinkedList;

import org.jvnet.ixent.graphics.objects.Triangle;
import org.jvnet.ixent.math.coord.Rectangle2D;
import org.jvnet.ixent.math.coord.Vertex2D;

public final class QuadTree {
    public final class QuadTreeNode {
        public int level;

        public double xL;
        public double xR;
        public double yT;
        public double yB;

        public LinkedList<Triangle> triangles;

        public QuadTreeNode father;

        public boolean hasSons;
        public QuadTreeNode sonLT;
        public QuadTreeNode sonLB;
        public QuadTreeNode sonRT;
        public QuadTreeNode sonRB;

        public QuadTreeNode(int maxLevel, int level, double skew,
                            QuadTreeNode father, double xL, double xR,
                            double yT, double yB) {

            this.level = level;
            this.xL = xL;
            this.xR = xR;
            this.yT = yT;
            this.yB = yB;

            this.triangles = new LinkedList<Triangle>();

            this.father = father;
            if (this.level < maxLevel) {
                double xM = (xL + xR) / 2.0;
                double dx = skew * (xR - xL) / 2.0;
                double yM = (yT + yB) / 2.0;
                double dy = skew * (yB - yT) / 2.0;
                this.sonLT =
                        new QuadTreeNode(maxLevel, level + 1, skew, this, xL,
                                xM + dx, yT, yM + dy);
                this.sonLB =
                        new QuadTreeNode(maxLevel, level + 1, skew, this, xL,
                                xM + dx, yM + dy, yB);
                this.sonRT =
                        new QuadTreeNode(maxLevel, level + 1, skew, this,
                                xM + dx, xR, yT, yM + dy);
                this.sonRB =
                        new QuadTreeNode(maxLevel, level + 1, skew, this,
                                xM + dx, xR, yM + dy, yB);
                this.hasSons = true;
            }
            else {
                this.sonLT = null;
                this.sonLB = null;
                this.sonRT = null;
                this.sonRB = null;
                this.hasSons = false;
            }
        }

        public boolean isInside(double xL, double xR, double yT, double yB) {
            if ((xL < this.xL) || (xL > this.xR)) {
                return false;
            }
            if ((xR < this.xL) || (xR > this.xR)) {
                return false;
            }
            if ((yT < this.yT) || (yT > this.yB)) {
                return false;
            }
            if ((yB < this.yT) || (yB > this.yB)) {
                return false;
            }
            return true;
        }

        public boolean isInside(double x, double y) {
            if ((x < this.xL) || (x > this.xR)) {
                return false;
            }
            if ((y < this.yT) || (y > this.yB)) {
                return false;
            }
            return true;
        }

        public void insert(Triangle triangle) {
            Point2D circCenter = triangle.getCircCenter();
            double circRadius = triangle.getCircRadius();
            double xL = circCenter.getX() - circRadius;
            double xR = circCenter.getX() + circRadius;
            double yT = circCenter.getY() - circRadius;
            double yB = circCenter.getY() + circRadius;
            QuadTreeNode qtn = this.locate(xL, xR, yT, yB);
            qtn.add(triangle);
        }

        public void add(Triangle triangle) {
            this.triangles.add(triangle);
            triangle.setLocatorHelperObject1(this);
            triangle.setLocatorHelperObject2(this.triangles.getFirst());
        }

        public QuadTreeNode locate(Triangle triangle) {
            Point2D circCenter = triangle.getCircCenter();
            double circRadius = triangle.getCircRadius();
            double xL = circCenter.getX() - circRadius;
            double xR = circCenter.getX() + circRadius;
            double yT = circCenter.getY() - circRadius;
            double yB = circCenter.getY() + circRadius;
            return this.locate(xL, xR, yT, yB);
        }

        public QuadTreeNode locate(double xL, double xR, double yT, double yB) {
            if (this.hasSons) {
                // check sons
                if (this.sonLT.isInside(xL, xR, yT, yB)) {
                    return this.sonLT.locate(xL, xR, yT, yB);
                }
                if (this.sonLB.isInside(xL, xR, yT, yB)) {
                    return this.sonLB.locate(xL, xR, yT, yB);
                }
                if (this.sonRT.isInside(xL, xR, yT, yB)) {
                    return this.sonRT.locate(xL, xR, yT, yB);
                }
                if (this.sonRB.isInside(xL, xR, yT, yB)) {
                    return this.sonRB.locate(xL, xR, yT, yB);
                }
                return this;
            }
            else {
                return this;
            }
        }

        public void delete(Triangle triangle) {
            this.triangles.remove(
                    (Triangle) triangle.getLocatorHelperObject2());
        }

        public QuadTreeNode getLeaf(double x, double y) {
            if (!hasSons) {
                return this;
            }

            if (this.sonLT.isInside(x, y)) {
                return this.sonLT.getLeaf(x, y);
            }
            if (this.sonLB.isInside(x, y)) {
                return this.sonLB.getLeaf(x, y);
            }
            if (this.sonRT.isInside(x, y)) {
                return this.sonRT.getLeaf(x, y);
            }
            if (this.sonRB.isInside(x, y)) {
                return this.sonRB.getLeaf(x, y);
            }
            return null;
        }

        public Triangle locate(Vertex2D point, boolean isExact) {
//      System.out.println("locate : " + isExact);
            // check in all triangles at this level
//            ObjectList.ObjectElement currElem = this.triangles.head;
//            while (currElem != null) {
            for (Triangle currTriangle : this.triangles) {
//                Triangle currTriangle = (Triangle) currElem.value;
                if (isExact) {
                    if (currTriangle.isPointInside(point)) {
                        return currTriangle;
                    }
                }
                else {
                    if (currTriangle.isPointInCircumcircle(point)) {
                        return currTriangle;
                    }
                }
//                currElem = currElem.next;
            }
            // go to father
            if (this.father != null) {
                return father.locate(point, isExact);
            }

            // not good
            return null;
        }

        public Triangle locateExceptRootLevel(Vertex2D point, boolean isExact) {
//      System.out.println("locateExceptRootLevel : " + isExact);
            if (this.father == null) {
                return null;
            }
            // check in all triangles at this level
//            ObjectList.ObjectElement currElem = this.triangles.head;
//            while (currElem != null) {
            for (Triangle currTriangle : this.triangles) {
//                Triangle currTriangle = (Triangle) currElem.value;
                if (isExact) {
                    if (currTriangle.isPointInside(point)) {
                        return currTriangle;
                    }
                }
                else {
                    if (currTriangle.isPointInCircumcircle(point)) {
                        return currTriangle;
                    }
                }
//                currElem = currElem.next;
            }
            // go to father
            return father.locate(point, isExact);
        }

        public long size() {
            long result = this.triangles.size();
            if (this.hasSons) {
                result += this.sonLT.size();
                result += this.sonLB.size();
                result += this.sonRT.size();
                result += this.sonRB.size();
            }
            return result;
        }

        public long count() {
            if (!this.hasSons) {
                return 1;
            }
            return 1 + this.sonLT.count() + this.sonLB.count() +
                    this.sonRT.count() +
                    this.sonRB.count();
        }
    }

    private QuadTreeNode root;
    private QuadTreeNode rootSkew;
    private boolean isExact;

    public QuadTree(Rectangle2D boundingRectangle, int maxLevels,
                    boolean isExact) {
        this.isExact = isExact;
        long time0 = System.currentTimeMillis();
        this.root = new QuadTreeNode(maxLevels, 0, 0.0, null,
                boundingRectangle.getPointTL().getX(),
                boundingRectangle.getPointBR().getX(),
                boundingRectangle.getPointTL().getY(),
                boundingRectangle.getPointBR().getY());
        this.rootSkew = new QuadTreeNode(maxLevels, 0, 0.2, null,
                boundingRectangle.getPointTL().getX(),
                boundingRectangle.getPointBR().getX(),
                boundingRectangle.getPointTL().getY(),
                boundingRectangle.getPointBR().getY());
        long time1 = System.currentTimeMillis();
//    System.out.println("Created quadtrees with " + maxLevels + " levels in " + (time1-time0));
    }

    public void insert(Triangle triangle) {
        QuadTreeNode regularNode = this.root.locate(triangle);
        QuadTreeNode skewNode = this.rootSkew.locate(triangle);
        if (regularNode.level >= skewNode.level) {
//      System.out.println("Adding to regular at level " + regularNode.level);
            regularNode.add(triangle);
        }
        else {
//      System.out.println("Adding to skew at level " + skewNode.level);
            skewNode.add(triangle);
        }
//    this.root.insert(triangle);
    }

    public void delete(Triangle triangle) {
        ((QuadTreeNode) triangle.getLocatorHelperObject1()).delete(triangle);
    }

    // isExact:
    //   true - locate triangle that contains the point
    //   false - locate any triangle whose circumcircle contains the point
    public Triangle locate(Vertex2D point) {
        // search from regular leaf upwards
//    System.out.println("Searching in regular");
        QuadTreeNode leaf = this.root.getLeaf(point.getX(), point.getY());
        Triangle tri1 = leaf.locateExceptRootLevel(point, this.isExact);
        if (tri1 != null) {
//      System.out.println("Regular found");
            return tri1;
        }

        // search from skew leaf upwards
//    System.out.println("Searching in skew");
        QuadTreeNode leafSkew = this.rootSkew.getLeaf(point.getX(),
                point.getY());
        Triangle tri2 = leafSkew.locateExceptRootLevel(point, this.isExact);
        if (tri2 != null) {
//      System.out.println("Skew found");
            return tri2;
        }

        // search in regular root triangle list
//    System.out.println("Searching in regular root");
        return this.root.locate(point, isExact);
    }

    public void dumpInfo() {
        System.out.println("REGULAR QUADTREE");
        System.out.println(
                this.root.triangles.size() + " triangles at top level");
        System.out.println(this.root.size() + " triangles at all levels");
        System.out.println(this.root.count() + " nodes in quad tree");

        if (this.root.hasSons) {
            System.out.println("LT: (" + this.root.sonLT.xL + ", " +
                    this.root.sonLT.yT +
                    ")-(" +
                    +this.root.sonLT.xR + ", " + this.root.sonLT.yB + ")");
            System.out.println("LB: (" + this.root.sonLB.xL + ", " +
                    this.root.sonLB.yT +
                    ")-(" +
                    +this.root.sonLB.xR + ", " + this.root.sonLB.yB + ")");
            System.out.println("RT: (" + this.root.sonRT.xL + ", " +
                    this.root.sonRT.yT +
                    ")-(" +
                    +this.root.sonRT.xR + ", " + this.root.sonRT.yB + ")");
            System.out.println("RB: (" + this.root.sonRB.xL + ", " +
                    this.root.sonRB.yT +
                    ")-(" +
                    +this.root.sonRB.xR + ", " + this.root.sonRB.yB + ")");
        }

        System.out.println("SKEW QUADTREE");
        System.out.println(
                this.rootSkew.triangles.size() + " triangles at top level");
        System.out.println(this.rootSkew.size() + " triangles at all levels");
        System.out.println(this.rootSkew.count() + " nodes in quad tree");

        if (this.root.hasSons) {
            System.out.println("LT: (" + this.rootSkew.sonLT.xL + ", " +
                    this.rootSkew.sonLT.yT +
                    ")-(" +
                    +this.rootSkew.sonLT.xR + ", " +
                    this.rootSkew.sonLT.yB +
                    ")");
            System.out.println("LB: (" + this.rootSkew.sonLB.xL + ", " +
                    this.rootSkew.sonLB.yT +
                    ")-(" +
                    +this.rootSkew.sonLB.xR + ", " +
                    this.rootSkew.sonLB.yB +
                    ")");
            System.out.println("RT: (" + this.rootSkew.sonRT.xL + ", " +
                    this.rootSkew.sonRT.yT +
                    ")-(" +
                    +this.rootSkew.sonRT.xR + ", " +
                    this.rootSkew.sonRT.yB +
                    ")");
            System.out.println("RB: (" + this.rootSkew.sonRB.xL + ", " +
                    this.rootSkew.sonRB.yT +
                    ")-(" +
                    +this.rootSkew.sonRB.xR + ", " +
                    this.rootSkew.sonRB.yB +
                    ")");
        }

//    this.root.triangles.dump();
    }
}

