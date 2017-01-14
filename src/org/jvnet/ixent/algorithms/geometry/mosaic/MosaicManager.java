package org.jvnet.ixent.algorithms.geometry.mosaic;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import org.jvnet.ixent.algorithms.geometry.voronoi.VoronoiIndexDiagramInfo;
import org.jvnet.ixent.graphics.colors.interpolator.ColorInterpolator;

public final class MosaicManager {
	public static BufferedImage createGradientMosaic(
			BufferedImage bitmapObject,
			VoronoiIndexDiagramInfo voronoiIndexDiagramInfo,
			ColorInterpolator colorInterpolator) {

		long time0 = System.currentTimeMillis();

		// get voronoi diagram info
		int width = voronoiIndexDiagramInfo.getWidth();
		int height = voronoiIndexDiagramInfo.getHeight();
		int[][] diagramIndex = voronoiIndexDiagramInfo.getDiagramIndex();
		Point2D[] centers = voronoiIndexDiagramInfo.getCenters();
		int maxIndex = voronoiIndexDiagramInfo.getMaxIndex();
		int maxRadius = voronoiIndexDiagramInfo.getMaxRadius();

		BufferedImage result = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Point2D centerPoint = centers[diagramIndex[x][y]];
				double xa = centerPoint.getX() - maxRadius;
				double ya = centerPoint.getY();
				double adjustCoef = ((x - xa) * Math.sqrt(3.0) + (y - ya))
						/ maxRadius;

				double colorCoef = 0.6;
				colorCoef += ((double) adjustCoef) / 12.0;

				double coef = 1.0 - (double) x / (double) width;
				Color midColor = colorInterpolator.getInterpolatedColor(coef);
				int midR = midColor.getRed();
				int midG = midColor.getGreen();
				int midB = midColor.getBlue();

				int origValue = bitmapObject.getRGB(x, y);
				int red = (origValue & 0x00FF0000) >> 16;
				int green = (origValue & 0x0000FF00) >> 8;
				int blue = (origValue & 0x000000FF);

				int newR = red + (int) ((double) (midR - red) * colorCoef);
				int newG = green + (int) ((double) (midG - green) * colorCoef);
				int newB = blue + (int) ((double) (midB - blue) * colorCoef);
				if (newR < 0) {
					newR = 0;
				}
				if (newR > 255) {
					newR = 255;
				}
				if (newG < 0) {
					newG = 0;
				}
				if (newG > 255) {
					newG = 255;
				}
				if (newB < 0) {
					newB = 0;
				}
				if (newB > 255) {
					newB = 255;
				}

				result.setRGB(x, y, (255 << 24) | (newR << 16) | (newG << 8)
						| newB);
			}
		}

		long time1 = System.currentTimeMillis();
		System.out.println("Mosaic: " + (time1 - time0));

		return result;
	}
}
