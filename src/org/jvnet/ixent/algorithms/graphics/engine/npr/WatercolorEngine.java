package org.jvnet.ixent.algorithms.graphics.engine.npr;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

import org.jvnet.ixent.algorithms.graphics.colorreduction.ColorReductor;
import org.jvnet.ixent.algorithms.graphics.colorreduction.ColorReductorFactory;
import org.jvnet.ixent.algorithms.graphics.engine.NprEngineBase;
import org.jvnet.ixent.algorithms.graphics.engine.linkinfo.WeightedWrapper;
import org.jvnet.ixent.algorithms.graphics.engine.npr.watercolor.*;
import org.jvnet.ixent.algorithms.graphics.segmentation.SegmentationInfo;
import org.jvnet.ixent.algorithms.graphics.turbulence.DisplacementMatrix;
import org.jvnet.ixent.algorithms.graphics.turbulence.PerlinNoiseGenerator;
import org.jvnet.ixent.graphics.IndexBitmapObject;
import org.jvnet.ixent.math.MathConstants;
import org.jvnet.ixent.math.coord.Polygon2D;
import org.jvnet.ixent.math.filters.GaussConvolution;
import org.jvnet.ixent.math.intersect.ClippingManager;
import org.jvnet.ixent.util.ImageCreator;
import org.jvnet.ixent.util.WeightedPixel;

/**
 * NPR engine that creates watercolor effect. Based on <i>"Computer-Generated
 * Watercolor"</i> by Cassidy Curtis, Sean Anderson, Joshua Seims, Kurt
 * Fleischery and David Salesin in 1997 paper.<br>
 * 
 * The input image is converted to glazes based on its segmentation. The initial
 * pigments for each pixel are chosen based on its original color, average
 * colors of all tesselation cells that intersect with it and degree of
 * closeness to the boundaries of the abovementioned tesselation cells.
 * 
 * @author Kirill Grouchnikov
 */
public class WatercolorEngine extends NprEngineBase {
	private static final double MIN_FLUID_CAPACITY = 0.2;
	private static final double MAX_FLUID_CAPACITY = 0.8;
	private static final int MAIN_LOOP_ITERATIONS = 10;
	private static final double WATERCOLOR_VISCOSITY = 0.1;
	private static final double WATERCOLOR_VISCOUS_DRAG = 0.01;
	private static final int RELAXATION_STEPS = 50;
	private static final double RELAXATION_TOLERANCE = 0.01;
	private static final double RELAXATION_FLUID_DISTRIBUTION = 0.1;
	private static final int WET_AREA_BOUNDARIES_EFFECT = 8;
	private static final double WET_AREA_OUTTAKE = 0.02;
	private static final double CAPILLARY_ABSORPTION_RATE = 0.3;
	private static final double CAPILLARY_SATURATION_THRESHOLD = 0.2;
	private static final double CAPILLARY_DIFFUSE_SATURATION = 0.4;
	private static final double CAPILLARY_RECEIVE_SATURATION = 0.2;

	private Logger logger;

	private PaperCellProperties[][] paperProperties;

	private Glaze[] glazes;

	private double[][] tempVelocityX;
	private double[][] tempVelocityY;
	private double[][] tempConcentration;
	private double[][] tempConcentration2;

	/**
	 * A class for holding information on single cell of watercolor paper
	 * medium.
	 */
	private static class PaperCellProperties {
		public double paperHeight;
		public double slopeX;
		public double slopeY;
		public double fluidCapacity;
		public PigmentList pigmentConcentrations;

		public PaperCellProperties() {
			this.paperHeight = 0.0;
			this.slopeX = 0.0;
			this.slopeY = 0.0;
			this.fluidCapacity = 0.0;
			this.pigmentConcentrations = new PigmentList();
		}
	}

	/**
	 * Construct watercolor engine
	 */
	public WatercolorEngine() {
		super();
		this.logger = Logger.getLogger(WatercolorEngine.class.getPackage()
				.getName());
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
	}

