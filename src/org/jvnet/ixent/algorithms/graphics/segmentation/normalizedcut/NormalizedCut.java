package org.jvnet.ixent.algorithms.graphics.segmentation.normalizedcut;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.*;

import org.jvnet.ixent.algorithms.graphics.general.KMeans;
import org.jvnet.ixent.graphics.IndexBitmapObject;
import org.jvnet.ixent.math.MathConstants;
import org.jvnet.ixent.math.coord.PointND;
import org.jvnet.ixent.math.filters.*;
import org.jvnet.ixent.math.matrix.Matrix;
import org.jvnet.ixent.math.matrix.SparseMatrix;

public class NormalizedCut implements EigenChecker {
	private Applet app;

	private BufferedImage inputImage;

	private int width, height;

	private boolean isFirstEigen;

	private double bestEigenValue;

	private double[] bestEigenVector;

	private double bestNCut;

	private int bestIteration;

	private NCWeightMatrix weightMatrix;

	public NormalizedCut(Applet app, BufferedImage image, int width, int height) {
		this.app = app;
		this.width = width;
		this.height = height;
		this.inputImage = image;
	}

	private SparseMatrix getWeightMap() {
		Convolver convolver = new Convolver(IndexBitmapObject
				.getAsGreyscale(this.inputImage));

		long time0 = System.currentTimeMillis();
		Filter[] filters = FilterBank.getFilters(1.0, 1.0, 0, 0, 1, 1);
		PointND[][] responses = convolver.convolve(filters);
		long time1 = System.currentTimeMillis();
		System.out.println("Creating and convolving " + filters.length
				+ " filters : " + (time1 - time0));
		PointND[] responseVector = new PointND[this.width * this.height];
		for (int i = 0; i < this.width; i++) {
			for (int j = 0; j < this.height; j++) {
				responseVector[j * this.width + i] = responses[i][j];
			}
		}
		KMeans km = new KMeans(responseVector, filters.length, 12);
		PointND[] centers = km.getCenters(filters.length, 100);

		Color[] colors = new Color[] { Color.red, Color.blue, Color.green,
				Color.yellow, Color.black, Color.cyan, Color.darkGray,
				Color.lightGray, Color.magenta, Color.orange, Color.pink,
				Color.white };

		int[][] bitmap = new int[this.width][this.height];
		for (int i = 0; i < this.width; i++) {
			for (int j = 0; j < this.height; j++) {
				int index = j * this.width + i;
				int closestCenter = km.getClosestCenterIndex(index);
				bitmap[i][j] = colors[closestCenter].getRGB();
			}
		}

		int n = this.height * this.width;
		SparseMatrix smmBig = new SparseMatrix(n, n);
		int c = 10;
		for (int i = 0; i < n; i++) {
			int row = i / this.width;
			int col = i % this.width;
			int center = km.getClosestCenterIndex(i);
			double d = 0.0;
			// choose neighbours
			Map<Integer, Double> neighbours = new HashMap<Integer, Double>();
			for (int j = 0; j < c; j++) {
				int dr = 5 - (int) (10 * Math.random());
				int dc = 5 - (int) (10 * Math.random());
				int nr = row + dr;
				int nc = col + dc;
				if ((nr < 0) || (nr >= this.height) || (nc < 0)
						|| (nc >= this.width)) {
					continue;
				}
				// check if already setLocation
				if (smmBig.get(nc, nr) > 0.0) {
					continue;
				}
				int ni = nr * this.width + nc;
				int nCenter = km.getClosestCenterIndex(ni);
				double w = (center == nCenter) ? 0.9 : 0.1;
				d += w;
				// smmBig.setLocation(i, ni, w);
				// smmBig.setLocation(ni, i, w);
				neighbours.put(ni, w);
			}
			neighbours.put(i, 0.9);

			Set keys = neighbours.keySet();
			for (Iterator it = keys.iterator(); it.hasNext();) {
				Integer currKey = (Integer) it.next();
				Double currVal = (Double) neighbours.get(currKey);
				int ni = currKey.intValue();
				double w = currVal.doubleValue();
				double fw;
				if (i == ni) {
					fw = (d - w) / d;
				} else {
					fw = -w / d;
				}
				smmBig.set(i, ni, fw);
				smmBig.set(ni, i, fw);
			}
			neighbours = null;
			// smmBig.setLocation(i, i, (0.9-w));
		}
		long startTime = System.currentTimeMillis();
		System.out.println("Set : " + (startTime - time0));
		return smmBig;
	}

