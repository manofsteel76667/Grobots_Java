/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package support;

public class GBMath {

	public static double clamp(double value, double min, double max) {
		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}

	public static int clamp(int value, int min, int max) {
		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}

	public static float clamp(float value, float min, float max) {
		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}

	public static double reorient(double value) {
		double ret = value;
		while (ret > Math.PI)
			ret -= Math.PI * 2;
		while (ret < Math.PI * -1)
			ret += Math.PI * 2;
		return ret;
	}

	// Included these constants to avoid behavior changes
	public static final double kEpsilon = 1.0f / 4096;
	public static final double kInfinity = (float) 0x7FFFFFFF / 4096;
	public static final double kPi = 3.14159265358979;
	public static final double kE = 2.71828182846;
	public static final double sqrt2 = 1.41421356;
	public static final double sqrt3 = 1.73205081;
}