	/**
	 * Generate various paper parameters (height, slope, ...)
	 */
	private void generatePaper() {
		this.paperProperties = new PaperCellProperties[this.imageWidth][this.imageHeight];
		for (int col = 0; col < this.imageWidth; col++) {
			for (int row = 0; row < this.imageHeight; row++) {
				this.paperProperties[col][row] = new PaperCellProperties();
			}
		}
		// compute paper height field
		PerlinNoiseGenerator noiseGenerator = new PerlinNoiseGenerator();
		double[][] noise = noiseGenerator.getDenseNormalizedNoise(
				this.imageWidth, this.imageHeight);
		IndexBitmapObject ibo = new IndexBitmapObject(this.imageWidth,
				this.imageHeight);
		for (int col = 0; col < this.imageWidth; col++) {
			for (int row = 0; row < this.imageHeight; row++) {
				ibo.setValue(col, row, (int) (255 * noise[col][row]));
			}
		}
		GaussConvolution gc = new GaussConvolution(2.0, 2);
		IndexBitmapObject smoothed1 = gc.getSmoothedBitmap(ibo);
		GaussConvolution gc2 = new GaussConvolution(2.0, 2);
		IndexBitmapObject smoothed2 = gc2.getSmoothedBitmap(smoothed1);

		double[][] paperHeightField = new double[this.imageWidth][this.imageHeight];
		for (int col = 0; col < this.imageWidth; col++) {
			for (int row = 0; row < this.imageHeight; row++) {
				double alpha = Math.abs(smoothed1.getValue(col, row)
						- smoothed2.getValue(col, row));
				alpha /= 255.0;
				double mmin = 0.0;
				double mmax = 0.9;
				alpha = mmin + alpha * (mmax - mmin);
				paperHeightField[col][row] = alpha;
			}
		}
		for (int col = 0; col < this.imageWidth; col++) {
			for (int row = 0; row < this.imageHeight; row++) {
				this.paperProperties[col][row].paperHeight = paperHeightField[col][row];
			}
		}

		// compute horizontal slope
		for (int col = 0; col < (this.imageWidth - 1); col++) {
			for (int row = 0; row < this.imageHeight; row++) {
				this.paperProperties[col][row].slopeX = 0.4 * (this.paperProperties[col + 1][row].paperHeight - this.paperProperties[col][row].paperHeight);
			}
		}

		// compute vertical slope
		for (int col = 0; col < this.imageWidth; col++) {
			for (int row = 0; row < (this.imageHeight - 1); row++) {
				this.paperProperties[col][row].slopeY = 0.4 * (this.paperProperties[col][row + 1].paperHeight - this.paperProperties[col][row].paperHeight);
			}
		}

		// compute fluid capacity
		for (int col = 0; col < this.imageWidth; col++) {
			for (int row = 0; row < this.imageHeight; row++) {
				this.paperProperties[col][row].fluidCapacity = MIN_FLUID_CAPACITY
						+ this.paperProperties[col][row].paperHeight
						* (MAX_FLUID_CAPACITY - MIN_FLUID_CAPACITY);
			}
		}
	}

	/**
	 * Compute average velocity of water of two adjacent horizontal cells
	 * 
	 * @param glazeIndex
	 *            index of glaze
	 * @param column
	 *            column of left cell
	 * @param row
	 *            row of both cells
	 * @return average velocity of water of two adjacent horizontal cells (the
	 *         right neighbour of the rightmost cell is the leftmost cell)
	 */
	private double getMidVelocityX(int glazeIndex, int column, int row) {
		return 0.5 * (this.glazes[glazeIndex].getWaterVelocityX(column, row) + this.glazes[glazeIndex]
				.getWaterVelocityX((column + 1) % this.imageWidth, row));
	}

	/**
	 * Compute average velocity of water of two adjacent vertical cells
	 * 
	 * @param glazeIndex
	 *            index of glaze
	 * @param column
	 *            column of both cells
	 * @param row
	 *            row of top cell
	 * @return average velocity of water of two adjacent vertical cells (the
	 *         bottom neighbour of the lowermost cell is the upmost cell)
	 */
	private double getMidVelocityY(int glazeIndex, int column, int row) {
		return 0.5 * (this.glazes[glazeIndex].getWaterVelocityY(column, row) + this.glazes[glazeIndex]
				.getWaterVelocityY(column, (row + 1) % this.imageHeight));
	}