	private SparseMatrix getWeightMap2() {
		long time0 = System.currentTimeMillis();
		IndexBitmapObject ibo = IndexBitmapObject
				.getAsGreyscale(this.inputImage);
		int[][] greys = ibo.getBitmap();

		int n = this.height * this.width;
		SparseMatrix smmBig = new SparseMatrix(n, n);
		int c = 20;
		for (int i = 0; i < n; i++) {
			int row = i / this.width;
			int col = i % this.width;
			int thisGrey = greys[col][row];
			double d = 0.0;
			// choose neighbours
			Map<Integer, Double> neighbours = new HashMap<Integer, Double>();
			for (int j = 0; j < c; j++) {
				int dr = 5 - (int) (10 * Math.random());
				int dc = 5 - (int) (10 * Math.random());
				int nr = row + dr;
				int nc = col + dc;
				if ((nr < 0) || (nr >= this.height) || (nc < 0)
						|| (nc >= this.width)) {
					continue;
				}
				// check if already setLocation
				if (smmBig.get(nc, nr) > 0.0) {
					continue;
				}
				int ni = nr * this.width + nc;
				int currGrey = greys[nc][nr];
				double diffGrey = currGrey - thisGrey;
				double w = Math
						.exp(-((diffGrey * diffGrey) / 1000.0 + (dr * dr + dc
								* dc) / 400.0));
				if (w < 0.001) {
					continue;
				}
				d += w;
				// smmBig.setLocation(i, ni, w);
				// smmBig.setLocation(ni, i, w);
				neighbours.put(ni, w);
			}
			neighbours.put(i, Math.E);
			d += Math.E;

			Set keys = neighbours.keySet();
			for (Iterator it = keys.iterator(); it.hasNext();) {
				Integer currKey = (Integer) it.next();
				Double currVal = (Double) neighbours.get(currKey);
				int ni = currKey.intValue();
				double w = currVal.doubleValue();
				double fw = (i == ni) ? ((d - w) / d) : (-w / d);
				smmBig.set(i, ni, fw);
				smmBig.set(ni, i, fw);
			}
			neighbours = null;
			// smmBig.setLocation(i, i, (0.9-w));
		}
		long startTime = System.currentTimeMillis();
		System.out.println("Set : " + (startTime - time0));
		return smmBig;
	}

	private SparseMatrix getWeightMap4(int n) {
		long time0 = System.currentTimeMillis();
		int n1 = n / 2;
		int n2 = n * n;
		SparseMatrix smmBig = new NCWeightMatrix(n2, n2);

		int c = 100;
		double[] d = new double[n2];
		for (int i = 0; i < n2; i++) {
			d[i] = 0.0;
		}

		for (int i = 0; i < n2; i++) {
			int row = i / n;
			int col = i % n;
			// choose neighbours
			// Map neighbours = new HashMap();
			// neighbours.put(new Integer(i), new Double(Math.E));
			smmBig.set(i, i, 1.0);
			d[i] += 1.0;
			for (int dr = -n; dr <= n; dr++) {
				for (int dc = -n; dc <= n; dc++) {

					// for (int j = 0; j < c; j++) {
					// int dr = n - (int) (n2 * Math.random());
					// int dc = n - (int) (n2 * Math.random());
					int nr = row + dr;
					int nc = col + dc;
					if ((nr < 0) || (nr >= n) || (nc < 0) || (nc >= n)) {
						continue;
					}

					if ((col < n1) && (nc >= n1)) {
						continue;
					}
					if ((nc < n1) && (col >= n1)) {
						continue;
					}
					double w1 = Math.exp((-Math.abs(dr) - Math.abs(dc)) / 16.0);

					// check if already setLocation
					int ni = nr * n + nc;
					if (smmBig.get(i, ni) != 0.0) {
						continue;
					}
					// if (neighbours.containsKey(new Integer(ni)))
					// continue;
					// d += w1;
					smmBig.set(i, ni, w1);
					d[i] += w1;
					smmBig.set(ni, i, w1);
					d[ni] += w1;
					// neighbours.put(new Integer(ni), new Double(w1));
				}
			}

			// Set keys = neighbours.keySet();
			// for (Iterator it = keys.iterator(); it.hasNext();) {
			// Integer currKey = (Integer) it.next();
			// Double currVal = (Double) neighbours.get(currKey);
			// int ni = currKey.intValue();
			// double w = currVal.doubleValue();
			// double fw = (i==ni)?((d-w)/d):(-w/d);
			// smmBig.setLocation(i, ni, fw);
			// smmBig.setLocation(ni, i, fw);
			// }
			// neighbours = null;
			//
			// smmBig.setLocation(i, i, (0.9-w));
		}
		for (int i = 0; i < n2; i++) {
			for (int j = 0; j < n2; j++) {
				double w = smmBig.get(j, i);
				if (i == j) {
					double nw = (d[i] - w) / d[i];
					smmBig.set(i, i, nw);
				} else {
					if (w != 0.0) {
						double nw = -w / d[i];
						smmBig.set(j, i, nw);
					}
				}
			}
		}
		long startTime = System.currentTimeMillis();
		System.out.println("Set : " + (startTime - time0));
		return smmBig;
	}

