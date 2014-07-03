package simulation;

// GBRobot.cpp
// Grobots (c) 2002-2007 Devon and Warren Schudy
// Distributed under the GNU General Public License.
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import sides.GBRobotDecoration;
import sides.RobotType;
import sides.Side;
import support.FinePoint;
import support.GBColor;
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
		return type.toString() + " #" + id;
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
		Graphics2D g2d = (Graphics2D) g;
		GBColor color = bgcolor.ChooseContrasting(color1, color2,
				kMinMeterContrast);
		int angle = (int) Math.ceil(fraction * (oneAngle - zeroAngle));
		float phase = System.currentTimeMillis() * 6.28f / 500;
		/*
		 * GBGraphics.drawArc(g, where, zeroAngle + (angle < 0 ? angle : 0),
		 * Math .abs(angle), color .multiply(pulse ? 0.85f + 0.15f * (float)
		 * Math.sin(phase) : 1.0f), width);
		 */
		g2d.setColor(color.multiply(pulse ? 0.85f + 0.15f * (float) Math
				.sin(phase) : 1.0f));
		g2d.drawArc(where.x, where.y, where.width, where.height, zeroAngle,
				angle);

	}

	@Override
	protected void DrawUnderlay(Graphics g, GBProjection proj, Rectangle where,
			boolean detailed) {
		// halo: crashes, prints
		Rectangle halo = new Rectangle(where);
		halo.grow(3, 3);
		/*
		 * if (brain != null) if (brain.status != BrainStatus.bsOK)
		 * GBGraphics.fillOval(g, halo, brain.status == BrainStatus.bsStopped ?
		 * Color.yellow : Color.red);
		 */
		// velocity and engine-velocity
		if (Velocity().isNonzero())
			DrawShadow(g, proj, Velocity().multiply(-2.5),
					hardware.EnginePower() != 0 ? Color.gray : Color.darkGray);
		if (detailed && hardware.EnginePower() != 0
				&& hardware.EngineVelocity().isNonzero()) {
			FinePoint dv = hardware.EngineVelocity().subtract(Velocity());
			if (dv.norm() > 0.01) {
				/*FinePoint head = Position().add(
						dv.unit().multiply(
								Radius() + hardware.EnginePower()
										/ Math.sqrt(Mass()) * 30));
				((Graphics2D) g).setStroke(new BasicStroke(2));
				
				 * GBGraphics.drawLine(g, proj.ToScreenX(Position().x),
				 * proj.ToScreenY(Position().y), proj.ToScreenX(head.x),
				 * proj.ToScreenY(head.y), Color.GREEN);
				 */
				DrawShadow(g, proj, dv.unit().multiply(hardware.EnginePower())
						.divide(Mass()).multiply(-30), Color.GREEN);
			}
		}
		// weapon ranges? //sensor results?
	}

	/**
	 * Moved from GBPortal
	 * 
	 * @param g
	 * @param proj
	 */
	public void drawRangeCircles(Graphics g, GBProjection proj) {
		Point center = new Point(proj.ToScreenX(position.x),
				proj.ToScreenY(position.y));
		Graphics2D g2d = (Graphics2D) g;
		if (hardware.blaster.MaxRange() > 0) {
			g2d.setColor(new GBColor(Color.magenta).multiply(0.5f));
			g2d.drawOval(
					center.x,
					center.y,
					(int) (radius + hardware.blaster.MaxRange())
							* proj.getScale(),
					(int) (radius + hardware.blaster.MaxRange())
							* proj.getScale());
		}
		if (hardware.grenades.MaxRange() > 0) {
			g2d.setColor(new GBColor(Color.yellow).multiply(0.5f));
			g2d.drawOval(
					center.x,
					center.y,
					(int) (radius + hardware.grenades.MaxRange())
							* proj.getScale(),
					(int) (radius + hardware.grenades.MaxRange())
							* proj.getScale());
		}
		if (hardware.syphon.MaxRate() > 0) {
			g2d.setColor(new GBColor(0.25f, 0.4f, 0.5f));
			g2d.drawOval(
					center.x,
					center.y,
					(int) (radius + hardware.syphon.MaxRate())
							* proj.getScale(),
					(int) (radius + hardware.syphon.MaxRate())
							* proj.getScale());
		}
		if (hardware.enemySyphon.MaxRate() > 0) {
			g2d.setColor(new GBColor(0.3f, 0.5f, 0));
			g2d.drawOval(
					center.x,
					center.y,
					(int) (radius + hardware.enemySyphon.MaxRate())
							* proj.getScale(),
					(int) (radius + hardware.enemySyphon.MaxRate())
							* proj.getScale());
		}
		if (hardware.forceField.MaxRange() > 0) {
			g2d.setColor(new GBColor(0, 0.4f, 0.5f));
			g2d.drawOval(
					center.x,
					center.y,
					(int) (radius + hardware.forceField.MaxRange())
							* proj.getScale(),
					(int) (radius + hardware.forceField.MaxRange())
							* proj.getScale());
		}
	}

	@Override
	public void Draw(Graphics g, GBProjection proj, boolean detailed) {
		Graphics2D g2d = (Graphics2D) g;
		Rectangle where = getScreenRect(proj);
		if (where.width <= 5) {
			DrawMini(g, where);
			return;
		}
		int meterWidth = (int) Math.max(1, (where.getWidth() + 10) / 10);
		// background and rim
		GBColor robotColor = (new GBColor(Color.red).Mix(0.8f * recentDamage
				/ kRecentDamageTime, Owner().Color()));
		g2d.setPaint(robotColor);
		g2d.fillOval(where.x, where.y, where.width, where.height);
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
		int thickness = (int) (15 + where.width) / 15; // was 2 for >15
														// else 1
		g2d.setStroke(new BasicStroke(thickness));
		Rectangle dec = new Rectangle(
				(int) (where.getCenterX() - where.getWidth() / 4),
				(int) (where.getCenterY() - where.getHeight() / 4),
				where.width / 2, where.height / 2);
		int dx = (where.width / 4);
		int dy = (where.height / 4);
		// cross, hline, and vline draw in bigDec instead of dec
		Rectangle bigDec = new Rectangle(where.x - dx, where.y - dy,
				where.width - dx * 2, where.height - dy * 2);
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
		g2d.setColor(color);
		g2d.setPaint(color);
		switch (type.Decoration()) {
		case none:
		default:
			if (hardware.blaster.Cooldown() == 0
					&& hardware.grenades.Cooldown() == 0)
				break;
			// if we're flashing, fall through and draw a dot
		case dot:
			g2d.fillOval((int) dec.getCenterX() - thickness,
					(int) dec.getCenterY() - thickness, thickness * 2,
					thickness * 2);
			break;
		case circle:
			g2d.drawOval((int) dec.getCenterX(), (int) dec.getCenterY(),
					dec.width, dec.height);
			break;
		case square:
			g2d.draw(dec);
			break;
		case triangle:
			g2d.drawLine(dec.x, dec.y + dec.height, (int) dec.getCenterX(),
					dec.y);
			g2d.drawLine((int) dec.getCenterX(), dec.y, dec.x + dec.width,
					dec.y + dec.height);
			g2d.drawLine(dec.x, dec.y + dec.height, dec.x + dec.width, dec.y
					+ dec.height);
			break;
		case cross:
			g2d.drawLine(dec.x, (int) dec.getCenterY(), dec.x + dec.width,
					(int) dec.getCenterY());
			g2d.drawLine((int) dec.getCenterX(), dec.y, (int) dec.getCenterX(),
					dec.y + dec.height);
			break;
		case x:
			g2d.drawLine(dec.x, dec.y, dec.x + dec.width, dec.y + dec.height);
			g2d.drawLine(dec.x, dec.y + dec.height, dec.x + dec.width, dec.y);
			break;
		case hline:
			g2d.drawLine(bigDec.x, (int) dec.getCenterY(), bigDec.x
					+ bigDec.width, (int) dec.getCenterY());
			break;
		case vline:
			g2d.drawLine((int) dec.getCenterX(), bigDec.y,
					(int) dec.getCenterX(), bigDec.y + bigDec.height);
			break;
		case slash:
			g2d.drawLine(dec.x, dec.y + dec.height, dec.x + dec.width, dec.y);
			break;
		case backslash:
			g2d.drawLine(dec.x, dec.y, dec.x + dec.width, dec.y + dec.height);
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
			/*
			 * GBGraphics .drawOval( g, halo, new GBColor(0.3f, 0.5f, 1)
			 * .multiply((float) (hardware.ActualShield() / (mass *
			 * kStandardShieldPerMass))));
			 */
		}
		// radio rings
		for (int age = 0; age < GBRadioState.kRadioHistory; ++age) {
			if (hardware.radio.sent[age] == 0
					&& hardware.radio.writes[age] == 0)
				continue;
			/*double r = kRingGrowthRate * (age + 1);
			/*Rectangle ring = new Rectangle(proj.ToScreenX(Position().x - r),
					proj.ToScreenY(Position().y + r),
					proj.ToScreenX(Position().x + r),
					proj.ToScreenY(Position().y - r));
			float intensity = Math.min(2.0f
					* (GBRadioState.kRadioHistory - age)
					/ GBRadioState.kRadioHistory, 1.0f);
			/*
			 * GBGraphics.drawOval(g, ring, (hardware.radio.sent[age] != 0 ? new
			 * GBColor(0.6f, 0.5f, 1) : new GBColor(1, 0.8f,
			 * 0.5f)).multiply(intensity));
			 */
		}
	}

	@Override
	public void DrawMini(Graphics g, Rectangle where) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setPaint(Color());
		if (where.getWidth() <= 4)
			g2d.fill(where);
		else {
			g2d.fillOval(where.x + where.width / 2, where.y + where.height / 2,
					where.height, where.width);
			// g2d.fillOval(where,
			// Owner().Color().Mix(0.5f, type.Color()));
		}
	}

}
