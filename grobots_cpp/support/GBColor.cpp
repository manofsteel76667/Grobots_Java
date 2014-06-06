// GBColor.cpp
// Grobots (c) 2002-2006 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBColor.h"
#include "GBNumber.h"
#include "GBPlatform.h"
#include <math.h>

// some things are inline, and can be found in GBColor.h
const float kRedWeight = 0.35f;
const float kGreenWeight = 0.45f;
const float kBlueWeight = 0.2f;

const float kMaxTextLightness = 0.7f;


GBColor::GBColor(const float grey)
	: r(Limit(grey)), g(Limit(grey)), b(Limit(grey))
{}

GBColor::GBColor(const float red, const float green, const float blue)
	: r(Limit(red)), g(Limit(green)), b(Limit(blue))
{}

void GBColor::Set(const float red, const float green, const float blue) {
	r = Limit(red);
	g = Limit(green);
	b = Limit(blue);
}

float GBColor::Lightness() const {
	return r * kRedWeight + g * kGreenWeight + b * kBlueWeight;
}

const GBColor GBColor::Mix(const float fraction, const GBColor & other) const {
	return *this * fraction + other * (1.0f - fraction);
}

const float GBColor::Contrast(const GBColor & other) const {
	return sqrt((r - other.r) * (r - other.r) * kRedWeight +
			(g - other.g) * (g - other.g) * kGreenWeight +
			(b - other.b) * (b - other.b) * kBlueWeight);
	//alternate, maybe better
	return fabs(r - other.r) * kRedWeight +
			fabs(g - other.g) * kGreenWeight +
			fabs(b - other.b) * kBlueWeight;
}


float GBColor::Limit(float val) {
	if ( val > 1.0f ) return 1.0f;
	if ( val < 0.0f ) return 0.0f;
	return val;
}

// this function may not return a color with Lightness>minimum if minimum is > 1/3!
const GBColor GBColor::EnsureContrastWithBlack(const float minimum) const {
	float contrast = Contrast(GBColor::black);
	if ( contrast >= minimum )
		return *this;
	if ( contrast == 0.0f )
		return GBColor(minimum);
	return *this * (minimum / contrast);
}

// Returns one of two colors which best contrasts with *this. If the primary color
//  is at least cutoff contrast, secondary is not considered.
const GBColor GBColor::ChooseContrasting(const GBColor & primary, const GBColor & secondary,
		const float cutoff) const {
	float cp, cs;
	cp = Contrast(primary);
	cs = Contrast(secondary);
	if ( cp >= cutoff )
		return primary;
	else
		return cp >= cs ? primary : secondary;
}

const GBColor GBColor::ContrastingTextColor() const {
	if ( Lightness() < kMaxTextLightness )
		return *this;
	else
		return *this * kMaxTextLightness;
}

const GBColor GBColor::operator *(float multiplier) const {
	return GBColor(r * multiplier, g * multiplier, b * multiplier);
}

const GBColor GBColor::operator /(float divisor) const {
	if ( ! divisor ) throw GBDivideByZeroError();
	return GBColor(r / divisor, g / divisor, b / divisor);
}

const GBColor GBColor::operator +(const GBColor & other) const {
	return GBColor(r + other.r, g + other.g, b + other.b);
}

const GBColor GBColor::red(1, 0, 0);
const GBColor GBColor::green(0, 1, 0);
const GBColor GBColor::blue(0, 0, 1);
const GBColor GBColor::cyan(0, 1, 1);
const GBColor GBColor::magenta(1, 0, 1);
const GBColor GBColor::yellow(1, 1, 0);
const GBColor GBColor::black(0);
const GBColor GBColor::white(1);
const GBColor GBColor::gray(0.5f);
const GBColor GBColor::lightGray(0.8f);
const GBColor GBColor::darkGray(0.2f);
const GBColor GBColor::purple(0.6f, 0, 0.8f);
const GBColor GBColor::darkGreen(0, 0.5f, 0);
const GBColor GBColor::darkRed(0.7f, 0, 0);

