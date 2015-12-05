/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
//RobotType.cpp
//Grobots (c) 2002-2004 Devon and Warren Schudy
//Distributed under the GNU General Public License.

package sides;

import java.awt.Color;

import simulation.GBRobot;
import brains.Brain;
import brains.BrainSpec;

public class RobotType {
	static int iconHeight = 64;
	public static final double kStandardMass = 20;
	public boolean debug = true;
	public Side side;
	public String name;
	public int id;
	public Color color;
	public GBRobotDecoration decoration = GBRobotDecoration.none;
	public Color decorationColor;
	public HardwareSpec hardware;
	public BrainSpec brain;
	public int population;
	public double biomass;
	public GBRobot sample;
	// private:
	double cost;
	double mass;

	public RobotType(Side owner) {
		side = owner;
		debug = owner.debug;
		decoration = GBRobotDecoration.none;
		decorationColor = Color.black;
		color = Color.black;
		hardware = new HardwareSpec(debug);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((side == null) ? 0 : side.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof RobotType))
			return false;
		RobotType other = (RobotType) obj;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (side == null) {
			if (other.side != null)
				return false;
		} else if (!side.equals(other.side))
			return false;
		return true;
	}

	// @Override
	public boolean _equals(Object other) {
		if (this == other)
			return true;
		if (other == null || !(other instanceof RobotType))
			return false;
		return this.side.name.equals(((RobotType) other).side.name)
				&& this.name.equals(((RobotType) other).name);
	}

	public void resetSampledStatistics() {
		population = 0;
		biomass = 0;
	}

	public void reportRobot(double botBiomass) {
		population++;
		biomass += botBiomass;
	}

	public void setName(String newname) {
		name = newname;
	}

	@Override
	public String toString() {
		return side.getName() + (side.getName().endsWith("s") ? "'" : "'s") + " "
				+ name;
	}

	public int getID() {
		return id;
	}

	public void setID(int newid) {
		id = newid;
	}

	public Color getColor() {
		return color;
	}

	void setColor(Color newcolor) {
		color = newcolor;
	}

	public GBRobotDecoration getDecoration() {
		return decoration;
	}

	public Color getDecorationColor() {
		return decorationColor;
	}

	void setDecoration(GBRobotDecoration dec, Color col) {
		decoration = dec;
		decorationColor = col;
	}

	public HardwareSpec getHardware() {
		return hardware;
	}

	public BrainSpec Brain() {
		return brain;
	}

	public void setBrain(BrainSpec spec) {
		spec.check();
		brain = spec;
	}

	public Brain makeBrain() {
		if (brain == null) {
			return null;
		}
		return brain.makeBrain();
	}

	public void recalculate() {
		hardware.Recalculate();
		cost = hardware.Cost() + (brain != null ? brain.getCost() : 0);
		mass = hardware.Mass() + (brain != null ? brain.getMass() : 0);
	}

	public double getCost() {
		return cost;
	}

	public double getMass() {
		return mass;
	}

	public double getMassiveDamageMultiplier(double mass) {
		double multiplier = 1;
		if (mass > kStandardMass)
			multiplier += (mass - kStandardMass) / 50;
		return multiplier;
	}
}
