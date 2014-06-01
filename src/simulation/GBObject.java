package simulation;

// GBObject.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

import sides.Side;
//Maps FinePoints to screen locations
import support.FinePoint;
import support.GBColor;
import support.GBObjectClass;
import brains.GBBadSymbolIndexError;
import exception.GBAbort;
import exception.GBBadArgumentError;
import exception.GBBadComputedValueError;
import exception.GBGenericError;
import exception.GBNilPointerError;
import exception.GBOutOfMemoryError;

interface GBProjection {
	public short ToScreenX(double x);

	public short ToScreenY(double y);

	public double FromScreenX(short h);

	public double FromScreenY(short v);

	public FinePoint FromScreen(short x, short y);
};

public abstract class GBObject /*
								 * 
								 * GBDeletionReporter
								 */{
	FinePoint position;
	FinePoint velocity;

	public static final double kCollisionRepulsion = 0.02;
	public static final double kCollisionRepulsionSpeed = 0.1;
	public static final double kMaxOverlap = 0.04;
	public static final short kMaxSquareMiniSize = 4;

	// Some abstract functions aren't implemented by all descendants
	// Java hates this so they were given default behavior here.
	public void TakeDamage(double amount, Side origin) {
	}

	public double TakeEnergy(double amount) throws GBBadArgumentError,
			GBBadComputedValueError {
		return 0;
	}

	public double GiveEnergy(double amount) throws GBBadArgumentError {
		return 0;
	}

	public double MaxTakeEnergy() {
		return 0;
	}

	public double MaxGiveEnergy() {
		return 0;
	}

	// high-level actions
	public void Think(GBWorld world) throws GBBadArgumentError,
			GBOutOfMemoryError, GBGenericError, GBAbort {
	} // Not everything has a brain

	public abstract void Act(GBWorld world) throws GBNilPointerError,
			GBBadArgumentError, GBGenericError, GBBadSymbolIndexError,
			GBOutOfMemoryError;

	public void CollideWithWall() {
	}

	public void CollideWith(GBObject other) throws GBGenericError, GBAbort,
			GBBadArgumentError, GBBadComputedValueError {
	}

	public void CollectStatistics(GBWorld world) {
	}

	// high-level queries
	public abstract GBObjectClass Class();

	public Side Owner() {
		return null;
	} // food is not owned by anyone

	public double Energy() {
		return 0;
	}

	public double Interest() {
		return 0;
	} // how attractive to autocamera

	@Override
	public abstract String toString(); //Was Description()

	public String Details() {
		return "";
	}

	// evil antimodular drawing code
	// TODO: Put these back in when we do the GUI
	public abstract GBColor Color() ;
	// public abstract void Draw(GBGraphics g, GBProjection proj, GBRect where, boolean detailed) ;
	// public abstract void DrawUnderlay(GBGraphics g, GBProjection proj, GBRect where, boolean detailed) ;
	// public abstract void DrawOverlay(GBGraphics g, GBProjection proj, GBRect where, boolean detailed) ;

	// protected:
	protected double radius;
	protected double mass;
	public GBObject next;// references the next object; allows GBObject to act
							// as a singly-linked list

	public GBObject(FinePoint where, double r) {
		position = where;
		radius = r;
		velocity = new FinePoint();
		mass = 0;
		next = null;
	}

	public GBObject(FinePoint where, double r, FinePoint vel) {
		position = where;
		radius = r;
		velocity = vel;
		mass = 0;
		next = null;
	}

	public GBObject(FinePoint where, double r, double m) {
		position = where;
		radius = r;
		velocity = new FinePoint();
		mass = m;
		next = null;
	}

	public GBObject(FinePoint where, double r, FinePoint vel, double m) {
		position = where;
		radius = r;
		velocity = vel;
		mass = m;
		next = null;
	}

	public FinePoint Position() {
		return position;
	}

	public void SetPosition(FinePoint newPosition) {
		position = newPosition;
	}

	public void MoveBy(FinePoint delta) {
		position.add(delta);
	}

	public void MoveBy(double deltax, double deltay) {
		position.x += deltax;
		position.y += deltay;
	}

	public double Left() {
		return position.x - radius;
	}

	public double Top() {
		return position.y + radius;
	}

	public double Right() {
		return position.x + radius;
	}

	public double Bottom() {
		return position.y - radius;
	}

	public FinePoint Velocity() {
		return velocity;
	}

	public double Speed() {
		return velocity.norm();
	}

	public void SetVelocity(double sx, double sy) {
		velocity.set(sx, sy);
	}

	public void SetSpeed(double speed) {
		velocity.setNorm(speed);
	}

	public void Accelerate(FinePoint deltav) {
		velocity = velocity.add(deltav);
	}

	public void Drag(double friction, double linearCoeff, double quadraticCoeff) {
		if (velocity.isZero())
			return;
		double speed = Speed();
		speed -= speed * (speed * quadraticCoeff + linearCoeff);
		if (speed > friction)
			SetSpeed(speed - friction);
		else
			SetVelocity(0, 0);
	}

	public double Radius() {
		return radius;
	}

	public double Mass() {
		return mass;
	}

	public boolean Intersects(GBObject other) {
		double r = radius + other.radius;
		return other.position.x - position.x < r
				&& position.x - other.position.x < r
				&& other.position.y - position.y < r
				&& position.y - other.position.y < r
				&& position.inRange(other.position, r);
	}

	public double OverlapFraction(GBObject other) {
		if (radius == 0)
			return 0;
		// returns the fraction of our radius that other overlaps
		double dist = (position.subtract(other.position)).norm(); // center-to-center
																	// dist
		dist = Math.max(radius + other.radius - dist, 0); // overlap of edges
		return Math.min(dist / radius, 1.0); // don't overlap more than 1
	}

	public void BasicCollide(GBObject other) throws GBGenericError, GBAbort,
			GBBadComputedValueError, GBBadArgumentError {
		if (Intersects(other)) {
			CollideWith(other);
			other.CollideWith(this);
		}
	}

	public void SolidCollide(GBObject other, double coefficient)
			throws GBGenericError, GBAbort, GBBadComputedValueError,
			GBBadArgumentError {
		if (Intersects(other)) {
			CollideWith(other);
			other.CollideWith(this);
			ElasticBounce(other, coefficient);
		}
	}

	public void PushBy(FinePoint impulse) {
		if (mass != 0)
			velocity = velocity.add(impulse.divide(mass));
	}

	public void PushBy(double impulse, double dir) {
		PushBy(FinePoint.makePolar(impulse, dir));
	}

	public void Push(GBObject other, double impulse) {
		FinePoint cc = other.position.subtract(position); // center-to-center
		if (cc.norm() == 0)
			return; // don't push if in same location
		other.PushBy(cc.divide(cc.norm()).multiply(impulse));
	}

	public void ElasticBounce(GBObject other, double coefficient /*
																 * TODO:
																 * substitute
																 * default value
																 * if needed: =1
																 */) {
		double m1 = mass;
		double m2 = other.mass;
		if (m1 == 0 || m2 == 0)
			return; // can't bounce massless objects
		FinePoint cc = other.position.subtract(position); // center-to-center
		if (cc.normSquare() == 0)
			cc.set(1, 0); // if in same place, pick an arbitrary cc
		double dist = cc.norm();
		FinePoint rv1 = velocity.projection(cc); // radial component of velocity
		FinePoint rv2 = other.velocity.projection(cc);
		if (velocity.dotProduct(cc) - other.velocity.dotProduct(cc) > 0) {
			// if still moving closer...
			FinePoint center = (rv1.multiply(m1).add(rv2.multiply(m2)))
					.divide((m1 + m2)); // velocity of center-of-mass
			/*
			 * TODO: convert this to work without the FinePoint operators
			 * Accelerate((rv2 - center) * (m2 * coefficient) / m1 - (rv1 -
			 * center)); other.Accelerate((rv1 - center) * (m1 * coefficient) /
			 * m2 - (rv2 - center));
			 */
			Accelerate(rv2.subtract(center).multiply(m2 * coefficient).divide(
					m1).subtract(rv1.subtract(center)));
			other.Accelerate(rv1.subtract(center).multiply(m1 * coefficient)
					.divide(m2).subtract(rv2.subtract(center)));
		}
		double totalr = radius + other.radius;
		double overlap = totalr - dist;
		if (overlap > kMaxOverlap) {
			/* TODO: convert this to work without the FinePoint operators */
			/*
			 * FinePoint away1 = - cc / dist * m2 / (m1 + m2); FinePoint away2 =
			 * cc / dist * m1 / (m1 + m2);
			 */
			FinePoint away1 = cc.negate().divide(dist).multiply(m2).divide(
					m1 + m2);
			FinePoint away2 = cc.divide(dist).multiply(m1).divide(m1 + m2);
			MoveBy(away1.multiply(kCollisionRepulsionSpeed));
			other.MoveBy(away2.multiply(kCollisionRepulsionSpeed));
			Accelerate(away1.multiply(kCollisionRepulsion));
			other.Accelerate(away2.multiply(kCollisionRepulsion));
		}
	}

	public void Move() {
		position = position.add(velocity);
	}
	// TODO: Put these back in when we start on the GUI
	// Draw a shadow slightly offset from our location.
	/*
	 * public void DrawShadow(GBGraphics g, GBProjection proj, FinePoint offset,
	 * GBColor color) { GBRect shadow(proj.ToScreenX(Left() + offset.x),
	 * proj.ToScreenY(Top() + offset.y), proj.ToScreenX(Right() + offset.x),
	 * proj.ToScreenY(Bottom() + offset.y)); g.DrawSolidOval(shadow, color); }
	 * 
	 * public void DrawMini(GBGraphics g, GBRect where) { if ( where.Width() <
	 * kMaxSquareMiniSize ) g.DrawSolidRect(where, Color()); else
	 * g.DrawSolidOval(where, Color()); }
	 */

};