	private NCWeightMatrix getWWeightMap4(int n) {
		long time0 = System.currentTimeMillis();
		int n1 = n / 2;
		int n2 = n * n;
		NCWeightMatrix smmBig = new NCWeightMatrix(n2, n2);

		for (int i = 0; i < n2; i++) {
			int row = i / n;
			int col = i % n;
			// choose neighbours
			smmBig.set(i, i, 1.0);
			for (int dr = -n; dr <= n; dr++) {
				for (int dc = -n; dc <= n; dc++) {

					int nr = row + dr;
					int nc = col + dc;
					if ((nr < 0) || (nr >= n) || (nc < 0) || (nc >= n)) {
						continue;
					}

					if ((col < n1) && (nc >= n1)) {
						continue;
					}
					if ((nc < n1) && (col >= n1)) {
						continue;
					}
					double w1 = Math.exp((-Math.abs(dr) - Math.abs(dc)) / 16.0);

					// check if already setLocation
					int ni = nr * n + nc;
					if (smmBig.get(i, ni) != 0.0) {
						continue;
					}
					smmBig.set(i, ni, w1);
					smmBig.set(ni, i, w1);
				}
			}
		}
		return smmBig;
	}

	private NCWeightMatrix getWWeightMap7(int n, int minRadius,
			int vicinityRadius, double percentage) {
		long time0 = System.currentTimeMillis();
		int n1 = n / 2;
		int n2 = n * n;
		NCWeightMatrix smmBig = new NCWeightMatrix(n2, n2);

		for (int i = 0; i < n2; i++) {
			int row = i / n;
			int col = i % n;
			smmBig.set(i, i, 1.0);

			for (int dr = -vicinityRadius; dr <= vicinityRadius; dr++) {
				for (int dc = -vicinityRadius; dc <= vicinityRadius; dc++) {
					if ((dr > minRadius) || (dc > minRadius)
							|| (dr < -minRadius) || (dc < -minRadius)) {
						if (Math.random() > percentage) {
							continue;
						}
					}
					int nr = row + dr;
					int nc = col + dc;
					if ((nr < 0) || (nr >= n) || (nc < 0) || (nc >= n)) {
						continue;
					}

					if ((col < n1) && (nc >= n1)) {
						continue;
					}
					if ((nc < n1) && (col >= n1)) {
						continue;
					}
					// double w1 = Math.exp((-Math.abs(dr) - Math.abs(dc)) /
					// 16.0);
					double w1 = 1.0 - 0.5 * (Math.abs(dr) - Math.abs(dc))
							/ vicinityRadius;
					if (w1 < 0.0) {
						continue;
					}

					// check if already setLocation
					int ni = nr * n + nc;
					if (smmBig.get(i, ni) != 0.0) {
						continue;
					}
					smmBig.set(i, ni, w1);
					smmBig.set(ni, i, w1);
				}
			}
		}
		long startTime = System.currentTimeMillis();
		System.out.println("Set : " + (startTime - time0));
		return smmBig;
	}

