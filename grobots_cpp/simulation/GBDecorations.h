// GBDecorations.h
// various decorative GBObject subclasses
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBDecorations_h
#define GBDecorations_h

#include "GBObject.h"

class GBTimedDecoration : public GBObject {
protected:
	GBFrames lifetime;
public:
	GBTimedDecoration(const GBPosition where, const GBDistance r, const GBFrames howLong);
	GBTimedDecoration(const GBPosition where, const GBDistance r, const GBVelocity vel,
		const GBFrames howLong);
	GBObjectClass Class() const;
	void Act(GBWorld * world);
	string Description() const;
};

// convenience constants
const GBSpeed kSmokeMaxSpeed = 0.03;
const GBFrames kSmokeMinLifetime = 30;
const GBFrames kSmokeMaxLifetime = 120;

class GBSmoke : public GBTimedDecoration {
public:
	GBSmoke(const GBPosition where, const GBVelocity vel, const GBFrames life);
	string Description() const;
// drawing code
	const GBColor Color() const;
	void Draw(GBGraphics & g, const GBRect & where, bool detailed) const;
};


class GBBlasterSpark : public GBTimedDecoration {
public:
	GBBlasterSpark(const GBPosition where);
	void Act(GBWorld * world);
// drawing code
	const GBColor Color() const;
	void Draw(GBGraphics & g, const GBRect & where, bool detailed) const;
};

class GBTransmission : public GBTimedDecoration {
	bool message;
public:
	GBTransmission(const GBPosition where, GBDistance initialradius, bool msg);
	void Act(GBWorld * world);
	string Description() const;
// drawing code
	const GBColor Color() const;
	void Draw(GBGraphics & g, const GBRect & where, bool detailed) const;
	void DrawMini(GBGraphics & g, const GBRect & where) const;
};

class GBSparkle : public GBTimedDecoration {
	GBColor color;
public:
	GBSparkle(const GBPosition where, const GBVelocity vel,
		const GBColor & color, const GBFrames life);
// drawing code
	const GBColor Color() const;
	void Draw(GBGraphics & g, const GBRect & where, bool detailed) const;
	void DrawMini(GBGraphics & g, const GBRect & where) const;
};


#endif
