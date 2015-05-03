/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

import sides.Side;
//Maps FinePoints to screen locations
import support.FinePoint;

public abstract class GBObject {
	FinePoint position;
	FinePoint velocity;

	public BufferedImage image;

	protected double radius;
	protected double mass;

	public static final double kCollisionRepulsion = 0.02;
	public static final double kCollisionRepulsionSpeed = 0.1;
	public static final double kMaxOverlap = 0.04;
	public static final short kMaxSquareMiniSize = 4;

	// Some abstract functions aren't implemented by all descendants.
	// Java hates this so they were made protected and given default behavior
	// here.
	protected void TakeDamage(double amount, Side origin) {
	}

	protected double TakeEnergy(double amount) {
		return 0;
	}

	protected double GiveEnergy(double amount) {
		return 0;
	}

	protected double MaxTakeEnergy() {
		return 0;
	}

	protected double MaxGiveEnergy() {
		return 0;
	}

	// high-level actions
	protected void Think(GBWorld world) {
	} // Not everything has a brain

	public abstract void Act(GBWorld world); // Everything can act

	protected void CollideWithWall() {
	}

	protected void CollideWith(GBObject other) {
	}

	protected void CollectStatistics(ScoreKeeper keeper) {
	}

	// high-level queries
	public abstract GBObjectClass Class();

	public Side Owner() {
		return null;
	} // food is not owned by anyone

	protected double Energy() {
		return 0;
	}

	protected double Interest() {
		return 0;
	} // how attractive to autocamera

	public String Details() {
		return "";
	}

	// evil antimodular drawing code
	public abstract Color Color();

	public abstract void Draw(Graphics g, GBProjection<GBObject> proj,
			boolean detailed);

	public void DrawUnderlay(Graphics g, GBProjection<GBObject> proj,
			boolean detailed) {
	}

	public void DrawOverlay(Graphics g, GBProjection<GBObject> proj,
			boolean detailed) {
	}

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
		position = position.add(delta);
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

	public void BasicCollide(GBObject other) {
		if (Intersects(other)) {
			CollideWith(other);
			other.CollideWith(this);
		}
	}

	public void SolidCollide(GBObject other, double coefficient) {
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
		if (cc.normSquare() == 0)
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
			Accelerate(rv2.subtract(center).multiply(m2 * coefficient)
					.divide(m1).subtract(rv1.subtract(center)));
			other.Accelerate(rv1.subtract(center).multiply(m1 * coefficient)
					.divide(m2).subtract(rv2.subtract(center)));
		}
		double totalr = radius + other.radius;
		double overlap = totalr - dist;
		if (overlap > kMaxOverlap) {
			FinePoint away1 = cc.negate().divide(dist).multiply(m2)
					.divide(m1 + m2);
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

	/**
	 * Returns a rectangle (approximately) representing the position of the
	 * rendered object on the screen.
	 * 
	 * @param proj
	 * @return
	 */
	protected Rectangle getScreenRect(GBProjection<GBObject> proj) {
		int oWidth = (int) Math.max(radius * proj.getScale() * 2, 1);
		return new Rectangle(proj.toScreenX(Left()), proj.toScreenY(Top()), oWidth, oWidth);
	}
	
	protected Ellipse2D.Double getScreenEllipse(GBProjection<GBObject> proj) {
		double oWidth = Math.max(radius * proj.getScale() * 2, 1);
		return new Ellipse2D.Double(proj.toScreenX(Left()), proj.toScreenY(Top()), oWidth, oWidth);
	}

	public void DrawShadow(Graphics g, GBProjection<GBObject> proj,
			FinePoint offset, Color color) {
		Graphics2D g2d = (Graphics2D) g;
		Ellipse2D.Double where = getScreenEllipse(proj);
		int scale = proj.getScale();
		where.x += offset.x * scale;
		where.y -= offset.y * scale;
		g2d.setPaint(color);
		g2d.fill(where);
	}

	protected void drawImage(Graphics g, GBProjection<GBObject> proj) {
		if (image == null)
			return;
		int x1 = proj.toScreenX(Left());
		int y1 = proj.toScreenY(Top());
		int x2 = proj.toScreenX(Right());
		int y2 = proj.toScreenY(Bottom());
		if (x2 - x1 < 2)
			x2 = x1 + 2;
		if (y2 - y1 < 2)
			y2 = y1 + 2;
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(image, x1, y1, x2, y2, 0, 0, image.getWidth(),
				image.getHeight(), null);
	}
};
