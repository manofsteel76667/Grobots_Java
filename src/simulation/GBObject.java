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

import Rendering.GBProjection;
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
	protected void takeDamage(double amount, Side origin) {
	}

	protected double takeEnergy(double amount) {
		return 0;
	}

	protected double giveEnergy(double amount) {
		return 0;
	}

	protected double maxTakeEnergy() {
		return 0;
	}

	protected double maxGiveEnergy() {
		return 0;
	}

	// high-level actions
	protected void think(GBWorld world) {
	} // Not everything has a brain

	public abstract void act(GBWorld world); // Everything can act

	protected void collideWithWall() {
	}

	protected void collideWith(GBObject other) {
	}

	protected void collectStatistics(ScoreKeeper keeper) {
	}

	// high-level queries
	public abstract GBObjectClass getObjectClass();

	public Side getOwner() {
		return null;
	} // food is not owned by anyone

	protected double getEnergy() {
		return 0;
	}

	protected double getInterest() {
		return 0;
	} // how attractive to autocamera
	
	public String getDetails() {
		return "";
	}

	// evil antimodular drawing code
	public abstract Color getColor();

	public abstract void draw(Graphics g, GBProjection proj,
			boolean detailed);

	public void drawUnderlay(Graphics g, GBProjection proj,
			boolean detailed) {
	}

	public void drawOverlay(Graphics g, GBProjection proj,
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

	public FinePoint getPosition() {
		return position;
	}

	public void setPosition(FinePoint newPosition) {
		position = newPosition;
	}

	public void moveBy(FinePoint delta) {
		position = position.add(delta);
	}

	public void moveBy(double deltax, double deltay) {
		position.x += deltax;
		position.y += deltay;
	}

	public double getLeft() {
		return position.x - radius;
	}

	public double getTop() {
		return position.y + radius;
	}

	public double getRight() {
		return position.x + radius;
	}

	public double getBottom() {
		return position.y - radius;
	}

	public FinePoint getVelocity() {
		return velocity;
	}

	public double getSpeed() {
		return velocity.norm();
	}

	public void setVelocity(double sx, double sy) {
		velocity.set(sx, sy);
	}

	public void setSpeed(double speed) {
		velocity.setNorm(speed);
	}

	public void accelerate(FinePoint deltav) {
		velocity = velocity.add(deltav);
	}

	public void drag(double friction, double linearCoeff, double quadraticCoeff) {
		if (velocity.isZero())
			return;
		double speed = getSpeed();
		speed -= speed * (speed * quadraticCoeff + linearCoeff);
		if (speed > friction)
			setSpeed(speed - friction);
		else
			setVelocity(0, 0);
	}

	public double getRadius() {
		return radius;
	}

	public double getMass() {
		return mass;
	}

	public boolean intersects(GBObject other) {
		double r = radius + other.radius;
		return other.position.x - position.x < r
				&& position.x - other.position.x < r
				&& other.position.y - position.y < r
				&& position.y - other.position.y < r
				&& position.inRange(other.position, r);
	}

	public double overlapFraction(GBObject other) {
		if (radius == 0)
			return 0;
		// returns the fraction of our radius that other overlaps
		double dist = (position.subtract(other.position)).norm(); // center-to-center
																	// dist
		dist = Math.max(radius + other.radius - dist, 0); // overlap of edges
		return Math.min(dist / radius, 1.0); // don't overlap more than 1
	}

	public void doBasicCollide(GBObject other) {
		if (intersects(other)) {
			collideWith(other);
			other.collideWith(this);
		}
	}

	public void doSolidCollide(GBObject other, double coefficient) {
		if (intersects(other)) {
			collideWith(other);
			other.collideWith(this);
			doElasticBounce(other, coefficient);
		}
	}

	public void pushBy(FinePoint impulse) {
		if (mass != 0)
			velocity = velocity.add(impulse.divide(mass));
	}

	public void pushBy(double impulse, double dir) {
		pushBy(FinePoint.makePolar(impulse, dir));
	}

	public void push(GBObject other, double impulse) {
		FinePoint cc = other.position.subtract(position); // center-to-center
		if (cc.normSquare() == 0)
			return; // don't push if in same location
		other.pushBy(cc.divide(cc.norm()).multiply(impulse));
	}

	public void doElasticBounce(GBObject other, double coefficient /*
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
			accelerate(rv2.subtract(center).multiply(m2 * coefficient)
					.divide(m1).subtract(rv1.subtract(center)));
			other.accelerate(rv1.subtract(center).multiply(m1 * coefficient)
					.divide(m2).subtract(rv2.subtract(center)));
		}
		double totalr = radius + other.radius;
		double overlap = totalr - dist;
		if (overlap > kMaxOverlap) {
			FinePoint away1 = cc.negate().divide(dist).multiply(m2)
					.divide(m1 + m2);
			FinePoint away2 = cc.divide(dist).multiply(m1).divide(m1 + m2);
			moveBy(away1.multiply(kCollisionRepulsionSpeed));
			other.moveBy(away2.multiply(kCollisionRepulsionSpeed));
			accelerate(away1.multiply(kCollisionRepulsion));
			other.accelerate(away2.multiply(kCollisionRepulsion));
		}
	}

	public void move() {
		position = position.add(velocity);
	}

	/**
	 * Returns a rectangle (approximately) representing the position of the
	 * rendered object on the screen.
	 * 
	 * @param proj
	 * @return
	 */
	protected Rectangle getScreenRect(GBProjection proj) {
		int oWidth = (int) Math.max(radius * proj.getScale() * 2, 1);
		return new Rectangle(proj.toScreenX(getLeft()), proj.toScreenY(getTop()), oWidth, oWidth);
	}
	
	protected Ellipse2D.Double getScreenEllipse(GBProjection proj) {
		double oWidth = Math.max(radius * proj.getScale() * 2, 1);
		return new Ellipse2D.Double(proj.toScreenX(getLeft()), proj.toScreenY(getTop()), oWidth, oWidth);
	}

	public void drawShadow(Graphics g, GBProjection proj,
			FinePoint offset, Color color) {
		Graphics2D g2d = (Graphics2D) g;
		Ellipse2D.Double where = getScreenEllipse(proj);
		int scale = proj.getScale();
		where.x += offset.x * scale;
		where.y -= offset.y * scale;
		g2d.setPaint(color);
		g2d.fill(where);
	}

	protected void drawImage(Graphics g, GBProjection proj) {
		if (image == null)
			return;
		int x1 = proj.toScreenX(getLeft());
		int y1 = proj.toScreenY(getTop());
		int x2 = proj.toScreenX(getRight());
		int y2 = proj.toScreenY(getBottom());
		if (x2 - x1 < 2)
			x2 = x1 + 2;
		if (y2 - y1 < 2)
			y2 = y1 + 2;
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(image, x1, y1, x2, y2, 0, 0, image.getWidth(),
				image.getHeight(), null);
	}
};
