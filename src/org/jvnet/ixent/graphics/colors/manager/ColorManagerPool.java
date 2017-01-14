package org.jvnet.ixent.graphics.colors.manager;

import java.awt.Color;
import java.util.*;

public final class ColorManagerPool {
    private List<ColorManager> colorManagerList;
    private Iterator<ColorManager> currIterator;

    public ColorManagerPool() {
        this.colorManagerList = new LinkedList<ColorManager>();
        // cyan-sky blue
        this.colorManagerList.add(new ColorManager3ColorScheme(Color.blue, Color.cyan,
                Color.blue, new Color(0, 0, 128)));
        // golden-brown
        this.colorManagerList.add(new ColorManager3ColorScheme(
                new Color(132, 99, 0), new Color(255, 255, 132),
                new Color(132, 99, 0), new Color(64, 50, 0)));
        // lilac-violet
        this.colorManagerList.add(new ColorManager3ColorScheme(
                new Color(254, 112, 254), new Color(228, 194, 228),
                new Color(254, 112, 254), new Color(50, 0, 50)));
        // lime-green
        this.colorManagerList.add(new ColorManager3ColorScheme(
                new Color(0, 240, 0), new Color(140, 255, 140),
                new Color(0, 240, 0), new Color(0, 102, 0)));
        // yellow-red
        this.colorManagerList.add(new ColorManager3ColorScheme(Color.red, new Color(
                255, 242, 0),
                Color.red, new Color(128, 0, 0)));

        this.currIterator = this.colorManagerList.iterator();
    }

    public ColorManager getCurr() {
//        return (NPRManager) this.nprManagerList.getCurr();
        ColorManager currManager = this.currIterator.next();
        if (!this.currIterator.hasNext()) {
            this.currIterator = this.colorManagerList.iterator();
        }
        return currManager;
    }
}

