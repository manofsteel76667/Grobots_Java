package support;

public class FinePoint {
	// FinePoint.cpp
	// Grobots (c) 2002-2004 Devon and Warren Schudy
	// Distributed under the GNU General Public License.

	/*
	 * 2 paired doubles with associated math functions Used for coordinates and
	 * vectors throughout the program
	 */

	// typedef FinePoint GBVector;

	// implementation //
	public double x;
	public double y;

	public FinePoint() {
		x = 0;
		y = 0;
	}

	public FinePoint(double nx, double ny) {
		x = nx;
		y = ny;
	}

	public FinePoint(FinePoint src) {
		x = src.x;
		y = src.y;
	}

	public FinePoint set(FinePoint newvalue) {
		x = newvalue.x;
		y = newvalue.y;
		return this;
	}

	public void set(double nx, double ny) {
		x = nx;
		y = ny;
	}

	public FinePoint add(FinePoint addend) {
		return new FinePoint(x + addend.x, y + addend.y);
	}

	public FinePoint subtract(FinePoint subtrahend) {
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

	// comparisons
	public boolean equals(FinePoint other) {
		return (x == other.x && y == other.y);
	}

	public boolean inRange(FinePoint other, double range) {
		double r = range;
		double dx = x - other.x;
		double dy = y - other.y;
		return (dx * dx + dy * dy) <= r * r;
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

	public FinePoint unit() {
		return makePolar(1, angle());
	}

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
		// TODO: C++ version was return base * DotProduct(base) /
		// base.NormSquare(); need to verify conversion
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

}