	private NCWeightMatrix getWWeightMap8(int minRadius, int vicinityRadius,
			double percentage) {
		long time0 = System.currentTimeMillis();
		int n = this.height * this.width;
		NCWeightMatrix smmBig = new NCWeightMatrix(n, n);
		IndexBitmapObject ibo = IndexBitmapObject
				.getAsGreyscale(this.inputImage);
		int[][] greys = ibo.getBitmap();

		for (int i = 0; i < n; i++) {
			int row = i / this.width;
			int col = i % this.width;
			int thisGrey = greys[col][row];
			smmBig.set(i, i, 0.0);

			for (int dr = -vicinityRadius; dr <= vicinityRadius; dr++) {
				for (int dc = -vicinityRadius; dc <= vicinityRadius; dc++) {
					if ((dr == 0) && (dc == 0)) {
						continue;
					}
					if ((dr > minRadius) || (dc > minRadius)
							|| (dr < -minRadius) || (dc < -minRadius)) {
						if (Math.random() > percentage) {
							continue;
						}
					}
					int nr = row + dr;
					int nc = col + dc;
					if ((nr < 0) || (nr >= this.height) || (nc < 0)
							|| (nc >= this.width)) {
						continue;
					}

					int currGrey = greys[nc][nr];
					double diffGrey = currGrey - thisGrey;
					double w = Math.exp(-((diffGrey * diffGrey) / 1000.0 + (dr
							* dr + dc * dc) / 400.0));
					if (w < 0.001) {
						continue;
					}

					// check if already setLocation
					int ni = nr * this.width + nc;
					if (smmBig.get(i, ni) != 0.0) {
						continue;
					}
					smmBig.set(i, ni, w);
					smmBig.set(ni, i, w);
				}
			}
		}
		long startTime = System.currentTimeMillis();
		System.out.println("Set : " + (startTime - time0));
		return smmBig;
	}

	private NCWeightMatrix getWWeightMap9(int minRadius, int vicinityRadius,
			double percentage) {
		long time0 = System.currentTimeMillis();
		int n = this.height * this.width;
		NCWeightMatrix smmBig = new NCWeightMatrix(n, n);
		Convolver convolver = new Convolver(IndexBitmapObject
				.getAsGreyscale(this.inputImage));

		Filter[] filters = FilterBank.getFilters(2.0, 1.0, 1, 2, 2, 2);
		PointND[][] responses = convolver.convolve(filters);
		long time1 = System.currentTimeMillis();
		System.out.println("Creating and convolving " + filters.length
				+ " filters : " + (time1 - time0));
		PointND[] responseVector = new PointND[this.width * this.height];
		for (int i = 0; i < this.width; i++) {
			for (int j = 0; j < this.height; j++) {
				responseVector[j * this.width + i] = responses[i][j];
			}
		}
		KMeans km = new KMeans(responseVector, filters.length, 12);
		PointND[] centers = km.getCenters(0.1, 50);

		for (int i = 0; i < n; i++) {
			int row = i / this.width;
			int col = i % this.width;
			int center = km.getClosestCenterIndex(i);
			// smmBig.setLocation(i, i, 1.0);

			for (int dr = -vicinityRadius; dr <= vicinityRadius; dr++) {
				for (int dc = -vicinityRadius; dc <= vicinityRadius; dc++) {
					if ((dr == 0) && (dc == 0)) {
						continue;
					}
					if ((dr > minRadius) || (dc > minRadius)
							|| (dr < -minRadius) || (dc < -minRadius)) {
						if (Math.random() > percentage) {
							continue;
						}
					}
					int nr = row + dr;
					int nc = col + dc;
					if ((nr < 0) || (nr >= this.height) || (nc < 0)
							|| (nc >= this.width)) {
						continue;
					}

					int ni = nr * this.width + nc;
					int nCenter = km.getClosestCenterIndex(ni);
					double w = (center == nCenter) ? 0.9 : 0.1;
					// double w = Math
					// .exp(-((diffGrey * diffGrey) / 1000.0 + (dr * dr + dc
					// * dc) / 400.0));
					// if (w < 0.001)
					// continue;

					// check if already setLocation
					if (smmBig.get(i, ni) != 0.0) {
						continue;
					}
					smmBig.set(i, ni, w);
					smmBig.set(ni, i, w);
				}
			}
		}
		long startTime = System.currentTimeMillis();
		System.out.println("Set : " + (startTime - time0));
		return smmBig;
	}

	public int getSecondSmallestIndex(double[] values) {
		int len = values.length;
		double[] absv = new double[len];
		for (int i = 0; i < len; i++) {
			absv[i] = Math.abs(values[i]);
		}
		Arrays.sort(absv);

		double toFind = absv[0];
		if (toFind < 1e-04) {
			toFind = absv[1];
		}
		for (int i = 0; i < len; i++) {
			if (Math.abs(toFind - Math.abs(values[i])) < 1e-07) {
				return i;
			}
		}
		return 0;
	}

