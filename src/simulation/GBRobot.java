package simulation;

// GBRobot.cpp
// Grobots (c) 2002-2007 Devon and Warren Schudy
// Distributed under the GNU General Public License.
import sides.RobotType;
import sides.Side;
import support.FinePoint;
import support.GBObjectClass;
import brains.Brain;
import brains.GBBadSymbolIndexError;
import exception.GBAbort;
import exception.GBBadArgumentError;
import exception.GBBadComputedValueError;
import exception.GBGenericError;
import exception.GBIndexOutOfRangeError;
import exception.GBNilPointerError;
import exception.GBOutOfMemoryError;
import support.GBColor;

public class GBRobot extends GBObject {
	RobotType type;
	Brain brain;
	int id, parent;
	Side lastHit; // who hit us last
	int recentDamage;
	int friendlyCollisions, enemyCollisions, shotCollisions, foodCollisions,
			wallCollisions;
	// public:
	// hardware state
	public GBHardwareState hardware;
	public boolean dead;
	public double flag;

	public static final double kRobotRadiusFactor = 0.1;
	public static final double kRobotRadiusPadding = 3; // robots look this much
														// heavier than they are

	public static final double kFriction = 0.001;
	public static final double kLinearDragFactor = 0.01;
	public static final double kQuadraticDragFactor = 0.15;

	public static final double kShieldEffectiveness = 1;
	public static final double kStandardShieldPerMass = 1.0; // shield per mass
																// for
																// blue-white
																// shield
																// graphic
	public static final float kMinMinimapBotContrast = 0.35f;
	public static final float kMinMeterContrast = 0.3f;
	public static final int kRecentDamageTime = 10;

	public static final double kRingGrowthRate = 0.1f;

	public void Recalculate() {
		mass = type.Mass() + hardware.constructor.FetusMass();
		radius = Math.sqrt(mass + kRobotRadiusPadding) * kRobotRadiusFactor;
	}

	public GBRobot(RobotType rtype, FinePoint where) throws GBGenericError,
			GBNilPointerError, GBIndexOutOfRangeError, GBBadSymbolIndexError,
			GBOutOfMemoryError {
		super(where, 0.5, rtype.Mass());
		type = rtype;
		brain = rtype.MakeBrain();
		id = rtype.side.GetNewRobotNumber();
		hardware = new GBHardwareState(rtype.Hardware());
		// if (rtype == null)
		// throw new GBNilPointerError();
		hardware.radio.Reset(Owner());
		Recalculate();
	}

	public GBRobot(RobotType rtype, FinePoint where, FinePoint vel, int parentID)
			throws GBGenericError, GBIndexOutOfRangeError, GBNilPointerError,
			GBBadSymbolIndexError, GBOutOfMemoryError {
		super(where, 0.5, vel, rtype.Mass());
		type = rtype;
		brain = rtype.MakeBrain();
		id = rtype.side.GetNewRobotNumber();
		parent = parentID;
		hardware = new GBHardwareState(rtype.Hardware());
		// if (rtype == null)
		// throw new GBNilPointerError();
		hardware.radio.Reset(Owner());
		Recalculate();
	}

	public RobotType Type() {
		return type;
	}

	public int ID() {
		return id;
	}

	public int ParentID() {
		return parent;
	}

	@Override
	public String toString() {
		return type.Description() + " #" + id;
	}

	@Override
	public String Details() {
		String dets = Energy() + " energy, " + hardware.Armor() + " armor";
		if (hardware.constructor.Progress() != 0)
			dets += ", "
					+ Math.round(hardware.constructor.Progress()
							/ hardware.constructor.Type().Cost() * 100) + "%"
					+ " " + hardware.constructor.Type().name;
		return dets;
	}

	public int Collisions() {
		return friendlyCollisions + enemyCollisions + wallCollisions;
	}

	public int FriendlyCollisions() {
		return friendlyCollisions;
	}

	public int EnemyCollisions() {
		return enemyCollisions;
	}

	public int FoodCollisions() {
		return foodCollisions;
	}

	public int ShotCollisions() {
		return shotCollisions;
	}

	public int WallCollisions() {
		return wallCollisions;
	}

	public Side LastHit() {
		return lastHit;
	}

	public Brain Brain() {
		return brain;
	}

