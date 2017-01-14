package org.jvnet.ixent.algorithms.graphics.engine.npr;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

import org.jvnet.ixent.algorithms.graphics.engine.NprEngineBase;
import org.jvnet.ixent.algorithms.graphics.engine.linkinfo.WeightedWrapper;
import org.jvnet.ixent.algorithms.graphics.segmentation.SegmentationInfo;
import org.jvnet.ixent.algorithms.graphics.turbulence.DisplacementMatrix;
import org.jvnet.ixent.graphics.IndexBitmapObject;
import org.jvnet.ixent.math.coord.Polygon2D;
import org.jvnet.ixent.math.coord.Square2D;
import org.jvnet.ixent.math.intersect.ClippingManager;
import org.jvnet.ixent.util.*;

/**
 * NPR engine that creates mosaic tiled effect
 * 
 * @author Kirill Grouchnikov
 */
public class MosaicEngine extends NprEngineBase {
	private Logger logger;

	public enum PixelStatus {
		/**
		 * pixel is marked and has a mosaic square centered somewhere inside it
		 */
		marked,
		/**
		 * pixel is marked and has a mosaic square centered somewhere inside it
		 * that partly overlaps with some of the neighbouring mosaics
		 */
		markedOverlapping,
		/**
		 * pixel is marked and is completely covered by a mosaic square centered
		 * in some other pixel
		 */
		notCandidate,
		/**
		 * pixel is already in queue to check
		 */
		queued,
		/**
		 * pixel is not marked and is a candidate for placing a mosaic square
		 */
		notMarked
	}

	/**
	 * holds pixel status (above enum) for all the pixels during the algorithm
	 */
	private PixelStatus[][] pixelStatus;

	/**
	 * holds a mosaic square (cell tile) for each pixel that is marked as having
	 * such a square. Holds 'null' otherwise
	 */
	private Square2D[][] allocatedSquares;

	/**
	 * an array for finding mosaic sqaures centers
	 */
	private double[] delta = { 0, /* 0.25, -0.25, */0.5, -0.5 };

	/**
	 * Construct mosaic engine
	 */
	public MosaicEngine() {
		super();
	}

	/**
	 * Initialize this engine with all the input information
	 * 
	 * @param pInputImage
	 *            input image
	 * @param pSegmentationLinkInfo
	 *            information on image segmentation
	 * @param pEdgeDetectionLinkInfo
	 *            information on edge detection
	 * @param pInputImageTesselationLinkInfo
	 *            input image tesselation (list of polygons)
	 * @param pStructureVicinityLinkInfo
	 *            structure vicinity map
	 * @param pStructureGradientLinkInfo
	 *            structure gradient map
	 * @param pDisplacementMapLinkInfo
	 *            displacement map
	 * @throws IllegalArgumentException
	 *             if one of the input parameters is null
	 */
	public void init(BufferedImage pInputImage,
			WeightedWrapper<SegmentationInfo> pSegmentationLinkInfo,
			WeightedWrapper<IndexBitmapObject> pEdgeDetectionLinkInfo,
			WeightedWrapper<List<Polygon2D>> pInputImageTesselationLinkInfo,
			WeightedWrapper<IndexBitmapObject> pStructureVicinityLinkInfo,
			WeightedWrapper<IndexBitmapObject> pStructureGradientLinkInfo,
			WeightedWrapper<DisplacementMatrix> pDisplacementMapLinkInfo) {
		super.init(pInputImage, pSegmentationLinkInfo, pEdgeDetectionLinkInfo,
				pInputImageTesselationLinkInfo, pStructureVicinityLinkInfo,
				pStructureGradientLinkInfo, pDisplacementMapLinkInfo);
		this.logger = Logger.getLogger(MosaicEngine.class.getPackage()
				.getName());
	}

