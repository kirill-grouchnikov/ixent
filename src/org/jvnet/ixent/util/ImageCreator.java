package org.jvnet.ixent.util;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

import org.jvnet.ixent.ImageCallback;
import org.jvnet.ixent.algorithms.graphics.engine.linkinfo.WeightedWrapper;
import org.jvnet.ixent.algorithms.graphics.engine.npr.MosaicEngine;
import org.jvnet.ixent.algorithms.graphics.engine.npr.watercolor.Glaze;
import org.jvnet.ixent.algorithms.graphics.engine.npr.watercolor.Pigment;
import org.jvnet.ixent.graphics.IndexBitmapObject;
import org.jvnet.ixent.math.coord.Polygon2D;
import org.jvnet.ixent.math.coord.Square2D;
import org.jvnet.ixent.math.intersect.ClippingManager;

/**
 * @author Kirill Grouchnikov
 */
public class ImageCreator {
	// public static boolean toCreateJpegs = false;
	public static BufferedImage originalImage;
	public static double ratio;

	public static ImageCallback imageCallback;

	private static Logger logger = Logger.getLogger(ImageCreator.class
			.getPackage().getName());

	public static void paintProgress(BufferedImage tcbo) {
		imageCallback.imageUpdated(tcbo);
	}

	public static BufferedImage createWithEdges(BufferedImage inputImage,
			IndexBitmapObject edgeObject) {

		int width = inputImage.getWidth();
		int height = inputImage.getHeight();
		BufferedImage result = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) result.createGraphics();
		// TrueColorBitmapManager tcbm = new TrueColorBitmapManager(width,
		// height);
		IndexBitmapObject ibo = IndexBitmapObject.getAsGreyscale(inputImage);
		int[][] greys = ibo.getBitmap();

		// ImageUtilities.overlayGreyscaleIndexObject(result, originalImage
		// .getAsGreyscale());
		// tcbm.overlayGreyscaleIndexObject(originalImage.getAsGreyscale());
		g.setColor(Color.blue);
		for (int col = 0; col < width; col++) {
			for (int row = 0; row < height; row++) {
				int greyV = greys[col][row];
				result.setRGB(col, row, (255 << 24) | (greyV << 16)
						| (greyV << 8) | greyV);
				if (edgeObject.getValue(col, row) > 0) {
					float alpha = edgeObject.getValue(col, row) / 256.0f;
					g.setComposite(AlphaComposite.getInstance(
							AlphaComposite.SRC_OVER, alpha));
					g.drawLine(col, row, col, row);
					g.setComposite(AlphaComposite.SrcOver);
				}
			}
		}

