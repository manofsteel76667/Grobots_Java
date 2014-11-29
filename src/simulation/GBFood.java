/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import exception.GBSimulationError;
import sides.Side;
import support.FinePoint;
import support.GBObjectClass;

// GBFood.cpp
// Grobots (c) 2002-2007 Devon and Warren Schudy
// Distributed under the GNU General Public License.

public class GBFood extends GBObject {
	public static final double kFoodMinRadius = 0.1;
	public static final double kFoodRadiusFactor = 0.01;
	public static final double kFoodMassPerValue = 0.015;
	public static final double kFoodDecayRate = 0.12;

	public static final double kFriction = 0.004;
	public static final double kLinearDragFactor = 0.01;
	public static final double kQuadraticDragFactor = 0.3;
	// protected:
	protected double value;

	// public:
	protected void Recalculate() {
		radius = Math.sqrt(value) * kFoodRadiusFactor + kFoodMinRadius;
		mass = value * kFoodMassPerValue;
	}

	public GBFood(FinePoint where, double val) {
		super(where, Math.sqrt(val) * kFoodRadiusFactor + kFoodMinRadius, val
				* kFoodMassPerValue);
		value = Math.max(val, 0);
	}

	public GBFood(FinePoint where, FinePoint vel, double val) {
		super(where, Math.sqrt(val) * kFoodRadiusFactor + kFoodMinRadius, vel,
				val * kFoodMassPerValue);
		value = val;

		if (val < 0)
			throw new GBSimulationError("negative-value food");
	}

	@Override
	public double Energy() {
		return value;
	}

	@Override
	public double TakeEnergy(double limit) {
		if (limit < 0)
			throw new GBSimulationError("can't take negative energy from food");
		if (value <= limit) {
			double amt = value;
			value = 0;
			Recalculate();
			return amt;
		} else {
			value -= limit;
			if (value < 0)
				throw new GBSimulationError("taking negative energy from food");
			Recalculate();
			return limit;
		}
	}

	@Override
	public double MaxTakeEnergy() {
		return value;
	}

	@Override
	public GBObjectClass Class() {
		if (value > 0)
			return GBObjectClass.ocFood;
		else
			return GBObjectClass.ocDead;
	}

	@Override
	public Side Owner() {
		return null;
	}

	@Override
	public void Move() {
		super.Move();
		Drag(kFriction, kLinearDragFactor, kQuadraticDragFactor);
	}

	@Override
	public void Act(GBWorld world) {
		value = Math.max(value - kFoodDecayRate, 0);
		Recalculate(); // FIXME this is slow
	}

	@Override
	public Color Color() {
		return Color.white;
	}

	@Override
	public void Draw(Graphics g, GBProjection proj, boolean detailed) {
		Graphics2D g2d = (Graphics2D) g;
		Rectangle where = getScreenRect(proj);
		g2d.setColor(Color());
		g2d.fillRect(where.x, where.y, where.width, where.height);
	}
};