	/**
	 * Order all the pixels by degree of vicinity to some image feature. The
	 * result is a collection of lists of pixels. Each list holds all the pixels
	 * lying at the same distance from some image feature. The entries in the
	 * list are shuffled randomly to prevent artifacts around top-left corner of
	 * the resulting image
	 * 
	 * @return A collection of lists of pixels. Each list holds all the pixels
	 *         lying at the same distance from some image feature. The entries
	 *         in the list are shuffled randomly to prevent artifacts around
	 *         top-left corner of the resulting image
	 */
	private Map<Integer, List<DoubleHolder<Integer>>> orderPixelsByStructureVicinity() {
		// find minimum and maximum distance
		IndexBitmapObject distanceObject = this.structureVicinityLinkInfo
				.getLinkObject();
		int minimalDist = distanceObject.getValue(0, 0);
		int maximalDist = minimalDist;
		for (int col = 0; col < this.imageWidth; col++) {
			for (int row = 0; row < this.imageHeight; row++) {
				int currDist = distanceObject.getValue(col, row);
				if (currDist < minimalDist) {
					minimalDist = currDist;
				}
				if (currDist > maximalDist) {
					maximalDist = currDist;
				}
			}
		}

		List<DoubleHolder<Integer>>[] temporary = new List[maximalDist + 1];
		for (int i = 0; i <= maximalDist; i++) {
			temporary[i] = new LinkedList<DoubleHolder<Integer>>();
		}

		for (int col = 0; col < this.imageWidth; col++) {
			for (int row = 0; row < this.imageHeight; row++) {
				int currDist = distanceObject.getValue(col, row);
				temporary[currDist].add(new DoubleHolder<Integer>(col, row));
			}
		}

		// shuffle them
		for (int i = 0; i <= maximalDist; i++) {
			Collections.shuffle(temporary[i]);
		}

		Map<Integer, List<DoubleHolder<Integer>>> result = new HashMap<Integer, List<DoubleHolder<Integer>>>();
		for (int i = 0; i <= maximalDist; i++) {
			result.put(i, temporary[i]);
		}

		return result;
	}

	/**
	 * Compute side of mosaic tile based on the distance to image feature
	 * 
	 * @param distanceToFeature
	 *            distance to image feature
	 * @return side of mosaic tile
	 */
	private double getSide(int distanceToFeature) {
		switch (distanceToFeature) {
		case 0:
		case 1:
		case 2:
			return 3.5;
		case 3:
		case 4:
			return 4;
		default:
			return 5;
		}
	}

	/**
	 * Return a square centered somewhere in given pixel so that it doesn't
	 * intersect (but might touch) all previously allocated squares.
	 * 
	 * @param column
	 *            pixel column
	 * @param row
	 *            pixel row
	 * @return <code>Square2D</code> if succeeded to allocate such a square,
	 *         <code>null</code> if failed
	 */
	private Square2D fitSquare(int column, int row) {
		// compute side of this square
		double side = this.getSide(this.structureVicinityLinkInfo
				.getLinkObject().getValue(column, row));
		int radius = (int) (Math
				.ceil(2.0 * Math.sqrt(0.25 + side * side / 2.0)));
		double angle = this.structureGradientLinkInfo.getLinkObject().getValue(
				column, row);

		// compute all neighbouring squares that can intersect with this square
		List<Square2D> neighbouringSquares = new LinkedList<Square2D>();
		int colStart = Math.max(0, column - radius);
		int colEnd = Math.min(this.imageWidth - 1, column + radius);
		int rowStart = Math.max(0, row - radius);
		int rowEnd = Math.min(this.imageHeight - 1, row + radius);
		for (int currCol = colStart; currCol <= colEnd; currCol++) {
			for (int currRow = rowStart; currRow <= rowEnd; currRow++) {
				if (this.allocatedSquares[currCol][currRow] != null) {
					neighbouringSquares
							.add(this.allocatedSquares[currCol][currRow]);
				}
			}
		}

		// if no neighbours with squares - just create a new square
		if (neighbouringSquares.size() == 0) {
			return new Square2D(new Point2D.Double(column + 0.5, row + 0.5),
					side, angle);
		}

		double startingX = column + 0.5;
		double startingY = row + 0.5;
		int n = this.delta.length;
		for (int i = 0; i < n; i++) {
			double currX = startingX + this.delta[i];
			for (int j = 0; j < n; j++) {
				double currY = startingY + this.delta[j];
				Square2D currSquare = new Square2D(new Point2D.Double(currX,
						currY), side, angle);
				if (!currSquare.intersects(neighbouringSquares, false)) {
					return currSquare;
				}
			}
		}

		// no point found
		return null;
	}