	public BufferedImage getResult() {
		// TrueColorImageManager tcim = new TrueColorImageManager(this.app,
		// this.width, this.height);
		// tcim.resetImage(0xffa0a0a0);

		BufferedImage result = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) result.createGraphics();
		g.setColor(new Color(0xffa0a0a0, true));
		g.fillRect(0, 0, width, height);

		// int n = 20;
		long startTime = System.currentTimeMillis();
		// NCWeightMatrix w = this.getWWeightMap4(n);
		// this.weightMatrix = this.getWWeightMap7(n, 3, (int)Math.sqrt(n),
		// 0.5);
		// this.weightMatrix = this.getWWeightMap7(n, n, 0.5);
		this.weightMatrix = this.getWWeightMap8(2, 8, 0.03);
		// this.weightMatrix = this.getWWeightMap9(2, 5, 0.1);
		Matrix matrix = this.weightMatrix.createMatrixForEigen();
		System.out.println("Created matrix");

		this.isFirstEigen = true;

		LanczosDecomposition ld = new LanczosDecomposition(this, matrix, 50, 50);
		System.out.println("Finished Lanczos");

		System.out.println("Best NCut value " + this.bestNCut
				+ " was obtained at iteration " + this.bestIteration
				+ " for eigen value " + this.bestEigenValue);

		double min0 = this.bestEigenVector[0];
		double max0 = this.bestEigenVector[0];
		for (int i = 1; i < this.bestEigenVector.length; i++) {
			double currV = this.bestEigenVector[i];
			if (currV < min0) {
				min0 = currV;
			}
			if (currV > max0) {
				max0 = currV;
			}
		}

		System.out.println("min: " + min0 + ", max " + max0);
		for (int i = 0; i < this.bestEigenVector.length; i++) {
			int row = i / this.width;
			int col = i % this.width;
			int ival = (int) (255.0 * (this.bestEigenVector[i] - min0) / (max0 - min0));
			if (ival > 255) {
				ival = 255;
			}
			if (ival >= 128) {
				ival = 255;
			} else {
				ival = 0;
			}
			result.setRGB(col, row, (255 << 24) | (ival << 16) | (ival << 8)
					| ival);
		}
		return result;
	}

	private double getAverage(double[] vector) {
		double sum = 0.0;
		for (int i = 0; i < vector.length; i++) {
			sum += vector[i];
		}
		return sum / vector.length;
	}

	private double getAverage(Matrix matrix, int column) {
		double sum = 0.0;
		int count = matrix.getRowCount();
		for (int i = 0; i < count; i++) {
			sum += matrix.get(column, i);
		}
		return sum / count;
	}

	private double computeNCut(NCWeightMatrix weights, double[] eigenvector,
			double eigenCut) {
		int n = eigenvector.length;
		boolean[] belongsToA = new boolean[n];
		boolean[] belongsToB = new boolean[n];
		for (int i = 0; i < n; i++) {
			belongsToA[i] = (eigenvector[i] > eigenCut);
			belongsToB[i] = (eigenvector[i] <= eigenCut);
		}

		double cutAB = 0.0;
		double cutAV = 0.0;
		double cutBV = 0.0;

		for (int i = 0; i < n; i++) {
			// go over all links of this node in the weight matrix
			if (belongsToA[i]) {
				cutAB += weights.getSum(i, belongsToB);
				cutAV += weights.getSum(i);
			} else {
				cutAB += weights.getSum(i, belongsToA);
				cutBV += weights.getSum(i);
			}
		}
		// cutAB /= 2.0;
		double result = (cutAB / cutAV) + (cutAB / cutBV);
		return result;
	}

	public boolean isAcceptable(int iteration, double evalue, double[] evector) {
		if (Math.abs(evalue) < MathConstants.EPS_BIG) {
			return false;
		}

		double average = this.getAverage(evector);
		double ncut = this.computeNCut(this.weightMatrix, evector, average);
		System.out.println("ncut for eigen " + evalue + " is " + ncut);

		if (this.isFirstEigen) {
			this.bestEigenValue = evalue;
			this.bestEigenVector = evector;
			this.bestNCut = ncut;
			this.isFirstEigen = false;
			this.bestIteration = iteration;
			return false;
		}

		if (ncut > this.bestNCut) {
			return false;
		}

		this.bestEigenValue = evalue;
		this.bestEigenVector = evector;
		this.bestNCut = ncut;
		this.bestIteration = iteration;

		return false;
	}
}