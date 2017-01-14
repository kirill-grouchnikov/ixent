package org.jvnet.ixent.graphics;

import java.awt.image.BufferedImage;

/**
 * Indexed bitmap object. Each entry in this object is non-negative and less
 * than given maximal value (if specified). This object may represent greyscale
 * version of true-color image or be used with <b>ColorManager</b> to create
 * true-color image
 * 
 * @author Kirill Grouchnikov
 */
public class IndexBitmapObject {
	private int[][] bitmap;
	private int width, height;
	private boolean hasMaximumValue;
	private int maximumValue;

	/**
	 * This constructor is private in order to prevent creating empty object
	 */
	private IndexBitmapObject() {
	}

	/**
	 * Constructor that gets only dimensions and initializes the bitmap to
	 * zeroes
	 * 
	 * @param width
	 *            bitmap width
	 * @param height
	 *            bitmap height
	 */
	public IndexBitmapObject(int width, int height) {
		this.width = width;
		this.height = height;
		this.bitmap = new int[this.width][this.height];
		for (int col = 0; col < this.width; col++) {
			for (int row = 0; row < this.height; row++) {
				this.bitmap[col][row] = 0;
			}
		}
		this.hasMaximumValue = true;
		this.maximumValue = 255;
	}

	/**
	 * Constructor that gets only dimensions and initializes the bitmap to
	 * zeroes
	 * 
	 * @param pWidth
	 *            bitmap pWidth
	 * @param pHeight
	 *            bitmap pHeight
	 */
	public IndexBitmapObject(int pWidth, int pHeight, boolean pHasMaximumValue,
			int pMaximumValue) {
		this.width = pWidth;
		this.height = pHeight;
		this.bitmap = new int[this.width][this.height];
		for (int col = 0; col < this.width; col++) {
			for (int row = 0; row < this.height; row++) {
				this.bitmap[col][row] = 0;
			}
		}
		this.hasMaximumValue = pHasMaximumValue;
		this.maximumValue = pMaximumValue;
	}

	/**
	 * Constructor that gets bitmap object
	 * 
	 * @param bitmap
	 *            bitmap object
	 * @param width
	 *            bitmap width
	 * @param height
	 *            bitmap height
	 */
	public IndexBitmapObject(int[][] bitmap, int width, int height) {
		this.width = width;
		this.height = height;
		this.bitmap = bitmap;
	}

	/**
	 * Copy constructor
	 * 
	 * @param toClone
	 *            object to copy
	 */
	public IndexBitmapObject(IndexBitmapObject toClone) {
		if (toClone == null) {
			throw new NullPointerException("Null object to clone");
		}

		this.width = toClone.width;
		this.height = toClone.height;
		int[][] newBitmap = new int[this.width][this.height];
		for (int col = 0; col < toClone.width; col++) {
			for (int row = 0; row < toClone.height; row++) {
				newBitmap[col][row] = toClone.bitmap[col][row];
			}
		}
		this.bitmap = newBitmap;
	}

	public static IndexBitmapObject getAsGreyscale(BufferedImage bImage) {
		int width = bImage.getWidth();
		int height = bImage.getHeight();
		int[][] gBitmap = new int[width][height];
		for (int col = 0; col < width; col++) {
			for (int row = 0; row < height; row++) {
				int rgb = bImage.getRGB(col, row);
				int r = (rgb & 0x00FF0000) >> 16;
				int g = (rgb & 0x0000FF00) >> 8;
				int b = (rgb & 0x000000FF);

				int luminance = (int) ((222.0 * r + 707.0 * g + 71.0 * b) / 1000.0);
				gBitmap[col][row] = luminance;
			}
		}
		return new IndexBitmapObject(gBitmap, width, height);
	}

	/**
	 * @return bitmap width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return bitmap height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @return bitmap array
	 */
	public int[][] getBitmap() {
		return bitmap;
	}

	/**
	 * Get value at given pixel
	 * 
	 * @param column
	 *            pixel column
	 * @param row
	 *            pixel row
	 * @return value at pixel
	 */
	public int getValue(int column, int row) {
		return this.bitmap[column][row];
	}

	/**
	 * Reset all entries to specified value
	 * 
	 * @param initValue
	 */
	public void reset(int initValue) {
		for (int col = 0; col < this.width; col++) {
			for (int row = 0; row < this.height; row++) {
				this.bitmap[col][row] = initValue;
			}
		}
	}

	/**
	 * Sets new value at given pixel. The value is normalized to 0..maximumValue
	 * interval if necessary
	 * 
	 * @param column
	 *            pixel column
	 * @param row
	 *            pixel row
	 * @param newValue
	 *            pixel new value
	 */
	public void setValue(int column, int row, int newValue) {
		int goodValue = newValue;
		if (goodValue < 0) {
			goodValue = 0;
		}
		if (this.hasMaximumValue) {
			if (goodValue > this.maximumValue) {
				goodValue = this.maximumValue;
			}
		}

		this.bitmap[column][row] = goodValue;
	}

	public int getMaxValueInNeighbourhood(int column, int row, int radius) {
		int xs = Math.max(0, column - radius);
		int xe = Math.min(this.width - 1, column + radius);
		int ys = Math.max(0, row - radius);
		int ye = Math.min(this.height - 1, row + radius);
		int val = 0;
		for (int x = xs; x <= xe; x++) {
			for (int y = ys; y <= ye; y++) {
				val = Math.max(val, this.bitmap[x][y]);
			}
		}
		return val;
	}
}