	/**
	 * Return a square centered exactly in the middle given pixel so that it
	 * partly overlaps any adjacent allocated square by no more than given
	 * percentage of (both squares') area
	 * 
	 * @param column
	 *            pixel column
	 * @param row
	 *            pixel row
	 * @return <code>Square2D</code> if succeeded to allocate such a square,
	 *         <code>null</code> if failed
	 */
	private Square2D fitSquare(int column, int row,
			double maxAllowedPercentageOfArea) {
		// compute side of this square
		double side = this.getSide(this.structureVicinityLinkInfo
				.getLinkObject().getValue(column, row) / 2);
		int radius = (int) (Math
				.ceil(2.0 * Math.sqrt(0.25 + side * side / 2.0)));
		double angle = this.structureGradientLinkInfo.getLinkObject().getValue(
				column, row);

		// compute all neighbouring squares that can intersect with this square
		List<Square2D> neighbouringSquares = new LinkedList<Square2D>();
		int colStart = Math.max(0, column - radius);
		int colEnd = Math.min(this.imageWidth - 1, column + radius);
		int rowStart = Math.max(0, row - radius);
		int rowEnd = Math.min(this.imageHeight - 1, row + radius);
		for (int currCol = colStart; currCol <= colEnd; currCol++) {
			for (int currRow = rowStart; currRow <= rowEnd; currRow++) {
				if (this.allocatedSquares[currCol][currRow] != null) {
					neighbouringSquares
							.add(this.allocatedSquares[currCol][currRow]);
				}
			}
		}

		// if no neighbours with squares - just create a new square
		if (neighbouringSquares.size() == 0) {
			return new Square2D(new Point2D.Double(column + 0.5, row + 0.5),
					side, angle);
		}

		Square2D currSquare = new Square2D(new Point2D.Double(column + 0.5,
				row + 0.5), side, angle);
		if (!currSquare.partlyOverlaps(neighbouringSquares,
				maxAllowedPercentageOfArea)) {
			return currSquare;
		}

		// no point found
		return null;
	}

