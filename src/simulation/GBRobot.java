package simulation;

// GBRobot.cpp
// Grobots (c) 2002-2007 Devon and Warren Schudy
// Distributed under the GNU General Public License.
import sides.GBRobotDecoration;
import sides.RobotType;
import sides.Side;
import support.FinePoint;
import support.GBObjectClass;
import brains.Brain;
import brains.BrainStatus;
import brains.GBBadSymbolIndexError;
import exception.GBAbort;
import exception.GBBadArgumentError;
import exception.GBBadComputedValueError;
import exception.GBGenericError;
import exception.GBIndexOutOfRangeError;
import exception.GBNilPointerError;
import exception.GBOutOfMemoryError;
import support.GBColor;
import support.GBGraphics;

import java.awt.*;

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

	// This color is only used for the minimap, so it has built-in contrast
	// handling.
	@Override
	public GBColor Color() {
		return Owner().Color().EnsureContrastWithBlack(kMinMinimapBotContrast)
				.Mix(0.9f, type.Color());
	}

	// Draw a meter with whichever color gives better contrast. If pulse, make
	// the meter flash.
	static void DrawMeter(Graphics g, double fraction, Rectangle where,
			int zeroAngle, int oneAngle, int width, GBColor color1,
			GBColor color2, GBColor bgcolor, boolean pulse) {
		GBColor color = bgcolor.ChooseContrasting(color1, color2,
				kMinMeterContrast);
		int angle = (int) Math.ceil(fraction * (oneAngle - zeroAngle));
		float phase = System.currentTimeMillis() * 6.28f / 500;
		GBGraphics.drawArc(g, where, zeroAngle + (angle < 0 ? angle : 0), Math
				.abs(angle), color
				.multiply(pulse ? 0.85f + 0.15f * (float) Math.sin(phase)
						: 1.0f), width);

	}

	@Override
	protected void DrawUnderlay(Graphics g, GBProjection proj, Rectangle where,
			boolean detailed) {
		// halo: crashes, prints
		Rectangle halo = new Rectangle(where);
		halo.grow(3, 3);
		if (brain != null)
			if (brain.status != BrainStatus.bsOK)
				GBGraphics.fillOval(g, halo,
						brain.status == BrainStatus.bsStopped ? Color.yellow
								: Color.red);
		// velocity and engine-velocity
		if (Velocity().isNonzero())
			DrawShadow(g, proj, Velocity().multiply(-2.5),
					hardware.EnginePower() != 0 ? Color.gray : Color.darkGray);
		if (detailed && hardware.EnginePower() != 0
				&& hardware.EngineVelocity().isNonzero()) {
			FinePoint dv = hardware.EngineVelocity().subtract(Velocity());
			if (dv.norm() > 0.01) {
				FinePoint head = Position().add(
						dv.unit().multiply(
								Radius() + hardware.EnginePower()
										/ Math.sqrt(Mass()) * 30));
				((Graphics2D) g).setStroke(new BasicStroke(2));
				GBGraphics.drawLine(g, proj.ToScreenX(Position().x),
						proj.ToScreenY(Position().y), proj.ToScreenX(head.x),
						proj.ToScreenY(head.y), Color.GREEN);
				DrawShadow(g, proj, dv.unit().multiply(hardware.EnginePower())
						.divide(Mass()).multiply(-30), Color.GREEN);
			}
		}
		// weapon ranges? //sensor results?
	}

	@Override
	public void Draw(Graphics g, GBProjection proj, Rectangle where,
			boolean detailed) {
		if (where.getWidth() <= 5) {
			DrawMini(g, where);
			return;
		}
		int meterWidth = (int) Math.max(1, (where.getWidth() + 10) / 10);
		// background and rim
		GBGraphics.fillOval(
				g,
				where,
				new GBColor(Color.red).Mix(0.8f * recentDamage
						/ kRecentDamageTime, Owner().Color()));
		GBGraphics.drawOval(g, where, type.Color());
		// meters
		if (detailed) {
			// energy meter
			if (hardware.MaxEnergy() != 0)
				DrawMeter(g, hardware.Energy() / hardware.MaxEnergy(), where,
						180, 0, meterWidth, new GBColor(Color.GREEN),
						new GBColor(0, 0.5f, 1), Owner().Color(),
						hardware.Eaten() != 0
								|| hardware.syphon.Syphoned() != 0
								|| hardware.enemySyphon.Syphoned() != 0);
			// damage meter
			if (hardware.Armor() < hardware.MaxArmor())
				DrawMeter(g, 1.0 - hardware.Armor() / hardware.MaxArmor(),
						where, 360, 180, meterWidth, new GBColor(Color.red),
						new GBColor(Color.lightGray), Owner().Color(),
						hardware.RepairRate() != 0);
			// gestation meter
			if (hardware.constructor.Progress() != 0) {
				Rectangle meterRect = where;
				meterRect.grow(meterWidth, meterWidth);
				DrawMeter(g, hardware.constructor.Fraction(), meterRect, 0,
						360, 1, new GBColor(Color.yellow), new GBColor(
								Color.GREEN), Owner().Color(),
						hardware.constructor.Rate() != 0);
			}
		}
		// decoration
		int thickness = (int) (15 + where.getWidth()) / 15; // was 2 for >15
															// else 1
		((Graphics2D) g).setStroke(new BasicStroke(thickness));
		Rectangle dec = new Rectangle(
				(int) (where.getX() * 2 + (where.getX() + where.getWidth()) + 2) / 3,
				(int) (where.getY() * 2 + (where.getY() + where.getHeight()) + 2) / 3,
				(int) (where.getX() + (where.getX() + where.getWidth()) * 2 + 1) / 3,
				(int) (where.getY() + (where.getY() + where.getHeight()) * 2 + 1) / 3);
		int dx = (int) (where.getWidth() / 4);
		int dy = (int) (where.getHeight() / 4);
		// cross, hline, and vline draw in bigDec instead of dec
		Rectangle bigDec = new Rectangle((int) where.getCenterX() - dx,
				(int) where.getCenterY() - dy, (int) where.getCenterX() + dx,
				(int) where.getCenterY() + dy);
		// flash decoration when reloading or sensing
		GBColor basecolor = type.Decoration() == GBRobotDecoration.none ? Owner()
				.Color() : type.DecorationColor();
		GBColor color = basecolor;
		if (hardware.grenades.Cooldown() != 0)
			color = new GBColor(Color.yellow).Mix(
					(float) hardware.grenades.Cooldown()
							/ hardware.grenades.ReloadTime(), basecolor);
		else if (hardware.blaster.Cooldown() != 0)
			color = new GBColor(Color.magenta).Mix(
					(float) hardware.blaster.Cooldown()
							/ hardware.blaster.ReloadTime(), basecolor);
		switch (type.Decoration()) {
		case none:
		default:
			if (hardware.blaster.Cooldown() == 0
					&& hardware.grenades.Cooldown() == 0)
				break;
			// if we're flashing, fall through and draw a dot
		case dot:
			GBGraphics.fillOval(g,
					new Rectangle(((int) where.getCenterX() - thickness),
							(int) (where.getCenterY() - thickness),
							(int) (where.getCenterX() + thickness),
							(int) (where.getCenterY() + thickness)), color);
			break;
		case circle:
			GBGraphics.drawOval(g, dec, color);
			break;
		case square:
			GBGraphics.drawRect(g, dec, color);
			break;
		case triangle:
			GBGraphics.drawLine(g, dec.getX(), dec.getY() + dec.getHeight(),
					dec.getCenterX(), dec.getY(), color);
			GBGraphics.drawLine(g, dec.getCenterX(), dec.getY(), dec.getX()
					+ dec.getWidth(), dec.getY() + dec.getHeight(), color);
			GBGraphics.drawLine(g, dec.getX(), dec.getY() + dec.getHeight(),
					dec.getX() + dec.getWidth(), dec.getY() + dec.getHeight(),
					color);
			break;
		case cross:
			GBGraphics.drawLine(g, where.getCenterX(), bigDec.getY(),
					where.getCenterX(), bigDec.getY() + dec.getHeight(), color);
			GBGraphics.drawLine(g, bigDec.getX(), where.getCenterY(),
					bigDec.getX() + dec.getWidth(), where.getCenterY(), color);
			break;
		case x:
			GBGraphics.drawLine(g, dec.getX(), dec.getY(),
					dec.getX() + dec.getWidth(), dec.getY() + dec.getHeight(),
					color);
			GBGraphics.drawLine(g, dec.getX(), dec.getY() + dec.getHeight(),
					dec.getX() + dec.getWidth(), dec.getY(), color);
			break;
		case hline:
			GBGraphics.drawLine(g, bigDec.getX(), where.getCenterY(),
					bigDec.getX() + bigDec.getWidth(), where.getCenterY(),
					color);
			break;
		case vline:
			GBGraphics.drawLine(g, where.getCenterX(), bigDec.getY(),
					where.getCenterX(), bigDec.getY() + bigDec.getHeight(),
					color);
			break;
		case slash:
			GBGraphics.drawLine(g, dec.getX(), dec.getY() + dec.getHeight(),
					dec.getX() + dec.getWidth(), dec.getY(), color);
			break;
		case backslash:
			GBGraphics.drawLine(g, dec.getX(), dec.getY(),
					dec.getX() + dec.getWidth(), dec.getY() + dec.getHeight(),
					color);
			break;
		}
	}

	@Override
	protected void DrawOverlay(Graphics g, GBProjection proj, Rectangle where,
			boolean detailed) {
		// shield
		if (hardware.ActualShield() > 0) {
			Rectangle halo = new Rectangle(where);
			halo.grow(2, 2);
			GBGraphics
					.drawOval(
							g,
							halo,
							new GBColor(0.3f, 0.5f, 1)
									.multiply((float) (hardware.ActualShield() / (mass * kStandardShieldPerMass))));
		}
		// radio rings
		for (int age = 0; age < GBRadioState.kRadioHistory; ++age) {
			if (hardware.radio.sent[age] == 0
					&& hardware.radio.writes[age] == 0)
				continue;
			double r = kRingGrowthRate * (age + 1);
			Rectangle ring = new Rectangle(proj.ToScreenX(Position().x - r),
					proj.ToScreenY(Position().y + r),
					proj.ToScreenX(Position().x + r),
					proj.ToScreenY(Position().y - r));
			float intensity = Math.min(2.0f
					* (GBRadioState.kRadioHistory - age)
					/ GBRadioState.kRadioHistory, 1.0f);
			GBGraphics.drawOval(g, ring,
					(hardware.radio.sent[age] != 0 ? new GBColor(0.6f, 0.5f, 1)
							: new GBColor(1, 0.8f, 0.5f)).multiply(intensity));
		}
	}

	@Override
	public void DrawMini(Graphics g, Rectangle where) {
		if (where.getWidth() <= 4)
			GBGraphics.fillRect(g, where, Color());
		else {
			GBGraphics.fillOval(g, where, Owner().Color());
			GBGraphics.fillOval(g, where,
					Owner().Color().Mix(0.5f, type.Color()));
		}
	}

}
