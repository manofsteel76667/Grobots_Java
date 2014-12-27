/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

// GBRobot.cpp
// Grobots (c) 2002-2007 Devon and Warren Schudy
// Distributed under the GNU General Public License.
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import sides.GBRobotDecoration;
import sides.RobotType;
import sides.Side;
import support.FinePoint;
import support.GBColor;
import support.GBMath;
import brains.Brain;
import brains.BrainStatus;

public class GBRobot extends GBObject {
	RobotType type;
	Brain brain;
	int id, parent;
	Side lastHit; // who hit us last
	int recentDamage;
	int friendlyCollisions, enemyCollisions, shotCollisions, foodCollisions,
			wallCollisions;

	// Precalculated colors
	Color minimapColor;
	Color blasterRangeCircleColor;
	Color syphonRangeCircleColor;
	Color grenadesRangeCircleColor;
	Color enemySyphonRangeCircleColor;
	Color forcefieldRangeCircleColor;
	Color energyMeterColor;
	Color energyMeterBackgroundColor;
	Color damageMeterColor;
	Color damageMeterBackgroundColor;
	Color gestationMeterColor;
	Color gestationMeterBackgroundColor;
	Color shieldColor;
	Color baseDecorationColor;

	// Radial paint arrays
	float[] radialSteps;
	Color[] radialColors;
	RadialGradientPaint painter;

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

	public static final double kRingGrowthRate = 0.1;
	public static final double kRadialPaintOffsetFactor = 6;

	public void Recalculate() {
		mass = type.Mass() + hardware.constructor.FetusMass();
		radius = Math.sqrt(mass + kRobotRadiusPadding) * kRobotRadiusFactor;
	}

	public GBRobot(RobotType rtype, FinePoint where) {
		super(where, 0.5, rtype.Mass());
		type = rtype;
		brain = rtype.MakeBrain();
		id = rtype.side.GetNewRobotNumber();
		hardware = new GBHardwareState(rtype.Hardware());
		hardware.radio.Reset(Owner());
		calcColors();
		Recalculate();
	}

