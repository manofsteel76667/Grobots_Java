/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package support;

import java.awt.geom.Point2D;

/*
 * 2 paired doubles with associated math functions Used for coordinates and
 * vectors throughout the program
 */
public class FinePoint extends Point2D.Double {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4947339690552890027L;

	public FinePoint() {
		super();
	}

	public FinePoint(double nx, double ny) {
		super(nx, ny);
	}

	public FinePoint(Point2D.Double src) {
		super(src.x, src.y);
	}

	public FinePoint set(Point2D.Double newvalue) {
		x = newvalue.x;
		y = newvalue.y;
		return this;
	}

	public void set(double nx, double ny) {
		x = nx;
		y = ny;
	}

	public FinePoint add(Point2D.Double addend) {
		return new FinePoint(x + addend.x, y + addend.y);
	}

	public FinePoint subtract(Point2D.Double subtrahend) {
		return new FinePoint(x - subtrahend.x, y - subtrahend.y);
	}

	public FinePoint negate() {
		return new FinePoint(-x, -y);
	}

	public FinePoint multiply(double factor) {
		return new FinePoint(x * factor, y * factor);
	}

	public FinePoint multiply(int factor) {
		return new FinePoint(x * factor, y * factor);
	}

	public FinePoint divide(double divisor) {
		return new FinePoint(x / divisor, y / divisor);
	}

	public FinePoint divide(int divisor) {
		return new FinePoint(x / divisor, y / divisor);
	}

	public boolean inRange(Point2D.Double other, double range) {
		double r = range;
		double dx = x - other.x;
		double dy = y - other.y;
		return (dx * dx + dy * dy) <= r * r;
	}

	public boolean inRangeSquared(Point2D.Double other, double rangeSquared) {
		double r = rangeSquared;
		double dx = x - other.x;
		double dy = y - other.y;
		return (dx * dx + dy * dy) <= r;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = java.lang.Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = java.lang.Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof FinePoint))
			return false;
		FinePoint other = (FinePoint) obj;
		if (java.lang.Double.doubleToLongBits(x) != java.lang.Double
				.doubleToLongBits(other.x))
			return false;
		if (java.lang.Double.doubleToLongBits(y) != java.lang.Double
				.doubleToLongBits(other.y))
			return false;
		return true;
	}

	public boolean isNonzero() {
		return x != 0 && y != 0;
	}

	public boolean isZero() {
		return x == 0 && y == 0;
	}

	public void setPolar(double r, double theta) {
		x = r * Math.cos(theta);
		y = r * Math.sin(theta);
	}

	public FinePoint addPolar(double r, double theta) {
		return new FinePoint(x + r * Math.cos(theta), y + r * Math.sin(theta));
	}

	public static FinePoint makePolar(double r, double theta) {
		return new FinePoint(r * Math.cos(theta), r * Math.sin(theta));
	}

	public double normSquare() {
		return x * x + y * y;
	}

	public double norm() {
		return Math.sqrt(x * x + y * y);
	}

	/**
	 * Returns a point equal to 1 unit moved at the current angle from 0
	 * @return
	 */
	public FinePoint unit() {
		return makePolar(1, angle());
	}

	/**
	 * 
	 * @param norm
	 */
	public void setNorm(double norm) {
		setPolar(norm, angle());
		// this *= norm / Norm(); //faster but could overflow in some cases
	}

	public double angle() {
		return Math.atan2(y, x);
	}

	public void setAngle(double angle) {
		setPolar(norm(), angle);
	}

	public double dotProduct(FinePoint other) {
		return x * other.x + y * other.y;
	}

	public FinePoint projection(FinePoint base) {
		return base.multiply(base.dotProduct(base)).divide(base.normSquare());
	}

	public double cross(FinePoint other) {
		return x * other.y - other.x * y;
	}

	public FinePoint rotateTo(FinePoint base) {
		FinePoint u = base.unit();
		return new FinePoint(x * u.x - y * u.y, y * u.x + x * u.y);
	}

	public FinePoint rotateFrom(FinePoint base) {
		FinePoint u = base.unit();
		return new FinePoint(x * u.x + y * u.y, y * u.x - x * u.y);
	}

	@Override
	public String toString() {
		return String.format("(%f,  %f)", x, y);
	}

	public String toString(int decimals) {
		return String.format("(%." + Integer.toString(decimals) + "f,  %."
				+ Integer.toString(decimals) + "f)", x, y);
	}
}