	/**
	 * Update velocities in a specified glaze by discretizing shallow-water
	 * equations on staggered grid (section 4.3.1)
	 * 
	 * @param glazeIndex
	 *            the index of the specified glaze
	 */
	private void updateVelocities(int glazeIndex) {
		this.logger.finer("at glaze " + glazeIndex);
		Glaze currGlaze = this.glazes[glazeIndex];
		// update velocities by slope
		double maxAbsoluteVelocity = 0.0;
		for (int col = 0; col < this.imageWidth; col++) {
			for (int row = 0; row < this.imageHeight; row++) {
				PaperCellProperties currCellProperties = this.paperProperties[col][row];
				double prevVelocityX = currGlaze.getWaterVelocityX(col, row);
				double newVelocityX = prevVelocityX + currCellProperties.slopeX;
				currGlaze.setWaterVelocityX(col, row, newVelocityX);
				maxAbsoluteVelocity = Math.max(maxAbsoluteVelocity, Math
						.abs(newVelocityX));
				double prevVelocityY = currGlaze.getWaterVelocityY(col, row);
				double newVelocityY = prevVelocityY + currCellProperties.slopeY;
				currGlaze.setWaterVelocityY(col, row, newVelocityY);
				maxAbsoluteVelocity = Math.max(maxAbsoluteVelocity, Math
						.abs(newVelocityY));
				if (maxAbsoluteVelocity > 1000) {
					System.out.println(maxAbsoluteVelocity);
				}
			}
		}

		// compute discrete solution on staggered grid to shallow water
		// equations
		if (maxAbsoluteVelocity < MathConstants.EPS) {
			return;
		}
		double dt = 1.0 / Math.ceil(maxAbsoluteVelocity);
		for (double t = 0.0; t <= 1.0; t += dt) {
			for (int col = 0; col < this.imageWidth; col++) {
				for (int row = 0; row < this.imageHeight; row++) {
					double uij = currGlaze.getWaterVelocityX(col, row);
					double uip2j = currGlaze.getWaterVelocityX(col + 1, row);
					double uip1j = this.getMidVelocityX(glazeIndex, col, row);
					double uip3j = this.getMidVelocityX(glazeIndex, col + 1,
							row);
					double uim1j = this.getMidVelocityX(glazeIndex, col - 1,
							row);
					double uip1jp2 = this.getMidVelocityX(glazeIndex, col,
							row + 1);
					double uip1jm2 = this.getMidVelocityX(glazeIndex, col,
							row - 1);

					double vij = currGlaze.getWaterVelocityY(col, row);
					double vijp2 = currGlaze.getWaterVelocityY(col, row + 1);
					double vijm1 = this.getMidVelocityY(glazeIndex, col,
							row - 1);
					double vijp1 = this.getMidVelocityY(glazeIndex, col, row);
					double vip2jp1 = this.getMidVelocityY(glazeIndex, col + 1,
							row);
					double vim2jp1 = this.getMidVelocityY(glazeIndex, col - 1,
							row);
					double vijp3 = this.getMidVelocityY(glazeIndex, col,
							row + 1);

					double pij = currGlaze.getWaterPressure(col, row);
					double pip2j = currGlaze.getWaterPressure(col + 1, row);
					double pijp2 = currGlaze.getWaterPressure(col, row + 1);

					double a = uij * uij - uip2j * uip2j + uip1j * vijm1
							- uip1j * vijp1;
					double b = uip3j + uim1j + uip1jp2 + uip1jm2 - 4.0 * uip1j;
					tempVelocityX[col][row] = uip1j
							+ dt
							* (a - WATERCOLOR_VISCOSITY * b + pij - pip2j - WATERCOLOR_VISCOUS_DRAG
									* uip1j);
					assert tempVelocityX[col][row] < 1000 : "not good";
					a = vij * vij - vijp2 * vijp2 + uim1j * vijp1 - uip1j
							* vijp1;
					b = vip2jp1 + vim2jp1 + vijp3 + vijm1 - 4.0 * vijp1;
					tempVelocityY[col][row] = vijp1
							+ dt
							* (a - WATERCOLOR_VISCOSITY * b + pij - pijp2 - WATERCOLOR_VISCOUS_DRAG
									* vijp1);
					assert tempVelocityY[col][row] < 1000 : "not good";
				}
			}
			for (int col = 1; col < this.imageWidth; col++) {
				for (int row = 1; row < this.imageHeight; row++) {
					double newVelocityX = 0.5 * (tempVelocityX[col - 1][row] + tempVelocityX[col][row]);
					assert newVelocityX < 1000 : "not good";
					currGlaze.setWaterVelocityX(col, row, newVelocityX);
					double newVelocityY = 0.5 * (tempVelocityY[col][row - 1] + tempVelocityY[col][row]);
					currGlaze.setWaterVelocityY(col, row, newVelocityY);
					assert newVelocityY < 1000 : "not good";
				}
			}
			// enforce boundary conditions
			currGlaze.enforceBoundaryConditions();
		}
	}

	/**
	 * Relax the divergence of the velocity field for the specified glaze
	 * (section 4.3.2)
	 * 
	 * @param glazeIndex
	 *            the index of the specified glaze
	 */
	private void relaxDivergence(int glazeIndex) {
		this.logger.finer("at glaze " + glazeIndex);
		Glaze currGlaze = this.glazes[glazeIndex];
		int t = 0;
		double deltaMax = 0.0;
		do {
			for (int col = 0; col < this.imageWidth; col++) {
				for (int row = 0; row < this.imageHeight; row++) {
					this.tempVelocityX[col][row] = currGlaze.getWaterVelocityX(
							col, row);
					this.tempVelocityY[col][row] = currGlaze.getWaterVelocityY(
							col, row);
				}
			}
			deltaMax = 0.0;
			for (int col = 0; col < this.imageWidth; col++) {
				for (int row = 0; row < this.imageHeight; row++) {
					double uip1j = this.getMidVelocityX(glazeIndex, col, row);
					double uim1j = this.getMidVelocityX(glazeIndex, col - 1,
							row);
					double vijp1 = this.getMidVelocityY(glazeIndex, col, row);
					double vijm1 = this.getMidVelocityY(glazeIndex, col,
							row - 1);
					double delta = RELAXATION_FLUID_DISTRIBUTION
							* (uip1j - uim1j + vijp1 - vijm1);
					if (col != 0) {
						this.tempVelocityX[col - 1][row] += delta;
					}
					this.tempVelocityX[col][row] -= delta;
					if (row != 0) {
						this.tempVelocityY[col][row - 1] += delta;
					}
					this.tempVelocityY[col][row] -= delta;
					deltaMax = Math.max(deltaMax, delta);
				}
			}
			for (int col = 1; col < this.imageWidth; col++) {
				for (int row = 1; row < this.imageHeight; row++) {
					double newVelocityX = 0.5 * (tempVelocityX[col - 1][row] + tempVelocityX[col][row]);
					assert newVelocityX < 1000 : "not good";
					currGlaze.setWaterVelocityX(col, row, newVelocityX);
					double newVelocityY = 0.5 * (tempVelocityY[col][row - 1] + tempVelocityY[col][row]);
					currGlaze.setWaterVelocityY(col, row, newVelocityY);
					assert newVelocityY < 1000 : "not good";
				}
			}
			t++;
		} while ((deltaMax > RELAXATION_TOLERANCE) && (t < RELAXATION_STEPS));
	}

