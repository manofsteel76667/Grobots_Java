package simulation;

// GBSensorShot.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.
import sides.Side;
import support.FinePoint;
import support.GBObjectClass;

class GBSensorShot extends GBObject {
	GBRobot owner;
	Side side;
	GBSensorState state;
	GBObjectClass seen;
	int age;
	FinePoint focus;

	// public:

	public static final int kSensorShotMinLifetime = 15;

	int Lifetime() {
		return (int) Math.floor(Math.max(radius, kSensorShotMinLifetime));
	}

	public GBSensorShot(FinePoint fcs, GBRobot who, GBSensorState st) {
		super(who.Position(), st.MaxRange());
		owner = who;
		side = who.Owner();
		state = st;
		seen = st.Seen();
		focus = fcs;
	}

	@Override
	public void CollideWith(GBObject other) {
		if (other.Class() == GBObjectClass.ocRobot && (GBRobot) other == owner)
			return; // Seeing self is never allowed
		state.Report(new GBSensorResult(other, (other.Position()
				.subtract(focus)).norm())); // most logic is now in SensorState
	}

	@Override
	public void Act(GBWorld world) {
		age++;
	}

	@Override
	public GBObjectClass Class() {
		if (age >= Lifetime())
			return GBObjectClass.ocDead;
		else
			return GBObjectClass.ocSensorShot;
	}

	@Override
	public String toString() {
		String classname;
		switch (seen) {
		case ocRobot:
			classname = "Robot";
			break;
		case ocFood:
			classname = "Food";
			break;
		case ocShot:
			classname = "Shot";
			break;
		default:
			classname = "Mystery";
			break;
		}
		return classname + " sensor for " + owner.toString();
	}

	// TODO: when GUI is done
	/*
	 * public static final GBColor Color() { float fraction = 1.0 - (float)age /
	 * Lifetime(); switch ( seen ) { case ocRobot: return GBColor(0.4f, 0.8f, 1)
	 * * fraction; case ocFood: return GBColor(0.5f, 1, 0.5f) * fraction; case
	 * ocShot: return GBColor(1, 1, 0.5f) * fraction; } return
	 * GBColor(fraction); }
	 */

	public GBRobot Firer() {
		return owner;
	}

	public GBObjectClass Seen() {
		return age == 0 ? seen : GBObjectClass.ocDead;
	}

	@Override
	public Side Owner() {
		return side;
	}
	// TODO: When GUI is done
	/*
	 * void Draw(GBGraphics g, GBProjection & proj, GBRect where, boolean
	 * /*detailed
	 *//*
		 * ) { // show focus, owner, and side? g.DrawOpenOval(where, Color()); }
		 * 
		 * void DrawMini(GBGraphics & g, GBRect & where) { g.DrawOpenOval(where,
		 * Color()); }8?
		 */

};
