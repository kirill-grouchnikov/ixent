package org.jvnet.ixent.math.filters;

import java.awt.image.BufferedImage;

import org.jvnet.ixent.algorithms.graphics.engine.npr.watercolor.Pigment;
import org.jvnet.ixent.graphics.IndexBitmapObject;

public class GaussConvolution {

	private double[][] kernel;
	private int[][] kernel_shift;
	private double[] kernel_1d;
	private int size;

	public GaussConvolution(double sigma, int size) {
		this.size = size;
		// compute the kernel
		kernel = new double[2 * size + 1][2 * size + 1];
		kernel_shift = new int[2 * size + 1][2 * size + 1];
		int width = 2 * size + 1;
		kernel_1d = new double[width * width];
		double sigsig = sigma * sigma;
		for (int x = -size; x <= size; x++) {
			for (int y = -size; y <= size; y++) {
				// approximate integral over a pixel*pixel area
				int count = 0;
				double startX = x - 0.5;
				double endX = x + 0.51;
				double startY = y - 0.5;
				double endY = y + 0.51;
				double val = 0.0;
				for (double subX = startX; subX < endX; subX += 0.1) {
					for (double subY = startY; subY < endY; subY += 0.1) {
						val += Math.pow(Math.E, -(subX * subX + subY * subY)
								/ (2.0 * sigsig));
						count++;
					}
				}
				val /= (2 * Math.PI * sigsig);
				val /= count;
				kernel[x + size][y + size] = val;
				kernel_shift[x + size][y + size] = (int) (Math.log(1.0 / val) / Math
						.log(2.0));
				kernel_1d[(y + size) * width + x + size] = val;
			}
		}
	}

	public int pixelConvolution(int[][] bitmap, int width, int height, int x,
			int y) {
		int startX = x - this.size;
		if (startX < 0) {
			startX = 0;
		}
		int endX = x + this.size;
		if (endX >= width) {
			endX = width - 1;
		}
		int startY = Math.max(0, y - this.size);
		if (startY < 0) {
			startY = 0;
		}
		int endY = y + this.size;
		if (endY >= height) {
			endY = height - 1;
		}
		short val = 0;
		int kernelIndex = 0;
		for (int col = startX; col <= endX; col++) {
			for (int row = startY; row <= endY; row++) {
				int shift = kernel_shift[size + col - x][size + row - y];
				if ((shift >= 0) && (shift < 8)) {
					val += (bitmap[col][row] >>> shift);
				}
				// vald += (bitmap[col][row]*kernel_1d[kernelIndex]);
				kernelIndex++;
			}
		}
		return val;
	}

	public int[][] getSmoothedBitmap(int[][] bitmap, int width, int height) {
		int[][] result = new int[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				result[x][y] = pixelConvolution(bitmap, width, height, x, y);
			}
		}
		return result;
	}

	public IndexBitmapObject getSmoothedBitmap(IndexBitmapObject inputImage) {
		return new IndexBitmapObject(this.getSmoothedBitmap(inputImage
				.getBitmap(), inputImage.getWidth(), inputImage.getHeight()),
				inputImage.getWidth(), inputImage.getHeight());
	}

	public IndexBitmapObject getChannel(BufferedImage bImage,
			Pigment.Component component) {
		int width = bImage.getWidth();
		int height = bImage.getHeight();
		int[][] channelArray = new int[width][height];
		for (int col = 0; col < width; col++) {
			for (int row = 0; row < height; row++) {
				int currColor = bImage.getRGB(col, row);
				switch (component) {
				case red:
					channelArray[col][row] = (currColor & 0x00FF0000) >> 16;
					break;
				case green:
					channelArray[col][row] = (currColor & 0x0000FF00) >> 8;
					break;
				case blue:
					channelArray[col][row] = currColor & 0x000000FF;
					break;
				}
			}
		}
		return new IndexBitmapObject(channelArray, width, height);
	}

	public BufferedImage getBufferedImage(IndexBitmapObject redChannel,
			IndexBitmapObject greenChannel, IndexBitmapObject blueChannel) {
		if ((redChannel == null) || (greenChannel == null)
				|| (blueChannel == null)) {
			throw new IllegalArgumentException("Can't pass null parameter");
		}

		int width = redChannel.getWidth();
		int height = redChannel.getHeight();
		if ((width != greenChannel.getWidth())
				|| (width != blueChannel.getWidth())
				|| (height != greenChannel.getHeight())
				|| (height != blueChannel.getHeight())) {
			throw new IllegalArgumentException(
					"Input channels are of different sizes");
		}
		BufferedImage result = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int red = Math.min(255, redChannel.getValue(x, y));
				int green = Math.min(255, greenChannel.getValue(x, y));
				int blue = Math.min(255, blueChannel.getValue(x, y));
				result.setRGB(x, y, (255 << 24) | (red << 16) | (green << 8)
						| blue);
			}
		}
		return result;
	}

	public BufferedImage getSmoothedImage(BufferedImage inputImage) {
		return this.getBufferedImage(this.getSmoothedBitmap(getChannel(
				inputImage, Pigment.Component.red)), this
				.getSmoothedBitmap(this.getChannel(inputImage,
						Pigment.Component.green)), this.getSmoothedBitmap(this
				.getChannel(inputImage, Pigment.Component.blue)));
	}
}