	/**
	 * Simulate the outward flow (towards the boundaries of the wet-mask) by
	 * evaporating water carrier near the boundaries of the specified glaze
	 * (section 4.3.3)
	 * 
	 * @param glazeIndex
	 *            the index of the specified glaze
	 */
	private void flowOutward(int glazeIndex) {
		this.logger.finer("at glaze " + glazeIndex);
		Glaze currGlaze = this.glazes[glazeIndex];
		currGlaze.computeDistancesToWetMaskBoundary(WET_AREA_BOUNDARIES_EFFECT);
		for (int col = 0; col < this.imageWidth; col++) {
			for (int row = 0; row < this.imageHeight; row++) {
				if (!currGlaze.isInWetMask(col, row)) {
					continue;
				}
				double distToWetMaskBoundary = currGlaze
						.getDistanceToWetMaskBoundary(col, row);
				if (distToWetMaskBoundary == 1.0) {
					continue;
				}
				double oldWaterPressure = currGlaze.getWaterPressure(col, row);
				double newWaterPressure = Math.max(0.0, oldWaterPressure
						- WET_AREA_OUTTAKE * (1.0 - distToWetMaskBoundary));
				currGlaze.setWaterPressure(col, row, newWaterPressure);
			}
		}
	}

	/**
	 * Move water in the shallow water layer of the specified glaze (section
	 * 4.3).
	 * 
	 * @param glazeIndex
	 *            the index of the specified glaze
	 */
	private void moveWater(int glazeIndex) {
		this.updateVelocities(glazeIndex);
		this.relaxDivergence(glazeIndex);
		this.flowOutward(glazeIndex);
	}

	/**
	 * Move pigments in the shallow water layer of the specified glaze (section
	 * 4.4).
	 * 
	 * @param glazeIndex
	 *            the index of the specified glaze
	 */
	private void movePigment(int glazeIndex) {
		this.logger.finer("at glaze " + glazeIndex);
		Glaze currGlaze = this.glazes[glazeIndex];
		// update velocities by slope
		double maxAbsoluteVelocity = 0.0;
		for (int col = 0; col < this.imageWidth; col++) {
			for (int row = 0; row < this.imageHeight; row++) {
				maxAbsoluteVelocity = Math.max(maxAbsoluteVelocity, Math
						.abs(currGlaze.getWaterVelocityX(col, row)));
				maxAbsoluteVelocity = Math.max(maxAbsoluteVelocity, Math
						.abs(currGlaze.getWaterVelocityY(col, row)));
			}
		}
		this.logger.finest("maxAbsoluteVelocity = " + maxAbsoluteVelocity);
		if (maxAbsoluteVelocity < MathConstants.EPS) {
			return;
		}
		double dt = 1.0 / maxAbsoluteVelocity;
		this.logger.finest("dt = " + dt);
		for (Pigment currPigment : Pigment.values()) {
			for (double t = 0.0; t <= 1.0; t += dt) {
				for (int col = 0; col < this.imageWidth; col++) {
					for (int row = 0; row < this.imageHeight; row++) {
						this.tempConcentration[col][row] = currGlaze
								.getPigmentConcentration(col, row, currPigment);
						this.tempConcentration2[col][row] = this.tempConcentration[col][row];
					}
				}
				for (int col = 0; col < this.imageWidth; col++) {
					for (int row = 0; row < this.imageHeight; row++) {
						double val1 = Math.max(0.0, this.getMidVelocityX(
								glazeIndex, col, row)
								* this.tempConcentration2[col][row]);
						if (col != (this.imageWidth - 1)) {
							this.tempConcentration[col + 1][row] += val1;
						}
						double val2 = Math.max(0.0, -this.getMidVelocityX(
								glazeIndex, col - 1, row)
								* this.tempConcentration2[col][row]);
						if (col != 0) {
							this.tempConcentration[col - 1][row] += val2;
						}
						double val3 = Math.max(0.0, this.getMidVelocityY(
								glazeIndex, col, row)
								* this.tempConcentration2[col][row]);
						if (row != (this.imageHeight - 1)) {
							this.tempConcentration[col][row + 1] += val3;
						}
						double val4 = Math.max(0.0, -this.getMidVelocityY(
								glazeIndex, col, row - 1)
								* this.tempConcentration2[col][row]);
						if (row != 0) {
							this.tempConcentration[col][row - 1] += val4;
						}
						this.tempConcentration[col][row] -= (val1 + val2 + val3 + val4);

					}
				}
				for (int col = 0; col < this.imageWidth; col++) {
					for (int row = 0; row < this.imageHeight; row++) {
						currGlaze.setPigmentConcentration(col, row,
								currPigment, this.tempConcentration[col][row]);
					}
				}
			}
		}
	}