	/**
	 * Start allocating the mosaic squares starting from given pixel. The
	 * allocating process starts from this pixel and proceeds to its neighbours
	 * following these rules:
	 * 
	 * <ul>
	 * 
	 * <li>if unable to allocate mosaic tile at this pixel, take all immediate
	 * neighbours</li>
	 * 
	 * <li>if able to allocate mosaic tile at this pixel, take all immediate
	 * neighbours of the newly allocated mosaic tile</li>
	 * 
	 * </ul>
	 * 
	 * Of all these neighbouring pixels remove:
	 * 
	 * <ul>
	 * 
	 * <li>those that were already processed</li>
	 * 
	 * <li>those that are already marked for processing</li>
	 * 
	 * <li>those that lie at different distance from image feature than the
	 * input</li>
	 * 
	 * </ul>
	 * 
	 * @param columnToStart
	 *            pixel column to start from
	 * @param rowToStart
	 *            pixel row to start from
	 */
	private void processSinglePixel(int columnToStart, int rowToStart) {
		List<DoubleHolder<Integer>> pixels = new LinkedList<DoubleHolder<Integer>>();
		pixels.add(new DoubleHolder<Integer>(columnToStart, rowToStart));

		while (pixels.size() > 0) {
			// get the first pixel
			DoubleHolder<Integer> firstPixel = pixels.remove(0);
			int column = firstPixel.getValue1();
			int row = firstPixel.getValue2();
			this.logger.finest("Process pixel [" + column + ", " + row + "]");
			PixelStatus currStatus = this.pixelStatus[column][row];
			if ((currStatus == PixelStatus.marked)
					|| (currStatus == PixelStatus.notCandidate)) {
				// continue to the next pixel on list (the first is already
				// removed)
				this.logger.finest("Not able to create square");
				continue;
			}

			// try fitting a square
			Square2D squareToFit = this.fitSquare(column, row);
			double minDistance, maxDistance;
			if (squareToFit == null) {
				this.logger.finest("No square fit");
				// failed to fit a square centered in this point.
				// take its unmarked neighbours with the same closeness to
				// image features
				this.pixelStatus[column][row] = PixelStatus.notCandidate;
				minDistance = 0.0;
				maxDistance = 1.5;
			} else {
				// mark this pixel as taken
				this.logger.finest("Square fit: " + squareToFit.toString());
				this.pixelStatus[column][row] = PixelStatus.marked;
				this.allocatedSquares[column][row] = squareToFit;
				// mark all the pixels that are completely covered by this
				// square as
				// not candidates
				int startX = Math.max(0, (int) Math
						.floor(squareToFit.getMinX()));
				int endX = Math.min(this.imageWidth - 1, (int) Math
						.ceil(squareToFit.getMaxX()));
				int startY = Math.max(0, (int) Math
						.floor(squareToFit.getMinY()));
				int endY = Math.min(this.imageHeight - 1, (int) Math
						.ceil(squareToFit.getMaxY()));

				for (int currX = startX; currX <= endX; currX++) {
					for (int currY = startY; currY <= endY; currY++) {
						Square2D currSq = new Square2D(new Point2D.Double(
								currX + 0.5, currY + 0.5), 1.0, 0.0);
						if (squareToFit.contains(currSq, false)) {
							if (this.pixelStatus[currX][currY] == PixelStatus.notMarked) {
								this.pixelStatus[currX][currY] = PixelStatus.notCandidate;
							}
						}
					}
				}

				minDistance = squareToFit.getSide();
				maxDistance = minDistance + 1.5;
			}

			int startColumn = Math.max(0, (int) (Math.floor(column
					- maxDistance)));
			int endColumn = Math.min(this.imageWidth - 1, (int) (Math
					.floor(column + maxDistance)));
			int startRow = Math.max(0, (int) (Math.floor(row - maxDistance)));
			int endRow = Math.min(this.imageHeight - 1, (int) (Math.floor(row
					+ maxDistance)));

			IndexBitmapObject distMatrix = this.structureVicinityLinkInfo
					.getLinkObject();
			for (int currCol = startColumn; currCol <= endColumn; currCol++) {
				double dx = currCol - column;
				for (int currRow = startRow; currRow <= endRow; currRow++) {
					double dy = currRow - row;
					double distance = Math.sqrt(dx * dx + dy * dy);
					if ((distance < minDistance) || (distance > maxDistance)) {
						continue;
					}
					if (this.pixelStatus[currCol][currRow] != PixelStatus.notMarked) {
						continue;
					}
					if (distMatrix.getValue(currCol, currRow) != distMatrix
							.getValue(column, row)) {
						continue;
					}
					pixels.add(new DoubleHolder<Integer>(currCol, currRow));
					this.pixelStatus[currCol][currRow] = PixelStatus.queued;
				}
			}
		}
	}