	public double ShieldFraction() {
		if (hardware.ActualShield() != 0)
			// return 1 / (square(hardware.ActualShield() * kShieldEffectiveness
			// / mass) + 1);
			return 1 / (Math.pow(hardware.ActualShield() * kShieldEffectiveness
					/ mass, 2) + 1);
		return 1;
	}
	@Override
	public void TakeDamage(double amount, Side origin) {
		double actual = amount * type.MassiveDamageMultiplier(mass)
				* ShieldFraction();
		hardware.TakeDamage(actual);
		lastHit = origin;
		recentDamage = kRecentDamageTime;
		if (origin == Owner())
			Owner().Scores().ReportFriendlyFire(actual);
		else {
			Owner().Scores().ReportDamageTaken(actual);
			if (origin != null)
				origin.Scores().ReportDamageDone(actual);
		}
	}
	@Override
	public double TakeEnergy(double amount) {
		return hardware.UseEnergyUpTo(amount);
	}
	@Override
	public double GiveEnergy(double amount) throws GBBadArgumentError {
		return hardware.GiveEnergy(amount);
	}
	@Override
	public double MaxTakeEnergy() {
		return hardware.Energy();
	}
	@Override
	public double MaxGiveEnergy() {
		return hardware.MaxEnergy() - hardware.Energy();
	}

	public void EngineSeek(FinePoint pos, FinePoint vel) {
		FinePoint delta = pos.subtract(Position());
		if (vel.isZero()
				&& (delta.add(Velocity().multiply(11))).norm() < radius)
			hardware.SetEnginePower(0);
		else {
			hardware.SetEnginePower(hardware.EngineMaxPower());
			hardware.SetEngineVelocity(vel.add(delta.multiply(0.09))); // FIXME:
																		// better
																		// seek
		}
	}

	public void Die(Side killer) {
		dead = true;
		lastHit = killer;
	}
	@Override
	public void Move() {
		friendlyCollisions = 0;
		enemyCollisions = 0;
		foodCollisions = 0;
		shotCollisions = 0;
		wallCollisions = 0;
		super.Move();
		Drag(kFriction, kLinearDragFactor, kQuadraticDragFactor);
	}
	@Override
	public void CollideWithWall() {
		wallCollisions++;
	}
	@Override
	public void CollideWith(GBObject other) throws GBBadComputedValueError,
			GBBadArgumentError {
		switch (other.Class()) {
		case ocRobot:
			if (other.Owner() == Owner())
				friendlyCollisions++;
			else
				enemyCollisions++;
			break;
		case ocFood: {
			foodCollisions++;
			// FIXME shields don't affect eaters
			double eaten = other.TakeEnergy(hardware.EaterLimit());
			hardware.Eat(eaten);
			Side source = other.Owner();
			if (source == null)
				Owner().ReportTheotrophy(eaten);
			else if (source == Owner())
				Owner().ReportCannibalism(eaten);
			else
				Owner().ReportHeterotrophy(eaten);
		}
			break;
		case ocShot:
			shotCollisions++;
			break;
		default:
			break;
		}
	}
	@Override
	public void Think(GBWorld world) throws GBBadArgumentError,
			GBOutOfMemoryError, GBGenericError, GBAbort {
		if (brain != null)
			brain.think(this, world);
	}
	@Override
	public void Act(GBWorld world) throws GBNilPointerError,
			GBBadArgumentError, GBGenericError, GBBadSymbolIndexError,
			GBOutOfMemoryError {
		hardware.Act(this, world);
		if (dead) {
			if (lastHit == null)
				; // no reports for accidents
			else if (lastHit == Owner())
				lastHit.ReportSuicide(Biomass());
			else
				lastHit.ReportKilled(Biomass());
			Owner().ReportDead(Biomass());
		}
		if (recentDamage != 0)
			--recentDamage;
		Recalculate();
	}

	@Override
	public void CollectStatistics(GBWorld world) {
		double bm = Biomass();
		Owner().ReportRobot(bm, type, Position());
		type.ReportRobot(bm);
		world.ReportRobot(bm);
	}
	@Override
	public GBObjectClass Class() {
		if (dead)
			return GBObjectClass.ocDead;
		else
			return GBObjectClass.ocRobot;
	}
	@Override
	public Side Owner() {
		return type.side;
	}

	@Override
	public double Energy() {
		return hardware.Energy();
	}

	public double Biomass() {
		return type.Cost() - type.Hardware().InitialEnergy()
				+ hardware.Energy() + hardware.constructor.Progress();
	}

	@Override
	public double Interest() {
		double interest = Biomass() * (0.001 + Speed() * 0.01)
				+ hardware.ActualShield() / 2;
		if (hardware.blaster.Cooldown() != 0)
			interest += hardware.blaster.Damage() * 10
					/ hardware.blaster.ReloadTime();
		if (hardware.grenades.Cooldown() != 0)
			interest += hardware.grenades.Damage() * 10
					/ hardware.grenades.ReloadTime();
		if (hardware.blaster.Cooldown() != 0)
			interest += Math.abs(hardware.forceField.Power()) * 15 + 1;
		if (hardware.syphon.Rate() != 0)
			interest += Math.abs(hardware.syphon.Rate()) + 1;
		if (hardware.enemySyphon.Rate() != 0)
			interest += Math.abs(hardware.enemySyphon.Rate()) * 5 + 2;
		return interest;
	}
	
