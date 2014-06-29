//RobotType.cpp
//Grobots (c) 2002-2004 Devon and Warren Schudy
//Distributed under the GNU General Public License.

package sides;

import java.awt.Color;

import support.GBColor;
import brains.Brain;
import brains.BrainSpec;
import brains.GBBadSymbolIndexError;
import exception.GBGenericError;
import exception.GBOutOfMemoryError;

public class RobotType extends support.Model {
	public static final double kStandardMass = 20;
	public boolean debug = true;
	public Side side;
	public String name;
	public int id;
	public GBColor color;
	public GBRobotDecoration decoration = GBRobotDecoration.none;
	public GBColor decorationColor;
	public HardwareSpec hardware;
	public BrainSpec brain;
	public long population;
	public double biomass;
	// private:
	double cost;
	double mass;

	// cpp file
	public RobotType() throws GBGenericError {
		throw new GBGenericError("Bad constructor");
	}

	public RobotType(Side owner) {
		side = owner;
		debug = owner.debug;
		decoration = GBRobotDecoration.none;
		decorationColor = new GBColor(Color.black);
		color = new GBColor();
		hardware = new HardwareSpec(debug);
	}

	@Override
	public RobotType clone() {
		RobotType type = new RobotType(side);
		type.name = name;
		type.SetColor(color);
		type.SetDecoration(decoration, decorationColor);
		type.hardware = hardware;
		type.brain = brain.clone();
		return type;
	}

	public boolean equals(RobotType other) {
		if (other == null)
			return false;
		return this.side.name == other.side.name && this.name == other.name;
	}

	public void ResetSampledStatistics() {
		population = 0;
		biomass = 0;
	}

	public void ReportRobot(double botBiomass) {
		population++;
		biomass += botBiomass;
	}

	public void SetName(String newname) {
		name = newname;
		Changed();
	}

	public String Description() {
		return side.Name() + (side.Name().endsWith("s") ? "'" : "'s") + " "
				+ name;// (sidename[sidename.size() - 1] == 's' ? "' " : "'s ")
						// + name;
	}

	public int ID() {
		return id;
	}

	public void SetID(int newid) {
		id = newid;
	}

	public GBColor Color() {
		return color;
	}

	void SetColor(GBColor newcolor) {
		color = newcolor;
		Changed();
	}

	public GBRobotDecoration Decoration() {
		return decoration;
	}

	public GBColor DecorationColor() {
		return decorationColor;
	}

	void SetDecoration(GBRobotDecoration dec, GBColor col) {
		decoration = dec;
		decorationColor = col;
	}

	public HardwareSpec Hardware() {
		return hardware;
	}

	public BrainSpec Brain() {
		return brain;
	}

	public void SetBrain(BrainSpec spec) throws GBGenericError {
		spec.Check();
		brain = spec;
		Changed();
	}

	public Brain MakeBrain() throws GBBadSymbolIndexError, GBOutOfMemoryError {
		if (brain == null) {
			return null;
		}
		return brain.MakeBrain();
	}

	public void Recalculate() throws GBGenericError {
		hardware.Recalculate();
		cost = hardware.Cost() + (brain != null ? brain.Cost() : 0);
		mass = hardware.Mass() + (brain != null ? brain.Mass() : 0);
		Changed();
	}

	public double Cost() {
		return cost;
	}

	public double Mass() {
		return mass;
	}

	public double MassiveDamageMultiplier(double mass) {
		double multiplier = 1;
		if (mass > kStandardMass)
			multiplier += (mass - kStandardMass) / 50;
		return multiplier;
	}

}