	/**
	 * Return resulting image
	 * 
	 * @return the resulting image
	 */
	public BufferedImage getResultingImage() {
		long time0 = System.currentTimeMillis();
		// create the following data structure:
		// each element in it contains a list of all pixels that lie at the
		// same distance from some image feature
		// the pixels in each list are shuffled randomly
		// the elements in the data structure are sorted by the distance
		// that is common to all the pixels in each element
		Map<Integer, List<DoubleHolder<Integer>>> distanceBins = this
				.orderPixelsByStructureVicinity();

		// initialize pixel status matrix
		this.pixelStatus = new PixelStatus[this.imageWidth][this.imageHeight];
		for (int col = 0; col < this.imageWidth; col++) {
			for (int row = 0; row < this.imageHeight; row++) {
				this.pixelStatus[col][row] = PixelStatus.notMarked;
			}
		}

		this.allocatedSquares = new Square2D[this.imageWidth][this.imageHeight];

		Set<Integer> distanceSet = distanceBins.keySet();
		List<Integer> orderedDistances = new ArrayList<Integer>();
		for (int currDist : distanceSet) {
			orderedDistances.add(currDist);
		}
		Collections.sort(orderedDistances);
		for (int currDist : orderedDistances) {
			this.logger.finer("Checking all pixels at distance " + currDist);
			List<DoubleHolder<Integer>> pixelsAtThisDistance = distanceBins
					.get(currDist);

			// take the first pixel from the corresponding list
			while (pixelsAtThisDistance.size() > 0) {
				DoubleHolder<Integer> currPixel = pixelsAtThisDistance
						.remove(0);
				int column = currPixel.getValue1();
				int row = currPixel.getValue2();

				if (this.pixelStatus[column][row] == PixelStatus.notMarked) {
					this.logger.finest("Start from pixel [" + column + ", "
							+ row + "]");
					this.processSinglePixel(column, row);
				}
			}
		}

		for (int col = 0; col < this.imageWidth; col++) {
			for (int row = 0; row < this.imageHeight; row++) {
				assert this.pixelStatus[col][row] != PixelStatus.notMarked : "Pixel ["
						+ col + ", " + row + "] is notMarked";
				if (this.pixelStatus[col][row] == PixelStatus.notCandidate) {
					// try to fit overlapping square
					Square2D overlappingSquare = this.fitSquare(col, row, 0.1);
					if (overlappingSquare != null) {
						this.pixelStatus[col][row] = PixelStatus.markedOverlapping;
						this.allocatedSquares[col][row] = overlappingSquare;
					}
				}
			}
		}

		for (int col = 0; col < this.imageWidth; col++) {
			for (int row = 0; row < this.imageHeight; row++) {
				assert this.pixelStatus[col][row] != PixelStatus.notMarked : "Pixel ["
						+ col + ", " + row + "] is notMarked";
				if (this.pixelStatus[col][row] == PixelStatus.marked) {
					assert this.allocatedSquares[col][row] != null : "No square for marked pixel ["
							+ col + ", " + row + "]";
				}
			}
		}

		this.logger.fine("Creating resulting image");
		// TrueColorBitmapManager tcbm = new TrueColorBitmapManager(
		// this.imageWidth, this.imageHeight);
		BufferedImage result = new BufferedImage(this.imageWidth,
				this.imageHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) result.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.black);
		g.fillRect(0, 0, this.imageWidth, this.imageHeight);
		// tcbm.fillRect(0, 0, this.imageWidth, this.imageHeight, 0xFF000000);
		for (int col = 0; col < this.imageWidth; col++) {
			for (int row = 0; row < this.imageHeight; row++) {
				PixelStatus currStatus = this.pixelStatus[col][row];
				if ((currStatus == PixelStatus.marked)
						|| (currStatus == PixelStatus.markedOverlapping)) {
					Square2D currSquare = this.allocatedSquares[col][row];
					// compute average color of this square
					Polygon2D currPolygon = new Polygon2D(currSquare);
					int startX = Math.max(0, (int) Math.floor(currSquare
							.getMinX()));
					int endX = Math.min(this.imageWidth - 1, (int) Math
							.ceil(currSquare.getMaxX()));
					int startY = Math.max(0, (int) Math.floor(currSquare
							.getMinY()));
					int endY = Math.min(this.imageHeight - 1, (int) Math
							.ceil(currSquare.getMaxY()));
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
							int currRGB = (this.inputImage.getRGB(x, y));
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
						Point2D p1 = currSquare.getPoint(0);
						Point2D p2 = currSquare.getPoint(1);
						Point2D p3 = currSquare.getPoint(2);
						Point2D p4 = currSquare.getPoint(3);
						GeneralPath gp = new GeneralPath();
						gp.moveTo((float) p1.getX(), (float) p1.getY());
						gp.lineTo((float) p2.getX(), (float) p2.getY());
						gp.lineTo((float) p3.getX(), (float) p3.getY());
						gp.lineTo((float) p4.getX(), (float) p4.getY());
						gp.lineTo((float) p1.getX(), (float) p1.getY());
						g.draw(gp);
					}
				}
			}
		}
		long time1 = System.currentTimeMillis();

		this.logger.info("Mosaic engine: " + (time1 - time0));

		ImageCreator.createMosaic(this.pixelStatus, this.allocatedSquares,
				this.imageWidth, this.imageHeight);

		g.dispose();
		return result;
	}
}