	/**
	 * Adsorb pigments by the pigment deposition layer and desorb the pigments
	 * into the fluid in the specified glaze (section 4.5).
	 * 
	 * @param glazeIndex
	 *            the index of the specified glaze
	 */
	private void transferPigment(int glazeIndex) {
		this.logger.finer("at glaze " + glazeIndex);
		Glaze currGlaze = this.glazes[glazeIndex];
		for (Pigment currPigment : Pigment.values()) {
			double pigmDensity = currPigment.getDensity();
			double pigmStainingPower = currPigment.getStainingPower();
			double pigmGranulation = currPigment.getGranulation();
			for (int col = 0; col < this.imageWidth; col++) {
				for (int row = 0; row < this.imageHeight; row++) {
					if (!currGlaze.isInWetMask(col, row)) {
						continue;
					}
					double paperHeight = this.paperProperties[col][row].paperHeight;
					double concentrationInWater = currGlaze
							.getPigmentConcentration(col, row, currPigment);
					double deltaDown = concentrationInWater
							* (1.0 - paperHeight * pigmGranulation)
							* pigmDensity;
					double concentrationOnPaper = this.paperProperties[col][row].pigmentConcentrations
							.getPigmentConcentration(currPigment);
					double deltaUp = concentrationOnPaper
							* (1.0 + (paperHeight - 1.0) * pigmGranulation)
							* pigmDensity / pigmStainingPower;
					if ((concentrationOnPaper + deltaDown) > 1.0) {
						deltaDown = Math.max(0.0, 1.0 - concentrationOnPaper);
					}
					if ((concentrationInWater + deltaUp) > 1.0) {
						deltaUp = Math.max(0.0, 1.0 - concentrationInWater);
					}
					double deltaDiff = deltaDown - deltaUp;
					if (Math.abs(deltaDiff) >= MathConstants.EPS_BIG) {
						this.paperProperties[col][row].pigmentConcentrations
								.setPigment(currPigment, concentrationOnPaper
										+ deltaDiff);
						currGlaze.setPigmentConcentration(col, row,
								currPigment, concentrationInWater - deltaDiff);
					}
				}
			}
		}
	}

	/**
	 * Simulate backruns - diffusing water through the capillary layer of the
	 * specified glaze (section 4.6).
	 * 
	 * @param glazeIndex
	 *            the index of the specified glaze
	 */
	private void simulateCapillaryFlow(int glazeIndex) {
		this.logger.finer("at glaze " + glazeIndex);
		Glaze currGlaze = this.glazes[glazeIndex];
		for (int col = 0; col < this.imageWidth; col++) {
			for (int row = 0; row < this.imageHeight; row++) {
				double oldSaturation = currGlaze.getPaperSaturation(col, row);
				double newSaturation = oldSaturation
						+ Math.max(0.0, Math.min(CAPILLARY_ABSORPTION_RATE,
								this.paperProperties[col][row].fluidCapacity
										- oldSaturation));
				currGlaze.setPaperSaturation(col, row, newSaturation);
				this.tempConcentration[col][row] = newSaturation;
			}
		}
		for (int col = 0; col < this.imageWidth; col++) {
			int xs = Math.max(0, col - 1);
			int xe = Math.min(this.imageWidth - 1, col + 1);
			for (int row = 0; row < this.imageHeight; row++) {
				int ys = Math.max(0, row - 1);
				int ye = Math.min(this.imageHeight - 1, row + 1);
				double currSaturation = currGlaze.getPaperSaturation(col, row);
				for (int neighbourCol = xs; neighbourCol <= xe; neighbourCol++) {
					for (int neighbourRow = ys; neighbourRow <= ye; neighbourRow++) {
						// don't look at the same pixel
						if ((neighbourCol == col) && (neighbourRow == row)) {
							continue;
						}
						double neighbourSaturation = currGlaze
								.getPaperSaturation(neighbourCol, neighbourRow);
						if ((currSaturation > CAPILLARY_DIFFUSE_SATURATION)
								&& (currSaturation > neighbourSaturation)
								&& (neighbourSaturation > CAPILLARY_RECEIVE_SATURATION)) {
							double val1 = currSaturation - neighbourSaturation;
							double val2 = this.paperProperties[neighbourCol][neighbourRow].fluidCapacity
									- neighbourSaturation;
							double deltaS = Math.max(0.0, 0.25 * Math.min(val1,
									val2));
							this.tempConcentration[col][row] -= deltaS;
							this.tempConcentration[neighbourCol][neighbourRow] += deltaS;
						}
					}
				}
			}
		}
		for (int col = 0; col < this.imageWidth; col++) {
			for (int row = 0; row < this.imageHeight; row++) {
				if (tempConcentration[col][row] > CAPILLARY_SATURATION_THRESHOLD) {
					currGlaze.setInWetMask(col, row, true);
				}
				currGlaze.setPaperSaturation(col, row,
						tempConcentration[col][row]);
			}
		}

	}

