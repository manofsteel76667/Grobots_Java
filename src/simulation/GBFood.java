package simulation;

import sides.Side;
import support.FinePoint;
import support.GBObjectClass;
import exception.GBBadArgumentError;
import exception.GBBadComputedValueError;

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

	public GBFood(FinePoint where, double val) throws GBBadArgumentError {
		super(where, Math.sqrt(val) * kFoodRadiusFactor + kFoodMinRadius, val
				* kFoodMassPerValue);
		value = val;

		if (val < 0)
			throw new GBBadArgumentError();
	}

	public GBFood(FinePoint where, FinePoint vel, double val)
			throws GBBadArgumentError {
		super(where, Math.sqrt(val) * kFoodRadiusFactor + kFoodMinRadius, vel,
				val * kFoodMassPerValue);
		value = val;

		if (val < 0)
			throw new GBBadArgumentError();
	}

	@Override
	public double Energy() {
		return value;
	}
	@Override
	public double TakeEnergy(double limit) throws GBBadArgumentError,
			GBBadComputedValueError {
		if (limit < 0)
			throw new GBBadArgumentError();
		if (value <= limit) {
			double amt = value;
			value = 0;
			Recalculate();
			return amt;
		} else {
			value -= limit;
			if (value < 0)
				throw new GBBadComputedValueError();
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
	public String toString() {
		// TODO Auto-generated method stub
		return "";
	}

	// TODO: After GUI
	/*
	 * public GBColor Color() { return GBColor::white; }
	 * 
	 * void Draw(GBGraphics & g, GBProjection &, GBRect & where, boolean
	 * /*detailed
	 *//*
		 * ) { g.DrawSolidRect(where, Color()); }
		 */

};

// GBFood //

// GBManna //

// GBCorpse //

