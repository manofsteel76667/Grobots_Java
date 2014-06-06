// GBRobotType.h
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBRobotType_h
#define GBRobotType_h

#include "GBHardwareSpec.h"
#include "GBColor.h"
#include "GBModel.h"
#include "GBLongNumber.h"


class GBSide;
class GBBrain;
class GBBrainSpec;

typedef enum {
	rdNone = 0,
	rdDot, rdCircle, rdSquare, rdTriangle,
	rdCross, rdX, rdHLine, rdVLine, rdSlash, rdBackslash,
	kNumRobotDecorations
} GBRobotDecoration;


class GBRobotType : public GBModel {
	GBSide * side;
	string name;
	long id;
	GBColor color;
	GBRobotDecoration decoration;
	GBColor decorationColor;
	GBHardwareSpec hardware;
	GBBrainSpec * brain;
	long population;
	GBLongNumber biomass;
public:
	GBRobotType * next;
private:
	GBEnergy cost;
	GBMass mass;
// forbidden
	GBRobotType();
public:
	GBRobotType(GBSide * owner);
	~GBRobotType();
	GBRobotType * Copy(GBSide * side) const;
// statistics
	void ResetSampledStatistics();
	void ReportRobot(GBEnergy botBiomass);
	long Population() const;
	long Biomass() const;
// accessors
	GBSide * Side() const;
	const string & Name() const;
	void SetName(const string & newname);
	string Description() const;
	long ID() const;
	void SetID(long newid);
	GBColor Color() const;
	void SetColor(const GBColor & newcolor);
	GBRobotDecoration Decoration() const;
	GBColor DecorationColor() const;
	void SetDecoration(GBRobotDecoration dec, const GBColor & col);
	GBHardwareSpec & Hardware();
	const GBHardwareSpec & Hardware() const; //the same but const
	GBBrainSpec * Brain() const;
	void SetBrain(GBBrainSpec * spec);
	GBBrain * MakeBrain() const;
// computed
	void Recalculate();
	GBEnergy Cost() const;
	GBEnergy Mass() const;
	GBNumber MassiveDamageMultiplier(const GBMass mass) const; //takes parameter because mass changes when pregnant
};

#endif