	public GBRobot(RobotType rtype, FinePoint where, FinePoint vel, int parentID) {
		super(where, 0.5, vel, rtype.Mass());
		type = rtype;
		brain = rtype.MakeBrain();
		id = rtype.side.GetNewRobotNumber();
		parent = parentID;
		hardware = new GBHardwareState(rtype.Hardware());
		hardware.radio.Reset(Owner());
		calcColors();
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
		String dets = String.format("%.0f energy, %.0f armor", Energy(),
				hardware.armor);
		if (hardware.constructor.Progress() != 0)
			dets += String.format(", %.1f%% %s",
					hardware.constructor.Progress()
							/ hardware.constructor.Type().Cost() * 100,
					hardware.constructor.Type().name);
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
	public double GiveEnergy(double amount) {
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
	public void CollideWith(GBObject other) {
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
	public void Think(GBWorld world) {
		if (brain != null)
			brain.think(this, world);
	}

	@Override
	public void Act(GBWorld world) {
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
	public void CollectStatistics(ScoreKeeper keeper) {
		double bm = Biomass();
		Owner().ReportRobot(bm, type, Position());
		type.ReportRobot(bm);
		keeper.ReportRobot(bm);
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
	public Color Color() {
		return GBColor.Mix(GBColor.EnsureContrastWithBlack(Owner().Color(),
				kMinMinimapBotContrast), 0.9f, type.Color());
		// return
		// Owner().Color().EnsureContrastWithBlack(kMinMinimapBotContrast)
		// .Mix(0.9f, type.Color());
	}

	// Precalculation of colors that won't change over the robot's life
	void calcColors() {
		minimapColor = GBColor.Mix(Color(), 0.5f, type.color);
		blasterRangeCircleColor = new Color(1f, 0f, 1f, .5f);
		grenadesRangeCircleColor = new Color(1f, 1f, 0f, .5f);
		syphonRangeCircleColor = new Color(1f, 1f, 0f, .5f);
		enemySyphonRangeCircleColor = new Color(.6f, 1f, 0f, .5f);
		forcefieldRangeCircleColor = new Color(0f, .8f, 1f, .5f);
		energyMeterColor = Color.GREEN;
		energyMeterBackgroundColor = new Color(0, 0.5f, 1);
		damageMeterColor = Color.red;
		damageMeterBackgroundColor = Color.lightGray;
		gestationMeterColor = Color.yellow;
		gestationMeterBackgroundColor = Color.GREEN;
		baseDecorationColor = type.Decoration() == GBRobotDecoration.none ? Owner()
				.Color() : type.DecorationColor();
		shieldColor = new Color(0.3f, 0.5f, 1);
		/*
		 * Center big enough to hold the decoration, a lighter ring around
		 * center Wide spot of colors that change based on game conditions, then
		 * outer ring same color as base type
		 */
		radialSteps = new float[] { .3f, .35f, .9f, .95f };
		radialColors = new Color[] {
				type.Decoration() == GBRobotDecoration.none ? type.color
						: GBColor.ChooseContrasting(type.decorationColor,
								type.color, Color.black, .1f),
				new Color(1f, 1f, 1f, .8f), Color.white, type.color };
	}

	// Draw a meter with whichever color gives better contrast. If pulse, make
	// the meter flash.
	static void DrawMeter(Graphics g, double fraction, Ellipse2D.Double where,
			int zeroAngle, int oneAngle, int width, Color color1, Color color2,
			Color bgcolor, boolean pulse) {
		Graphics2D g2d = (Graphics2D) g;
		Color color = GBColor.ChooseContrasting(bgcolor, color1, color2,
				kMinMeterContrast);
		int angle = (int) Math.ceil(fraction * (oneAngle - zeroAngle));
		float phase = System.currentTimeMillis() * 6.28f / 500;
		g2d.setColor(GBColor.multiply(color,
				pulse ? 0.85f + 0.15f * (float) Math.sin(phase) : 1.0f));
		Rectangle rect = where.getBounds();
		g2d.drawArc(rect.x, rect.y, rect.width, rect.height, zeroAngle, angle);

	}

	@Override
	public void DrawUnderlay(Graphics g, GBProjection<GBObject> proj,
			boolean detailed) {
		// Get the basics
		Graphics2D g2d = (Graphics2D) g;
		Ellipse2D.Double halo = proj.toScreenEllipse(this);
		if (halo.width <= 5) {
			return;
		}
		halo.getBounds().grow(3, 3);
		// halo: crashes, prints
		if (brain != null)
			if (brain.status != BrainStatus.bsOK) {
				g2d.setPaint(brain.status == BrainStatus.bsStopped ? Color.yellow
						: Color.red);
				g2d.fill(halo);
			}
		// velocity and engine-velocity
		if (detailed && hardware.EnginePower() != 0
				&& hardware.EngineVelocity().isNonzero()) {
			FinePoint dv = hardware.EngineVelocity().subtract(Velocity());
			if (dv.normSquare() > 0.0001) {
				FinePoint head = Position().add(
						dv.unit().multiply(
								Radius() + hardware.EnginePower()
										/ Math.sqrt(Mass()) * 30));
				g2d.setStroke(new BasicStroke(2));
				g2d.setColor(Color.green);
				g2d.drawLine(proj.toScreenX(position.x),
						proj.toScreenY(position.y), proj.toScreenX(head.x),
						proj.toScreenY(head.y));
			}
		}
		DrawShadow(g2d, proj, new FinePoint(0, -.25), GBColor.shadow);
		// weapon ranges? //sensor results?
	}

	void drawRangeCircle(Graphics g, GBProjection<GBObject> proj, double r,
			Color color) {
		if (r <= 0)
			return;
		r += radius;
		g.setColor(color);
		g.drawOval(proj.toScreenX(position.x - r),
				proj.toScreenY(position.y + r),
				(int) (r * 2 * proj.getScale()),
				(int) (r * 2 * proj.getScale()));
	}

	public void drawRangeCircles(Graphics g, GBProjection<GBObject> proj) {
		((Graphics2D) g).setStroke(new BasicStroke(1));
		drawRangeCircle(g, proj, hardware.blaster.MaxRange(),
				blasterRangeCircleColor);
		drawRangeCircle(g, proj, hardware.grenades.MaxRange(),
				grenadesRangeCircleColor);
		if (hardware.syphon.MaxRate() > 0)
			drawRangeCircle(g, proj, hardware.syphon.MaxRange(),
					syphonRangeCircleColor);
		if (hardware.enemySyphon.MaxRate() > 0)
			drawRangeCircle(g, proj, hardware.enemySyphon.MaxRange(),
					enemySyphonRangeCircleColor);
		drawRangeCircle(g, proj, hardware.forceField.MaxRange(),
				forcefieldRangeCircleColor);
	}

	@Override
	public void Draw(Graphics g, GBProjection<GBObject> proj, boolean detailed) {
		Graphics2D g2d = (Graphics2D) g;
		Ellipse2D.Double where = proj.toScreenEllipse(this);
		int scale = proj.getScale();
		if (where.width <= kMaxSquareMiniSize) {
			DrawMini(g, where);
			return;
		}
		int meterWidth = (int) Math.max(1, (where.width + 10) / 10);
		// background and rim
		Color robotColor = GBColor.Mix(Color.red, 0.8f * recentDamage
				/ kRecentDamageTime, Owner().Color());
		radialColors[2] = robotColor;
		FinePoint focus = proj.toScreen(position.add(velocity.multiply(radius
				* kRadialPaintOffsetFactor)));
		painter = new RadialGradientPaint(new Point2D.Double(
				where.getCenterX(), where.getCenterY()),
				(float) (radius * scale), focus, radialSteps, radialColors,
				CycleMethod.NO_CYCLE);
		g2d.setPaint(painter);
		g2d.fill(where);
		// meters
		if (detailed) {
			// energy meter
			if (hardware.MaxEnergy() != 0)
				DrawMeter(g, hardware.Energy() / hardware.MaxEnergy(), where,
						180, 0, meterWidth, energyMeterColor,
						energyMeterBackgroundColor, Owner().Color(),
						hardware.Eaten() != 0
								|| hardware.syphon.Syphoned() != 0
								|| hardware.enemySyphon.Syphoned() != 0);
			// damage meter
			if (hardware.Armor() < hardware.MaxArmor())
				DrawMeter(g, 1.0 - hardware.Armor() / hardware.MaxArmor(),
						where, 360, 180, meterWidth, damageMeterColor,
						damageMeterBackgroundColor, Owner().Color(),
						hardware.RepairRate() != 0);
			// gestation meter
			if (hardware.constructor.Progress() != 0) {
				Ellipse2D.Double meter = new Ellipse2D.Double();
				meter.x = where.x + meterWidth;
				meter.y = where.y + meterWidth;
				meter.width = where.width - meterWidth * 2;
				meter.height = where.height - meterWidth * 2;
				DrawMeter(g, hardware.constructor.Fraction(), meter, 0, 360, 1,
						gestationMeterColor, gestationMeterBackgroundColor,
						Owner().Color(), hardware.constructor.Rate() != 0);
			}
		}
		// decoration appears inside the focus circle or the radial painter
		double decorationWidth = where.width * radialSteps[0];
		double decorationHeight = where.height * radialSteps[0];
		Ellipse2D.Double decorationOval = new Ellipse2D.Double(focus.x
				- decorationWidth / 2, focus.y - decorationHeight / 2,
				decorationWidth, decorationHeight);
		// flash decoration when reloading or sensing
		Color color = baseDecorationColor;
		if (hardware.grenades.Cooldown() != 0)
			color = GBColor.Mix(
					Color.yellow,
					(float) hardware.grenades.Cooldown()
							/ hardware.grenades.ReloadTime(),
					baseDecorationColor);
		else if (hardware.blaster.Cooldown() != 0)
			color = GBColor.Mix(
					Color.magenta,
					(float) hardware.blaster.Cooldown()
							/ hardware.blaster.ReloadTime(),
					baseDecorationColor);
		// Draw decoration
		g2d.setColor(color);
		g2d.setPaint(color);
		int thickness = (int) (15 + decorationOval.width) / 15;
		g2d.setStroke(new BasicStroke(thickness));
		switch (type.Decoration()) {
		case none:
		default:
			if (hardware.blaster.Cooldown() == 0
					&& hardware.grenades.Cooldown() == 0)
				break;
			// if we're flashing, fall through and draw a dot
		case dot:
			g2d.fill(decorationOval);
			break;
		case circle:
			g2d.draw(decorationOval);
			break;
		case square:
			g2d.draw(decorationOval.getBounds());
			break;
		case triangle:
			double bottom = decorationOval.y + .75 * decorationOval.height;
			double bottomHalfX = decorationOval.width / 4 * GBMath.sqrt3;
			double topMiddleX = decorationOval.x + decorationOval.width / 2;
			int[] x = new int[] { (int) (topMiddleX),
					(int) (topMiddleX + bottomHalfX),
					(int) (topMiddleX - bottomHalfX) };
			int[] y = new int[] { (int) decorationOval.y, (int) bottom,
					(int) bottom };
			g2d.drawPolygon(x, y, 3);
			break;
		case cross:
			g2d.drawLine(
					(int) (decorationOval.x + thickness / 2),
					(int) (decorationOval.y + decorationOval.height / 2 + thickness / 2),
					(int) (decorationOval.x + decorationOval.width + thickness / 2),
					(int) (decorationOval.y + decorationOval.height / 2 + thickness / 2));
			g2d.drawLine(
					(int) (decorationOval.x + decorationOval.width / 2 + thickness / 2),
					(int) (decorationOval.y + thickness / 2),
					(int) (decorationOval.x + decorationOval.width / 2 + thickness / 2),
					(int) (decorationOval.y + decorationOval.height + thickness / 2));
			break;
		case x:
			double cornerDist = decorationOval.width * (GBMath.sqrt2 - 1)
					/ (2 * GBMath.sqrt2);
			g2d.drawLine(
					(int) (decorationOval.x + cornerDist + thickness / 2),
					(int) (decorationOval.y + cornerDist + thickness / 2),
					(int) (decorationOval.x + decorationOval.width - cornerDist + thickness / 2),
					(int) (decorationOval.y + decorationOval.height
							- cornerDist + thickness / 2));
			g2d.drawLine((int) (decorationOval.x + decorationOval.width
					- cornerDist + thickness / 2), (int) (decorationOval.y
					+ cornerDist + thickness / 2), (int) (decorationOval.x
					+ cornerDist + thickness / 2), (int) (decorationOval.y
					+ decorationOval.height - cornerDist + thickness / 2));
			break;
		case hline:
			g2d.drawLine((int) decorationOval.x,
					(int) (decorationOval.y + decorationOval.height / 2),
					(int) (decorationOval.x + decorationOval.width),
					(int) (decorationOval.y + decorationOval.height / 2));
			break;
		case vline:
			g2d.drawLine((int) (decorationOval.x + decorationOval.height / 2),
					(int) decorationOval.y,
					(int) (decorationOval.x + decorationOval.height / 2),
					(int) (decorationOval.y + decorationOval.height));
			break;
		case slash:
			cornerDist = decorationOval.width * (GBMath.sqrt2 - 1)
					/ (2 * GBMath.sqrt2);
			g2d.drawLine(
					(int) (decorationOval.x + cornerDist + thickness / 2),
					(int) (decorationOval.y + cornerDist + thickness / 2),
					(int) (decorationOval.x + decorationOval.width - cornerDist + thickness / 2),
					(int) (decorationOval.y + decorationOval.height
							- cornerDist + thickness / 2));
			break;
		case backslash:
			cornerDist = decorationOval.width * (GBMath.sqrt2 - 1)
					/ (2 * GBMath.sqrt2);
			g2d.drawLine((int) (decorationOval.x + decorationOval.width
					- cornerDist + thickness / 2), (int) (decorationOval.y
					+ cornerDist + thickness / 2), (int) (decorationOval.x
					+ cornerDist + thickness / 2), (int) (decorationOval.y
					+ decorationOval.height - cornerDist + thickness / 2));
			break;
		}
	}

	@Override
	public void DrawOverlay(Graphics g, GBProjection<GBObject> proj,
			boolean detailed) {
		Graphics2D g2d = (Graphics2D) g;
		Ellipse2D.Double halo = proj.toScreenEllipse(this);
		int scale = proj.getScale();
		// shield
		if (hardware.ActualShield() > 0) {
			halo.height += scale / 2;
			halo.width += scale / 2;
			halo.x -= scale / 2;
			halo.y -= scale / 4;
			g2d.setColor(GBColor.multiply(
					shieldColor,
					(float) (hardware.ActualShield() / (mass * kStandardShieldPerMass))));
			g2d.setStroke(new BasicStroke(1));
			g2d.draw(halo);
		}
		// radio rings
		for (int age = 0; age < GBRadioState.kRadioHistory; ++age) {
			if (hardware.radio.sent[age] == 0
					&& hardware.radio.writes[age] == 0)
				continue;
			double r = kRingGrowthRate * (age + 1);
			Ellipse2D.Double ring = proj.toScreenEllipse(this);
			ring.x -= scale * r;
			ring.y -= scale * r;
			ring.width += r * 2 * scale;
			ring.height += r * 2 * scale;
			float intensity = Math.min(2.0f
					* (GBRadioState.kRadioHistory - age)
					/ GBRadioState.kRadioHistory, 1.0f);
			g2d.setColor(hardware.radio.sent[age] != 0 ? new Color(0.6f, 0.5f,
					1) : GBColor.multiply(new Color(1, 0.8f, 0.5f), intensity));
			g2d.setStroke(new BasicStroke(1));
			g2d.draw(ring);
		}
	}

	void DrawMini(Graphics g, Ellipse2D where) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setPaint(minimapColor);
		if (where.getWidth() <= 4)
			g2d.fill(where.getBounds());
		else {
			g2d.fill(where);
		}
	}

}