	 //This color is only used for the minimap, so it has built-in contrast
	 // handling. 
	@Override
	public GBColor Color() { return
	  Owner().Color().EnsureContrastWithBlack(kMinMinimapBotContrast)
	  .Mix(0.9f, type.Color()); }
	 /* 
	 * 
	 * //Draw a meter with whichever color gives better contrast. If pulse, make
	 * the meter flash. static void DrawMeter(GBGraphics g, double fraction,
	 * GBRect where, int zeroAngle, int oneAngle, int width, GBColor color1,
	 * GBColor color2, GBColor bgcolor, boolean pulse) { public static final
	 * GBColor & color = bgcolor.ChooseContrasting(color1, color2,
	 * kMinMeterContrast); short angle = Math.ceil(fraction * (oneAngle -
	 * zeroAngle)); float phase = Milliseconds() * 6.28 / 500; g.DrawArc(where,
	 * zeroAngle + (angle < 0 ? angle : 0), Math.abs(angle), color * (pulse ?
	 * 0.85 + 0.15f * sin(phase) : 1.0f), width); }
	 * 
	 * void DrawUnderlay(GBGraphics g, GBProjection proj, GBRect where, boolean
	 * detailed) { //halo: crashes, prints GBRect halo(where); halo.Shrink(-3);
	 * if ( brain && brain.Status() != bsOK ) g.DrawSolidOval(halo,
	 * brain.Status() == bsStopped ? GBColor::yellow : GBColor::red); //velocity
	 * and engine-velocity //if ( Velocity().Nonzero() ) // DrawShadow(g, proj,
	 * Velocity() * -2.5, hardware.EnginePower() ? GBColor::gray :
	 * GBColor::darkGray); if ( detailed && hardware.EnginePower() &&
	 * hardware.EngineVelocity().Nonzero() ) { FinePoint dv =
	 * hardware.EngineVelocity() - Velocity(); if ( dv.Norm() > 0.01 ) {
	 * FinePoint head = Position() + dv.Unit() * (Radius() +
	 * hardware.EnginePower() / sqrt(Mass()) * 30);
	 * g.DrawLine(proj.ToScreenX(Position().x), proj.ToScreenY(Position().y),
	 * proj.ToScreenX(head.x), proj.ToScreenY(head.y), GBColor::darkGreen, 2);
	 * //DrawShadow(g, proj, dv.Unit() * hardware.EnginePower() / Mass() * -30,
	 * GBColor::darkGreen); } } //weapon ranges? //sensor results? }
	 * 
	 * void Draw(GBGraphics g, GBProjection proj, GBRect where, boolean
	 * detailed) { if(where.Width() <= 5) { DrawMini(g,where); return; } short
	 * meterWidth = Math.max(1, (where.Width() + 10) / 10); //background and rim
	 * g.DrawSolidOval(where, GBColor::darkRed.Mix(0.8f * recentDamage /
	 * kRecentDamageTime, Owner().Color())); g.DrawOpenOval(where,
	 * type.Color()); // meters if ( detailed ) { // energy meter if (
	 * hardware.MaxEnergy() ) DrawMeter(g, hardware.Energy() /
	 * hardware.MaxEnergy(), where, 180, 0, meterWidth, GBColor::green,
	 * GBColor(0, 0.5f, 1), Owner().Color(), hardware.Eaten() ||
	 * hardware.syphon.Syphoned() || hardware.enemySyphon.Syphoned()); // damage
	 * meter if ( hardware.Armor() < hardware.MaxArmor() ) DrawMeter(g,
	 * double(1) - hardware.Armor() / hardware.MaxArmor(), where, 360, 180,
	 * meterWidth, GBColor::red, GBColor::lightGray, Owner().Color(),
	 * hardware.RepairRate()); // gestation meter if (
	 * hardware.constructor.Progress() ) { GBRect meterRect = where;
	 * meterRect.Shrink(meterWidth); DrawMeter(g,
	 * hardware.constructor.Fraction(), meterRect, 0, 360, 1, GBColor::yellow,
	 * GBColor::darkGreen, Owner().Color(), hardware.constructor.Rate()); } } //
	 * decoration short thickness = (15 + where.right - where.left) / 15; //was
	 * 2 for >15 else 1 GBRect dec((where.left * 2 + where.right + 2) / 3,
	 * (where.top * 2 + where.bottom + 2) / 3, (where.left + where.right * 2 +
	 * 1) / 3, (where.top + where.bottom * 2 + 1) / 3); short dx = where.Width()
	 * / 4; short dy = where.Height() / 4; //cross, hline, and vline draw in
	 * bigDec instead of dec GBRect bigDec(where.CenterX() - dx, where.CenterY()
	 * - dy, where.CenterX() + dx, where.CenterY() + dy); //flash decoration
	 * when reloading or sensing public static final GBColor & basecolor =
	 * type.Decoration() == rdNone ? Owner().Color() : type.DecorationColor();
	 * GBColor color = basecolor; if ( hardware.grenades.Cooldown() ) color =
	 * GBColor::yellow.Mix((float)hardware.grenades.Cooldown() /
	 * hardware.grenades.ReloadTime(), basecolor); else if (
	 * hardware.blaster.Cooldown() ) color =
	 * GBColor::magenta.Mix((float)hardware.blaster.Cooldown() /
	 * hardware.blaster.ReloadTime(), basecolor); switch ( type.Decoration() ) {
	 * case rdNone: default: if ( ! hardware.blaster.Cooldown() && !
	 * hardware.grenades.Cooldown() ) break; //if we're flashing, fall through
	 * and draw a dot case rdDot: g.DrawSolidOval(GBRect(where.CenterX() -
	 * thickness, where.CenterY() - thickness, where.CenterX() + thickness,
	 * where.CenterY() + thickness), color); break; case rdCircle:
	 * g.DrawOpenOval(dec, color, thickness); break; case rdSquare:
	 * g.DrawOpenRect(dec, color, thickness); break; case rdTriangle:
	 * g.DrawLine(dec.left, dec.bottom, dec.CenterX(), dec.top, color,
	 * thickness); g.DrawLine(dec.CenterX(), dec.top, dec.right, dec.bottom,
	 * color, thickness); g.DrawLine(dec.left, dec.bottom, dec.right,
	 * dec.bottom, color, thickness); break; case rdCross:
	 * g.DrawLine(where.CenterX(), bigDec.top, where.CenterX(), bigDec.bottom,
	 * color, thickness); g.DrawLine(bigDec.left, where.CenterY(), bigDec.right,
	 * where.CenterY(), color, thickness); break; case rdX: g.DrawLine(dec.left,
	 * dec.top, dec.right, dec.bottom, color, thickness); g.DrawLine(dec.left,
	 * dec.bottom, dec.right, dec.top, color, thickness); break; case rdHLine:
	 * g.DrawLine(bigDec.left, where.CenterY(), bigDec.right, where.CenterY(),
	 * color, thickness); break; case rdVLine: g.DrawLine(where.CenterX(),
	 * bigDec.top, where.CenterX(), bigDec.bottom, color, thickness); break;
	 * case rdSlash: g.DrawLine(dec.left, dec.bottom, dec.right, dec.top, color,
	 * thickness); break; case rdBackslash: g.DrawLine(dec.left, dec.top,
	 * dec.right, dec.bottom, color, thickness); break; } }
	 * 
	 * void DrawOverlay(GBGraphics & g, GBProjection & proj, GBRect & where,
	 * boolean/*detailed
	 */
	/*
	 * ) { // shield if ( hardware.ActualShield() > 0 ) { GBRect halo(where);
	 * halo.Shrink(-2); g.DrawOpenOval(halo, GBColor(0.3f, 0.5f, 1)
	 * (hardware.ActualShield() / (mass * kStandardShieldPerMass))); } //radio
	 * rings for ( int age = 0; age < kRadioHistory; ++ age ) { if ( !
	 * hardware.radio.sent[age] && ! hardware.radio.writes[age] ) continue;
	 * double r = kRingGrowthRate * (age + 1); GBRect
	 * ring(proj.ToScreenX(Position().x - r), proj.ToScreenY(Position().y + r),
	 * proj.ToScreenX(Position().x + r), proj.ToScreenY(Position().y - r));
	 * float intensity = Math.min(2.0f * (kRadioHistory - age) / kRadioHistory,
	 * 1.0f); g.DrawOpenOval(ring, (hardware.radio.sent[age] ? GBColor(0.6f,
	 * 0.5f, 1) : GBColor(1, 0.8f, 0.5f)) * intensity); } }
	 * 
	 * void DrawMini(GBGraphics & g, GBRect & where) { if ( where.Width() <= 4 )
	 * g.DrawSolidRect(where, Color()); else { g.DrawSolidOval(where,
	 * Owner().Color()); g.DrawOpenOval(where, Owner().Color().Mix(0.5f,
	 * type.Color())); } }
	 */
}
