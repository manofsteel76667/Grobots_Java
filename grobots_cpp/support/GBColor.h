// GBColor.h
// portable RGB color class
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBColor_h
#define GBColor_h


class GBColor {
	float r, g, b;
public:
	GBColor();
	explicit GBColor(const float grey);
	GBColor(const float red, const float green, const float blue);
	float Red() const { return r; }
	float Green() const { return g; }
	float Blue() const { return b; }
	void Set(const float red, const float green, const float blue);
	float Lightness() const;
// computations
	const GBColor Mix(const float fraction, const GBColor & other) const;
	const float Contrast(const GBColor & other) const;
	const GBColor EnsureContrastWithBlack(const float minimum) const;
	const GBColor ChooseContrasting(const GBColor & primary, const GBColor & secondary, const float cutoff) const;
	const GBColor ContrastingTextColor() const;
	const GBColor operator *(float multiplier) const;
	const GBColor operator /(float divisor) const;
	const GBColor operator +(const GBColor & other) const;
// statics
	static float Limit(float val);
// handy constants
	static const GBColor red;
	static const GBColor green;
	static const GBColor blue;
	static const GBColor cyan;
	static const GBColor magenta;
	static const GBColor yellow;
	static const GBColor black;
	static const GBColor white;
	static const GBColor gray;
	static const GBColor darkGray;
	static const GBColor lightGray;
	static const GBColor purple;	
	static const GBColor darkGreen;
	static const GBColor darkRed;
};

// implementation //

inline GBColor::GBColor()
	: r(1.0), g(1.0), b(1.0)
{}

#endif
