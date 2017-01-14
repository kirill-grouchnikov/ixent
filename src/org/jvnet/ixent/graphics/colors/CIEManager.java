package org.jvnet.ixent.graphics.colors;

import java.awt.image.BufferedImage;

public final class CIEManager {
	public static BufferedImage getXYZDiagram(int width, int height) {

		long time0 = System.currentTimeMillis();

		double[][] coef = new double[][] { { 0.607, 0.174, 0.200 },
				{ 0.299, 0.587, 0.114 }, { 0.000, 0.066, 1.116 } };

		BufferedImage result = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		for (int col = 0; col < width; col++) {
			for (int row = 1; row < height; row++) {
				double x = (double) col / (double) width;
				double y = (double) row / (double) height;
				double Y = 100.0;
				double X = x * Y / y;
				double Z = (1 - x - y) * Y / y;

				// double R = 3.0633*X-1.3933*Y-0.4578*Z;
				// double G = -0.9692*X+1.8760*Y-0.0416*Z;
				// double B = 0.0679*X-0.2288*Y+1.0693*Z;

				double R = 1.9104 * X - 0.5344 * Y - 0.2878 * Z;
				double G = -0.9841 * X + 2.0003 * Y - 0.0278 * Z;
				double B = 0.0582 * X - 0.1183 * Y + 0.8977 * Z;

				// double[] comp = EquationSystemManager.solve(coef, new
				// double[] {X, Y, Z});

				int red = (int) (256.0 * R / 100.0);
				int green = (int) (256.0 * G / 100.0);
				int blue = (int) (256.0 * B / 100.0);

				System.out.println("(col, row): (" + col + ", " + row
						+ "), (R, G, B) : (" + red + ", " + green + ", " + blue
						+ ")");

				if ((red < 0) || (red > 255) || (green < 0) || (green > 255)
						|| (blue < 0) || (blue > 255)) {
					result.setRGB(col, row, 0xFF000000);
				} else {
					result.setRGB(col, row, (255 << 24) | (red << 16)
							| (green << 8) | blue);
				}
			}
		}

		for (int col = 0; col < width; col++) {
			result.setRGB(col, 0, 0xFFFFFFFF);
			result.setRGB(col, height - 1, 0xFFFFFFFF);
		}
		for (int row = 0; row < height; row++) {
			result.setRGB(0, row, 0xFFFFFFFF);
			result.setRGB(width - 1, row, 0xFFFFFFFF);
		}

		long time1 = System.currentTimeMillis();
		System.out.println("CIE XYZ: " + (time1 - time0));

		return result;
	}
}