	/**
	 * Perform the main loop of the simulation for the specified glaze (section
	 * 4.2).
	 * 
	 * @param glazeIndex
	 *            the index of the specified glaze
	 */
	private void runMainLoop(int glazeIndex) {
		this.tempVelocityX = new double[this.imageWidth][this.imageHeight];
		this.tempVelocityY = new double[this.imageWidth][this.imageHeight];
		this.tempConcentration = new double[this.imageWidth][this.imageHeight];
		this.tempConcentration2 = new double[this.imageWidth][this.imageHeight];
		for (int iteration = 0; iteration < MAIN_LOOP_ITERATIONS; iteration++) {
			this.moveWater(glazeIndex);
			this.movePigment(glazeIndex);
			this.transferPigment(glazeIndex);
			this.simulateCapillaryFlow(glazeIndex);
		}
	}

	/**
	 * Return resulting image. Following is the list of influences of various
	 * coefficients (of type <code>WeightKind</code>)
	 * 
	 * <ul>
	 * 
	 * <li><code>weightEdgeDetectionForNPR</code> (taken from
	 * <code>pEdgeDetectionLinkInfo</code> parameter in the constructor) -
	 * influences the strength of "ink" strikes in the resulting image (that
	 * correspond to the edges in the original image). Value 0.0 - no ink
	 * strikes will be present, value 1.0 - full strength ink strikes will be
	 * present.</li>
	 * 
	 * <li><code>weightTesselationForNPR</code> (taken from
	 * <code>pEdgeDetectionLinkInfo</code> parameter in the constructor) -
	 * influences the preprocessing stage of the algorithm. The input image is
	 * first convoluted with Gaussian filter (to remove high frequency image
	 * features) and then tesselated. Average color is then computed for each
	 * tesselation cell. The pigments initially associated with each pixel are
	 * determined based on the original color of this pixel and on the average
	 * color of the tesselation cells it belongs to. The closer a pixel lies to
	 * a cell center, the more it is affected by the average color of this cell.
	 * In addition, if a pixel is covered by more than one tesselation cell,
	 * each cell contributes to the resulting color proportionally. The
	 * abovementioned weight controls the influence of cell average color. Value
	 * 0.0 - only pixel's initial color is accounted for, value 1.0 - full
	 * influence of cell average color (in the center of the cell).</li>
	 * 
	 * <li><code>weightSegmentationForNPR</code> (taken from
	 * <code>pSegmentationLinkInfo</code> parameter in the constructor) -
	 * influences the partition of the input image into glazes. Value 0.0 - the
	 * whole image is represented by a single glaze, any other value - each
	 * segment will produce a distinct glaze (that will be composited using
	 * Kubelka-Munk model).</li>
	 * 
	 * <li><code>weightDisplacementForNPR</code> (taken from
	 * <code>pSegmentationLinkInfo</code> parameter in the constructor) - not
	 * used.</li>
	 * 
	 * <li><code>weightStructureVicinityForNPR</code> (taken from
	 * <code>pStructureVicinityLinkInfo</code> parameter in the constructor) -
	 * not used.</li>
	 * 
	 * <li><code>weightStructureGradientForNPR</code> (taken from
	 * <code>pStructureGradientLinkInfo</code> parameter in the constructor) -
	 * not used.</li>
	 * 
	 * </ul>
	 * 
	 * @return the resulting image
	 */
	public BufferedImage getResultingImage() {
		long time0 = System.currentTimeMillis();
		BufferedImage result = new BufferedImage(this.imageWidth,
				this.imageHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) result.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.black);
		g.fillRect(0, 0, this.imageWidth, this.imageHeight);

		this.logger.info("Smoothing the input image");
		GaussConvolution gaussConvolution = new GaussConvolution(2.0, 3);
		BufferedImage smoothedImage = gaussConvolution
				.getSmoothedImage(this.inputImage);

