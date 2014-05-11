// GBColor.cpp
// Grobots (c) 2002-2006 Devon and Warren Schudy
// Distributed under the GNU General Public License.

package support;

class GBColor extends java.awt.Color {
	float r, g, b;
//public:
	
// implementation //

public GBColor(){
	r = 1.0f; g=1.0f; b=1.0f;
}

// some things are inline, and can be found in GBColor.h
public static final float kRedWeight = 0.35f;
public static final float kGreenWeight = 0.45f;
public static final float kBlueWeight = 0.2f;

public static final float kMaxTextLightness = 0.7f;


GBColor(const float grey)
	: r(Limit(grey)), g(Limit(grey)), b(Limit(grey))
{}

GBColor(const float red, const float green, const float blue)
	: r(Limit(red)), g(Limit(green)), b(Limit(blue))
{}

void Set(const float red, const float green, const float blue) {
	r = Limit(red);
	g = Limit(green);
	b = Limit(blue);
}

float Lightness() const {
	return r * kRedWeight + g * kGreenWeight + b * kBlueWeight;
}

const GBColor Mix(const float fraction, const GBColor & other) const {
	return *this * fraction + other * (1.0f - fraction);
}

const float Contrast(const GBColor & other) const {
	return sqrt((r - other.r) * (r - other.r) * kRedWeight +
			(g - other.g) * (g - other.g) * kGreenWeight +
			(b - other.b) * (b - other.b) * kBlueWeight);
	//alternate, maybe better
	return fabs(r - other.r) * kRedWeight +
			fabs(g - other.g) * kGreenWeight +
			fabs(b - other.b) * kBlueWeight;
}


float Limit(float val) {
	if ( val > 1.0f ) return 1.0f;
	if ( val < 0.0f ) return 0.0f;
	return val;
}

// this function may not return a color with Lightness>minimum if minimum is > 1/3!
const GBColor EnsureContrastWithBlack(const float minimum) const {
	float contrast = Contrast(black);
	if ( contrast >= minimum )
		return *this;
	if ( contrast == 0.0f )
		return GBColor(minimum);
	return *this * (minimum / contrast);
}

// Returns one of two colors which best contrasts with *this. If the primary color
//  is at least cutoff contrast, secondary is not considered.
const GBColor ChooseContrasting(const GBColor & primary, const GBColor & secondary,
		const float cutoff) const {
	float cp, cs;
	cp = Contrast(primary);
	cs = Contrast(secondary);
	if ( cp >= cutoff )
		return primary;
	else
		return cp >= cs ? primary : secondary;
}

const GBColor ContrastingTextColor() const {
	if ( Lightness() < kMaxTextLightness )
		return *this;
	else
		return *this * kMaxTextLightness;
}

const GBColor operator *(float multiplier) const {
	return GBColor(r * multiplier, g * multiplier, b * multiplier);
}

const GBColor operator /(float divisor) const {
	if ( ! divisor ) throw GBDivideByZeroError();
	return GBColor(r / divisor, g / divisor, b / divisor);
}

const GBColor operator +(const GBColor & other) const {
	return GBColor(r + other.r, g + other.g, b + other.b);
}

const GBColor red(1, 0, 0);
const GBColor green(0, 1, 0);
const GBColor blue(0, 0, 1);
const GBColor cyan(0, 1, 1);
const GBColor magenta(1, 0, 1);
const GBColor yellow(1, 1, 0);
const GBColor black(0);
const GBColor white(1);
const GBColor gray(0.5f);
const GBColor lightGray(0.8f);
const GBColor darkGray(0.2f);
const GBColor purple(0.6f, 0, 0.8f);
const GBColor darkGreen(0, 0.5f, 0);
const GBColor darkRed(0.7f, 0, 0);