		// BufferedImage tcbo = tcbm.getBitmapObject();
		paintProgress(result);
		return result;
	}

	public static BufferedImage createWithSegments(BufferedImage inputImage,
			IndexBitmapObject segmentationObject) {
		int width = inputImage.getWidth();
		int height = inputImage.getHeight();
		BufferedImage result = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) result.createGraphics();
		// TrueColorBitmapManager tcbm = new TrueColorBitmapManager(width,
		// height);
		IndexBitmapObject ibo = IndexBitmapObject.getAsGreyscale(inputImage);
		int[][] greys = ibo.getBitmap();

		for (int col = 0; col < width; col++) {
			for (int row = 0; row < height; row++) {
				int greyV = greys[col][row];
				result.setRGB(col, row, (255 << 24) | (greyV << 16)
						| (greyV << 8) | greyV);
				if (segmentationObject.getValue(col, row) > 0) {
					float alpha = segmentationObject.getValue(col, row) / 256.0f;
					g.setComposite(AlphaComposite.getInstance(
							AlphaComposite.SRC_OVER, alpha));
					g.drawLine(col, row, col, row);
					// tcbm.blendPixel(col, row, Color.blue.getRGB(), alpha);
					g.setComposite(AlphaComposite.SrcOver);
				}
			}
		}

		// BufferedImage tcbo = tcbm.getBitmapObject();
		paintProgress(result);
		g.dispose();
		return result;
	}

	public static BufferedImage createWithGradient(BufferedImage inputImage,
			IndexBitmapObject structureGradientMap) {
		int width = inputImage.getWidth();
		int height = inputImage.getHeight();
		BufferedImage result = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = (Graphics2D) result.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.white);
		g.fillRect(0, 0, width, height);
		g.setColor(Color.black);

		for (int col = 0; col < width; col += 4) {
			for (int row = 0; row < height; row += 4) {
				double direction = structureGradientMap.getValue(col, row);
				//
				// int v = (int) ((direction < 90) ? 255 - direction
				// : 75 + direction);
				// int color = (255 << 24) | (v << 16) | (v << 8) | v;
				// result.setRGB(col, row, color);
				double sin = Math.sin(direction * Math.PI / 90.0);
				double cos = Math.cos(direction * Math.PI / 90.0);
				double dx = 2.0 * cos;
				double dy = 2.0 * sin;
				GeneralPath gp = new GeneralPath();
				gp.moveTo(col - dx, row - dy);
				gp.lineTo(col + dx, row + dy);
				g.draw(gp);
			}
		}

		paintProgress(result);
		return result;
	}

	// public static BufferedImage createWithPolygons(BufferedImage inputImage,
	// List<Polygon2D> polygons) {
	// int width = inputImage.getWidth();
	// int height = inputImage.getHeight();
	// BufferedImage result = new BufferedImage(width, height,
	// BufferedImage.TYPE_INT_ARGB);
	// Graphics2D g = (Graphics2D) result.createGraphics();
	// // TrueColorBitmapManager tcbm = new TrueColorBitmapManager(width,
	// // height);
	// IndexBitmapObject ibo = IndexBitmapObject.getAsGreyscale(inputImage);
	//
	// g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	// RenderingHints.VALUE_ANTIALIAS_ON);
	//
	// int[][] greys = ibo.getBitmap();
	//
	// for (int col = 0; col < width; col++) {
	// for (int row = 0; row < height; row++) {
	// int greyV = greys[col][row];
	// result.setRGB(col, row, (255 << 24) | (greyV << 16)
	// | (greyV << 8) | greyV);
	// }
	// }
	//
	// Set<Point2D> allPoints = new HashSet<Point2D>();
	// g.setColor(Color.blue);
	// for (Polygon2D currPolygon : polygons) {
	// Point2D[] points = currPolygon.getPoints();
	// for (int i = 0; i < points.length; i++) {
	// Point2D currPoint = points[i];
	// Point2D nextPoint = (i == (points.length - 1)) ? points[0]
	// : points[i + 1];
	// allPoints.add(currPoint);
	// int x1 = (int) currPoint.getX();
	// int y1 = (int) currPoint.getY();
	// int x2 = (int) nextPoint.getX();
	// int y2 = (int) nextPoint.getY();
	// g.drawLine(x1, y1, x2, y2);
	// }
	// }
	//
	// g.setStroke(new BasicStroke(2.0f));
	// g.setColor(Color.green);
	// for (Point2D currVertex : allPoints) {
	// int cx = (int) currVertex.getX();
	// int cy = (int) currVertex.getY();
	// g.drawLine(cx, cy, cx, cy);
	// // g.blendFillCircle(cx, cy, 0.5, 0xFF00FF00);
	// }
	//
	// paintProgress(result);
	// g.dispose();
	// return result;
	// }

	public static BufferedImage createWithPolygons(List<Polygon2D> polygons) {
		int origWidth = originalImage.getWidth();
		int origHeight = originalImage.getHeight();
		BufferedImage result = new BufferedImage(origWidth, origHeight,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) result.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		// TrueColorBitmapManager tcbm = new TrueColorBitmapManager(origWidth,
		// origHeight);
		// tcbm.overlayGreyscaleIndexObject(originalImage.getAsGreyscale());

		IndexBitmapObject ibo = IndexBitmapObject.getAsGreyscale(originalImage);
		for (int col = 0; col < ibo.getWidth(); col++) {
			for (int row = 0; row < ibo.getHeight(); row++) {
				int val = ibo.getValue(col, row);
				int color = (255 << 24) | (val << 16) | (val << 8) | val;
				result.setRGB(col, row, color);
			}
		}

		Set<Point2D> allPoints = new HashSet<Point2D>();
		g.setColor(Color.blue);
		if (polygons != null) {
			for (Polygon2D currPolygon : polygons) {
				Point2D[] points = currPolygon.getPoints();
				for (int i = 0; i < points.length; i++) {
					Point2D currPoint = points[i];
					Point2D nextPoint = (i == (points.length - 1)) ? points[0]
							: points[i + 1];
					allPoints.add(currPoint);
					int x1 = (int) (ratio * currPoint.getX());
					int y1 = (int) (ratio * currPoint.getY());
					int x2 = (int) (ratio * nextPoint.getX());
					int y2 = (int) (ratio * nextPoint.getY());
					g.drawLine(x1, y1, x2, y2);
				}
			}
		}
		g.setColor(Color.green);
		g.setStroke(new BasicStroke(2.0f));
		for (Point2D currVertex : allPoints) {
			int cx = (int) (ratio * currVertex.getX());
			int cy = (int) (ratio * currVertex.getY());
			g.drawLine(cx, cy, cx, cy);
		}

		// BufferedImage tcbo = tcbm.getBitmapObject();
		paintProgress(result);
		g.dispose();
		return result;
	}

	public static BufferedImage createQuantized(
			IndexBitmapObject quantizedIndexes, Color[] quantizationColors) {
		int width = quantizedIndexes.getWidth();
		int height = quantizedIndexes.getHeight();
		// TrueColorBitmapManager tcbm = new TrueColorBitmapManager(width,
		// height);
		BufferedImage result = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		for (int col = 0; col < width; col++) {
			for (int row = 0; row < height; row++) {
				result.setRGB(col, row, quantizationColors[quantizedIndexes
						.getValue(col, row)].getRGB());
			}
		}
		paintProgress(result);
		return result;
	}

	public static BufferedImage createQuantizedPigments(
			IndexBitmapObject quantizedIndexes,
			List<WeightedWrapper<Pigment>>[] quantizationColorsApproximation) {
		int width = quantizedIndexes.getWidth();
		int height = quantizedIndexes.getHeight();
		BufferedImage result = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) result.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		// TrueColorBitmapManager tcbm = new TrueColorBitmapManager(width,
		// height);
		for (int col = 0; col < width; col++) {
			for (int row = 0; row < height; row++) {
				int colorIndex = quantizedIndexes.getValue(col, row);
				List<WeightedWrapper<Pigment>> pigmentList = quantizationColorsApproximation[colorIndex];
				double weightWhite = 1.0;
				double red = 0.0;
				double green = 0.0;
				double blue = 0.0;
				for (WeightedWrapper<Pigment> currPigment : pigmentList) {
					double w = currPigment.getWeight();
					weightWhite -= w;
					Pigment pigm = currPigment.getLinkObject();
					red += w * pigm.getComponent(Pigment.Component.red);
					green += w * pigm.getComponent(Pigment.Component.green);
					blue += w * pigm.getComponent(Pigment.Component.blue);
				}
				red += weightWhite * 255;
				green += weightWhite * 255;
				blue += weightWhite * 255;
				red = Math.min(255, red);
				green = Math.min(255, green);
				blue = Math.min(255, blue);
				int color = (255 << 24) | ((int) red << 16)
						| ((int) green << 8) | (int) blue;
				result.setRGB(col, row, color);
			}
		}
		// BufferedImage tcbo = tcbm.getBitmapObject();
		paintProgress(result);
		return result;
	}

	public static BufferedImage createGlaze(int width, int height,
			Glaze[] glazes) {
		BufferedImage result = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		// Graphics2D g = (Graphics2D) result.createGraphics();
		// g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		// RenderingHints.VALUE_ANTIALIAS_ON);
		// TrueColorBitmapManager tcbm = new TrueColorBitmapManager(width,
		// height);
		for (int col = 0; col < width; col++) {
			for (int row = 0; row < height; row++) {
				double weightWhite = 1.0;
				double red = 0.0;
				double green = 0.0;
				double blue = 0.0;
				for (Glaze currGlaze : glazes) {
					for (Pigment currPigment : Pigment.values()) {
						double concentration = currGlaze
								.getPigmentConcentration(col, row, currPigment);
						weightWhite -= concentration;
						red += concentration
								* currPigment
										.getComponent(Pigment.Component.red);
						green += concentration
								* currPigment
										.getComponent(Pigment.Component.green);
						blue += concentration
								* currPigment
										.getComponent(Pigment.Component.blue);
					}
				}
				red += weightWhite * 255;
				green += weightWhite * 255;
				blue += weightWhite * 255;
				red = Math.min(255, red);
				green = Math.min(255, green);
				blue = Math.min(255, blue);
				int color = (255 << 24) | ((int) red << 16)
						| ((int) green << 8) | (int) blue;
				result.setRGB(col, row, color);
			}
		}

		// BufferedImage tcbo = tcbm.getBitmapObject();
		paintProgress(result);
		return result;
	}

	// public static TrueColorBitmapObject createFlame(int width, int height,
	// List<FlameTongue> flames) {
	// TrueColorBitmapManager tcbm = new TrueColorBitmapManager(width, height);
	// tcbm.resetImage(0xFF000000);
	//
	// IndexBitmapManager ibm = new IndexBitmapManager(width, height);
	// for (FlameTongue flame : flames) {
	// int dx = flame.getOriginColumn();
	// int dy = flame.getOriginRow();
	// Map<Integer, List<WeightedPixel>> path = flame.getPath();
	// Set<Integer> keys = path.keySet();
	// for (Integer currKey : keys) {
	// // compute total energy at this distance
	// double energy = flame.getEnergyAtDistance(currKey);
	// List<WeightedPixel> pixels = path.get(currKey);
	// for (WeightedPixel currPixel : pixels) {
	// // tcbm.blendPixel(currPixel.getColumn(), currPixel.getRow(),
	// // 0xffffffff, currPixel.getArea());
	// ibm.intensifyPixel(currPixel.getColumn()+dx, currPixel.getRow()+dy,
	// (int) (255.0 * energy * currPixel.getArea()));
	// }
	// }
	// }
	// Map<Color, Integer> primaryColors = new HashMap<Color, Integer>();
	// Map<ColorManager.ColorKind, Integer> markerIndexes = new
	// HashMap<ColorManager.ColorKind, Integer>();
	// primaryColors.put(Color.white, 255);
	// primaryColors.put(Color.yellow, 245);
	// primaryColors.put(new Color(132, 99, 0), 155);
	// primaryColors.put(new Color(64, 50, 0), 55);
	// primaryColors.put(Color.black, 0);
	// markerIndexes.put(ColorManager.ColorKind.light, 240);
	// markerIndexes.put(ColorManager.ColorKind.medium, 200);
	// markerIndexes.put(ColorManager.ColorKind.dark, 10);
	// markerIndexes.put(ColorManager.ColorKind.master, 128);
	// ColorManager particleColorManager = new ColorManagerGeneral(
	// primaryColors,
	// markerIndexes);
	// GaussConvolution gaussConvolution = new GaussConvolution(1.0, 2);
	//
	// TrueColorBitmapObject res =
	// new TrueColorBitmapObject(new IndexBitmapObject(
	// ibm.getAsBitmap(),
	// width, height), particleColorManager);
	// return res;
	// }

	public static BufferedImage createMosaic(
			MosaicEngine.PixelStatus[][] pixelStatus,
			Square2D[][] allocatedSquares, int width, int height) {

		int origWidth = originalImage.getWidth();
		int origHeight = originalImage.getHeight();
		// TrueColorBitmapManager tcbm = new TrueColorBitmapManager(origWidth,
		// origHeight);
		// tcbm.fillRect(0, 0, origWidth, origHeight, 0xFF000000);
		BufferedImage result = new BufferedImage(origWidth, origHeight,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) result.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.black);
		g.fillRect(0, 0, width, height);

		for (int col = 0; col < width; col++) {
			for (int row = 0; row < height; row++) {
				MosaicEngine.PixelStatus currStatus = pixelStatus[col][row];
				if ((currStatus == MosaicEngine.PixelStatus.marked)
						|| (currStatus == MosaicEngine.PixelStatus.markedOverlapping)) {
					Square2D currSquare = allocatedSquares[col][row];
					Point2D oldCenter = currSquare.getCenter();
					Square2D newSquare = new Square2D(new Point2D.Double(ratio
							* oldCenter.getX(), ratio * oldCenter.getY()),
							ratio * currSquare.getSide(), currSquare
									.getAngleInDegrees());

					// compute average color of this square
					Polygon2D currPolygon = new Polygon2D(newSquare);
					int startX = Math.max(0, (int) Math.floor(newSquare
							.getMinX()));
					int endX = Math.min(origWidth - 1, (int) Math
							.ceil(newSquare.getMaxX()));
					int startY = Math.max(0, (int) Math.floor(newSquare
							.getMinY()));
					int endY = Math.min(origHeight - 1, (int) Math
							.ceil(newSquare.getMaxY()));
					double sumW = 0.0;
					double sumR = 0.0;
					double sumG = 0.0;
					double sumB = 0.0;
					List<WeightedPixel> pixels = new LinkedList<WeightedPixel>();
					for (int x = startX; x <= endX; x++) {
						for (int y = startY; y <= endY; y++) {
							double area = ClippingManager.intersectionArea(
									currPolygon, new Point2D.Double(x, y),
									new Point2D.Double(x + 1, y + 1));
							if (area == 0.0) {
								continue;
							}
							sumW += area;
							int currRGB = (originalImage.getRGB(x, y));
							int red = (currRGB & 0x00FF0000) >> 16;
							int green = (currRGB & 0x0000FF00) >> 8;
							int blue = (currRGB & 0x000000FF);
							sumR += (area * red);
							sumG += (area * green);
							sumB += (area * blue);
							pixels.add(new WeightedPixel(x, y, area));
						}
					}
					if (sumW > 0.0) {
						int finalR = (int) (sumR / sumW);
						int finalG = (int) (sumG / sumW);
						int finalB = (int) (sumB / sumW);
						int newColor = (255 << 24) | (finalR << 16)
								| (finalG << 8) | finalB;
						g.setColor(new Color(newColor));
						for (WeightedPixel currPixel : pixels) {
							g.setComposite(AlphaComposite.getInstance(
									AlphaComposite.SRC_OVER, (float) currPixel
											.getArea()));
							g.drawLine(currPixel.getColumn(), currPixel
									.getRow(), currPixel.getColumn(), currPixel
									.getRow());
							// tcbm.blendPixel(currPixel.getColumn(), currPixel
							// .getRow(), newColor, currPixel.getArea());
						}
					}
					g.setColor(Color.black);
					g.setComposite(AlphaComposite.getInstance(
							AlphaComposite.SRC_OVER, 0.5f));
					if (currStatus == MosaicEngine.PixelStatus.markedOverlapping) {
						Point2D p1 = newSquare.getPoint(0);
						Point2D p2 = newSquare.getPoint(1);
						Point2D p3 = newSquare.getPoint(2);
						Point2D p4 = newSquare.getPoint(3);
						GeneralPath gp = new GeneralPath();
						gp.moveTo((float) p1.getX(), (float) p1.getY());
						gp.lineTo((float) p2.getX(), (float) p2.getY());
						gp.lineTo((float) p3.getX(), (float) p3.getY());
						gp.lineTo((float) p4.getX(), (float) p4.getY());
						gp.lineTo((float) p1.getX(), (float) p1.getY());
						g.draw(gp);
						// g.drawLine(p1.getX(), p1.getY)
						// tcbm.blendLine(p1, p2, 0xFF000000, 0.5);
						// tcbm.blendLine(p2, p3, 0xFF000000, 0.5);
						// tcbm.blendLine(p3, p4, 0xFF000000, 0.5);
						// tcbm.blendLine(p4, p1, 0xFF000000, 0.5);
					}
				}
			}
		}
		// BufferedImage tcbo = tcbm.getBitmapObject();
		paintProgress(result);
		g.dispose();
		return result;
	}
}