		// compute average colors for the tesselated input image
		this.logger.info("Creating average tesselation colors");
		for (Polygon2D currPolygon : this.inputImageTesselationLinkInfo
				.getLinkObject()) {
			int startX = Math.max(0, (int) Math.floor(currPolygon.getMinX()));
			int endX = Math.min(this.imageWidth - 1, (int) Math
					.ceil(currPolygon.getMaxX()));
			int startY = Math.max(0, (int) Math.floor(currPolygon.getMinY()));
			int endY = Math.min(this.imageHeight - 1, (int) Math
					.ceil(currPolygon.getMaxY()));
			double dimension = currPolygon.getMinimalHalfDimension();
			double sumW = 0.0;
			double sumR = 0.0;
			double sumG = 0.0;
			double sumB = 0.0;
			List<WeightedPixel> pixels = new LinkedList<WeightedPixel>();
			for (int x = startX; x <= endX; x++) {
				for (int y = startY; y <= endY; y++) {
					double area = ClippingManager.intersectionArea(currPolygon,
							new Point2D.Double(x, y), new Point2D.Double(x + 1,
									y + 1));
					if (area == 0.0) {
						continue;
					}
					sumW += area;
					int currRGB = (smoothedImage.getRGB(x, y));
					int red = (currRGB & 0x00FF0000) >> 16;
					int green = (currRGB & 0x0000FF00) >> 8;
					int blue = (currRGB & 0x000000FF);
					sumR += (area * red);
					sumG += (area * green);
					sumB += (area * blue);
					double distToPolygonBoundary = currPolygon
							.getDistanceToPoint(new Point2D.Double(x + 0.5,
									y + 0.5));
					assert (distToPolygonBoundary <= dimension) : "invalid distance to polygon";
					pixels.add(new WeightedPixel(x, y, area,
							distToPolygonBoundary / dimension));
				}
			}
			if (sumW > 0.0) {
				int averageR = (int) (sumR / sumW);
				int averageG = (int) (sumG / sumW);
				int averageB = (int) (sumB / sumW);
				for (WeightedPixel currPixel : pixels) {
					int currRGB = (smoothedImage.getRGB(currPixel.getColumn(),
							currPixel.getRow()));
					int origR = (currRGB & 0x00FF0000) >> 16;
					int origG = (currRGB & 0x0000FF00) >> 8;
					int origB = (currRGB & 0x000000FF);

					// account for the influence of tesselation info
					double coef1 = this.inputImageTesselationLinkInfo
							.getWeight();
					int weightedR = (int) (origR + coef1 * (averageR - origR));
					int weightedG = (int) (origG + coef1 * (averageG - origG));
					int weightedB = (int) (origB + coef1 * (averageB - origB));

					double distanceToBoundary = currPixel.getAdditionalValue();
					// 0.0 -> orig
					// 1.0 -> average
					double coef2 = distanceToBoundary;
					int finalR = (int) (origR + coef2 * (weightedR - origR));
					int finalG = (int) (origG + coef2 * (weightedG - origG));
					int finalB = (int) (origB + coef2 * (weightedB - origB));
					int newColor = (255 << 24) | (finalR << 16) | (finalG << 8)
							| finalB;
					g.setColor(new Color(newColor));
					g.setComposite(AlphaComposite.getInstance(
							AlphaComposite.SRC_OVER, (float) currPixel
									.getArea()));

					g.drawLine(currPixel.getColumn(), currPixel.getRow(),
							currPixel.getColumn(), currPixel.getRow());
//					tcbm.blendPixel(currPixel.getColumn(), currPixel.getRow(),
//							newColor, currPixel.getArea());
				}
			}
		}
//		BufferedImage tesselatedImage = tcbm.getBitmapObject();

		// quantize the input image
		this.logger.info("Quantizing the tesselated image");
		ColorReductor colorReductor = ColorReductorFactory
				.getColorReductor(result);
		colorReductor.process(256);
		IndexBitmapObject quantizedBitmap = colorReductor.getValueMap2D();
		Color[] quantizationColors = colorReductor.getQuantizationColors();

		// compute the best approximation of each quantization color by one
		// or more pigments
		this.logger.info("Computing initial pigment approximation");
		List<WeightedWrapper<Pigment>>[] quantizationColorsApproximation = new List[quantizationColors.length];
		PigmentMatcher pigmentMatcher = new PigmentMatcher();
		for (int index = 0; index < quantizationColors.length; index++) {
			quantizationColorsApproximation[index] = pigmentMatcher
					.matchPigments(quantizationColors[index]);
		}

		// if (true)
		// return ImageCreator.createQuantizedPigments(quantizedBitmap,
		// quantizationColorsApproximation);

		// generate various paper parameters
		this.logger.info("Generating paper");
		this.generatePaper();

		// allocate and initialize the glazes
		this.logger.info("Initiliazing glazes");
		int glazeCount = 0;
		if ((this.segmentationLinkInfo.getLinkObject() == null)
				|| (this.segmentationLinkInfo.getWeight() == 0.0)) {
			this.logger.info("Creating single glaze");
			glazeCount = 1;
			this.glazes = new Glaze[1];
			this.glazes[0] = new Glaze(quantizedBitmap,
					quantizationColorsApproximation, 0.3);
		} else {
			IndexBitmapObject segmentationAreasBitmap = this.segmentationLinkInfo
					.getLinkObject().getAreasBitmap();
			glazeCount = this.segmentationLinkInfo.getLinkObject()
					.getNumberOfSegments();
			this.logger.info("Creating " + glazeCount + " glazes");
			this.glazes = new Glaze[glazeCount];
			for (int glIndex = 0; glIndex < glazeCount; glIndex++) {
				glazes[glIndex] = new Glaze(segmentationAreasBitmap, glIndex,
						quantizedBitmap, quantizationColorsApproximation, 0.3);
			}
		}

		// run simulation for each glaze
		this.logger.info("Running simulation");
		for (int glIndex = 0; glIndex < glazeCount; glIndex++) {
			this.runMainLoop(glIndex);
		}

