// GBColor.cpp
// Grobots (c) 2002-2006 Devon and Warren Schudy
// Distributed under the GNU General Public License.

package support;

import java.awt.Color;

import exception.*;

public class GBColor extends java.awt.Color {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5956330979660343356L;
	float r, g, b;

	// //public:

	// implementation //

	public GBColor() {
		super(1.0f, 1.0f, 1.0f);
		r = 1.0f;
		g = 1.0f;
		b = 1.0f;
	}

	public static final float kRedWeight = 0.35f;
	public static final float kGreenWeight = 0.45f;
	public static final float kBlueWeight = 0.2f;

	public static final float kMaxTextLightness = 0.7f;

	public GBColor(float grey) {
		super(Limit(grey), Limit(grey), Limit(grey));
		r = Limit(grey);
		g = Limit(grey);
		b = Limit(grey);
	}

	public GBColor(float red, float green, float blue) {
		super(Limit(red), Limit(green), Limit(blue));
		r = Limit(red);
		g = Limit(green);
		b = Limit(blue);
	}

	public GBColor(Color root) {
		super(root.getRed(), root.getGreen(), root.getBlue());
		r = root.getRed() / 256f;
		g = root.getGreen() / 256f;
		b = root.getBlue() / 256f;
	}

	/**
	 * TODO: This won't work in java; need to change all calls to this to create
	 * a new GBColor object instead.
	 * 
	 * @param red
	 * @param green
	 * @param blue
	 */
	public void Set(float red, float green, float blue) {
		r = Limit(red);
		g = Limit(green);
		b = Limit(blue);
	}

	public float Lightness() {
		return r * kRedWeight + g * kGreenWeight + b * kBlueWeight;
	}

	public GBColor Mix(float fraction, GBColor other) {
		return this.multiply(fraction).add(other).multiply(1.0f - fraction);
	}

	public float Contrast(GBColor other) {
		return (float) Math.sqrt((r - other.r) * (r - other.r) * kRedWeight
				+ (g - other.g) * (g - other.g) * kGreenWeight + (b - other.b)
				* (b - other.b) * kBlueWeight);
		// alternate, maybe better
		/*
		 * return (float)(Math.abs(r - other.r) * kRedWeight + Math.abs(g -
		 * other.g) * kGreenWeight + Math.abs(b - other.b) * kBlueWeight);
		 */
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
	public GBColor EnsureContrastWithBlack(float minimum) {
		float contrast = Contrast(new GBColor(Color.BLACK));
		if (contrast >= minimum)
			return this;
		if (contrast == 0.0f)
			return new GBColor(minimum);
		return this.multiply(minimum / contrast);
	}

	// Returns one of two colors which best contrasts with *this. If the primary
	// color
	// is at least cutoff contrast, secondary is not considered.
	public GBColor ChooseContrasting(GBColor primary, GBColor secondary,
			float cutoff) {
		float cp, cs;
		cp = Contrast(primary);
		cs = Contrast(secondary);
		if (cp >= cutoff)
			return primary;
		else
			return cp >= cs ? primary : secondary;
	}

	public GBColor ContrastingTextColor() {
		if (Lightness() < kMaxTextLightness)
			return this;
		else
			return this.multiply(kMaxTextLightness);
	}

	public GBColor multiply(float multiplier) {
		return new GBColor(r * multiplier, g * multiplier, b * multiplier);
	}

	public GBColor divide(float divisor) throws GBDivideByZeroError {
		if (divisor == 0)
			throw new GBDivideByZeroError();
		return new GBColor(r / divisor, g / divisor, b / divisor);
	}

	public GBColor add(GBColor other) {
		return new GBColor(r + other.r, g + other.g, b + other.b);
	}

}
