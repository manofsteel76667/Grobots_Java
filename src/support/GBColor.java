/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
// GBColor.cpp
// Grobots (c) 2002-2006 Devon and Warren Schudy
// Distributed under the GNU General Public License.

package support;

import java.awt.Color;

public class GBColor {
	/**
	 * 
	 */
	// Some standard colors that aren't a named color
	public static final Color darkRed = new Color(139, 0, 0);
	public static final Color darkGreen = new Color(0, 100, 0);
	public static final Color purple = new Color(1, 0, 1);
	public static final Color gold = new Color(0.4f, 0.6f, 0);
	public static final Color shadow = new Color(.2f, .2f, .2f, .5f);

	public static final float kRedWeight = 0.35f;
	public static final float kGreenWeight = 0.45f;
	public static final float kBlueWeight = 0.2f;

	public static final float kMaxTextLightness = 0.7f;

	public static Color getGreyColor(float grey) {
		return new Color(Limit(grey), Limit(grey), Limit(grey));
	}

	/* Not used outside of this class; make private? */
	public static float Lightness(Color c) {
		return c.getRed() * kRedWeight + c.getGreen() * kGreenWeight
				+ c.getBlue() * kBlueWeight / 255;
	}

	public static Color Mix(Color base, float fraction, Color other) {
		return add(multiply(base, fraction), multiply(other, 1.0f - fraction));
	}

	public static float Contrast(Color base, Color other) {
		double r = Math.pow((base.getRed() - other.getRed()) / 255f, 2)
				* kRedWeight;
		double g = Math.pow((base.getGreen() - other.getGreen()) / 255f, 2)
				* kGreenWeight;
		double b = Math.pow((base.getBlue() - other.getBlue()) / 255f, 2)
				* kBlueWeight;
		return (float) Math.sqrt(r + g + b);
	}

	static final float Limit(float val) {
		if (val > 1.0f)
			return 1.0f;
		if (val < 0.0f)
			return 0.0f;
		return val;
	}

	// this function may not return a color with Lightness>minimum if minimum is
	// > 1/3!
	public static Color EnsureContrastWithBlack(Color base, float minimum) {
		float contrast = Contrast(base, Color.BLACK);
		if (contrast >= minimum)
			return base;
		if (contrast == 0.0f)
			return getGreyColor(minimum);
		return multiply(base, minimum / contrast);
	}

	// Returns one of two colors which best contrasts with *this. If the primary
	// color is at least cutoff contrast, secondary is not considered.
	public static Color ChooseContrasting(Color base, Color primary,
			Color secondary, float cutoff) {
		float cp, cs;
		cp = Contrast(base, primary);
		cs = Contrast(base, secondary);
		if (cp >= cutoff)
			return primary;
		else
			return cp >= cs ? primary : secondary;
	}

	public static Color ContrastingTextColor(Color base) {
		if (Lightness(base) < kMaxTextLightness)
			return base;
		else
			return multiply(base, kMaxTextLightness);
	}

	public static Color multiply(Color base, float multiplier) {
		float r = GBMath.clamp(base.getRed() / 255f * multiplier, 0, 1);
		float g = GBMath.clamp(base.getGreen() / 255f * multiplier, 0, 1);
		float b = GBMath.clamp(base.getBlue() / 255f * multiplier, 0, 1);
		return new Color(r, g, b);
	}

	public static Color add(Color base, Color other) {
		int r = GBMath.clamp(base.getRed() + other.getRed(), 0, 255);
		int g = GBMath.clamp(base.getGreen() + other.getGreen(), 0, 255);
		int b = GBMath.clamp(base.getBlue() + other.getBlue(), 0, 255);
		return new Color(r, g, b);
	}

	/**
	 * Returns the hex representation of the given color. Useful for html and
	 * css styling.
	 * 
	 * @param base
	 * @return
	 */
	public static String toHex(Color base) {
		return String.format("#%02x%02x%02x", base.getRed(), base.getGreen(),
				base.getBlue());
	}
}