		// perform Kubelka-Munk model to compose the glazes
		this.logger.info("Performing glaze composing");
		for (int col = 0; col < this.imageWidth; col++) {
			for (int row = 0; row < this.imageHeight; row++) {
				PigmentList allPigments = new PigmentList();
				// get all pigments from all glazes
				for (Glaze currGlaze : this.glazes) {
					allPigments.combine(currGlaze.getAllPigmentConcentrations(
							col, row));
				}
				// and from the shallow-water layer
				allPigments
						.combine(this.paperProperties[col][row].pigmentConcentrations);
				List<WeightedWrapper<Pigment>> pigmentList = new LinkedList<WeightedWrapper<Pigment>>();
				double totalPigmentConcentration = 0.0;
				for (Pigment currPigment : Pigment.values()) {
					double currConcentration = allPigments
							.getPigmentConcentration(currPigment);
					if (currConcentration > MathConstants.EPS) {
						totalPigmentConcentration += currConcentration;
						pigmentList.add(new WeightedWrapper<Pigment>(
								currPigment, currConcentration));
					}
				}
				if (totalPigmentConcentration > 1.0) {
					totalPigmentConcentration = 1.0;
				}
				Map<Pigment.Component, Double> totalReflectance = new HashMap<Pigment.Component, Double>();
				Map<Pigment.Component, Double> totalTransmittance = new HashMap<Pigment.Component, Double>();
				for (Pigment.Component currComponent : Pigment.Component
						.values()) {
					totalReflectance.put(currComponent, 0.0);
					totalTransmittance.put(currComponent, 1.0);
				}

				for (WeightedWrapper<Pigment> currWrapper : pigmentList) {
					Pigment currPigment = currWrapper.getLinkObject();
					double currConcentration = currWrapper.getWeight();
					for (Pigment.Component currComponent : Pigment.Component
							.values()) {
						double reflectance = currPigment.getReflectance(
								currComponent, currConcentration);
						double transmittance = currPigment.getTransmittance(
								currComponent, currConcentration);
						double r1 = totalReflectance.get(currComponent);
						double r2 = reflectance;
						double t1 = totalTransmittance.get(currComponent);
						double t2 = transmittance;
						double newReflectance = r1 + (t1 * t1 * r2)
								/ (1.0 - r1 * r2);
						double newTransmittance = t1 * t2 / (1.0 - r1 * r2);
						totalReflectance.put(currComponent, newReflectance);
						totalTransmittance.put(currComponent, newTransmittance);
					}
				}

				double totalReflectanceR = Math.min(1.0, totalReflectance
						.get(Pigment.Component.red));
				double totalReflectanceG = Math.min(1.0, totalReflectance
						.get(Pigment.Component.green));
				double totalReflectanceB = Math.min(1.0, totalReflectance
						.get(Pigment.Component.blue));
				// combine with white color of the paper
				int finalComponentR = (int) (totalPigmentConcentration
						* totalReflectanceR * 255.0 + (1.0 - totalPigmentConcentration) * 255.0);
				int finalComponentG = (int) (totalPigmentConcentration
						* totalReflectanceG * 255.0 + (1.0 - totalPigmentConcentration) * 255.0);
				int finalComponentB = (int) (totalPigmentConcentration
						* totalReflectanceB * 255.0 + (1.0 - totalPigmentConcentration) * 255.0);
				int finalColor = (255 << 24) | (finalComponentR << 16)
						| (finalComponentG << 8) | finalComponentB;
				result.setRGB(col, row, finalColor);
			}
		}

		// add "ink" edges
		IndexBitmapObject edges = this.edgeDetectionLinkInfo.getLinkObject();
		double edgeCoef = this.edgeDetectionLinkInfo.getWeight() / 255.0;
		g.setColor(Color.black);
		for (int col = 0; col < this.imageWidth; col++) {
			for (int row = 0; row < this.imageHeight; row++) {
				int edgePresence = edges.getValue(col, row);
				if (edgePresence > 0) {
					g.setComposite(AlphaComposite.getInstance(
							AlphaComposite.SRC_OVER, edgePresence / 256.0f));
					g.drawLine(col, row, col, row);
					// tcbmFinal.blendPixel(col, row, 0xFF000000, edgeCoef
					// * edgePresence);
				}
			}
		}

		// add paper creases
		// for (int col = 0; col < this.imageWidth; col++) {
		// for (int row = 0; row < this.imageHeight; row++) {
		// g.setComposite(AlphaComposite.getInstance(
		// AlphaComposite.SRC_OVER, paperHeight/256.0f));
		// tcbmFinal.blendPixel(col, row, 0xFF000000,
		// this.paperProperties[col][row].paperHeight);
		// }
		// }

		long time1 = System.currentTimeMillis();

		this.logger.info("Watercolor engine: " + (time1 - time0));

		// BufferedImage result = tcbmFinal.getBitmapObject();
		ImageCreator.paintProgress(result);

		g.dispose();
		return result;
	}
}
