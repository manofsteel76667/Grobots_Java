/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import sides.GBRobotDecoration;
import sides.RobotType;
import sides.Side;
import support.FinePoint;
import support.GBColor;
import support.GBMath;
import Rendering.GBProjection;
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

	// Minimap and tiny picture
	BufferedImage miniImage;
	BufferedImage smallImage;
	public BufferedImage fullImage;

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
	public static final double kRadialPaintOffsetFactor = 3;

	public void recalculate() {
		mass = type.getMass() + hardware.constructor.getFetusMass();
		radius = Math.sqrt(mass + kRobotRadiusPadding) * kRobotRadiusFactor;
	}

	public GBRobot(RobotType rtype, FinePoint where) {
		super(where, 0.5, rtype.getMass());
		type = rtype;
		brain = rtype.makeBrain();
		id = rtype.side.getNewRobotNumber();
		hardware = new GBHardwareState(rtype.getHardware());
		hardware.radio.reset(getOwner());
		calcColors();
		recalculate();
		makeImages();
	}

	public GBRobot(RobotType rtype, FinePoint where, FinePoint vel, int parentID) {
		super(where, 0.5, vel, rtype.getMass());
		type = rtype;
		brain = rtype.makeBrain();
		id = rtype.side.getNewRobotNumber();
		parent = parentID;
		hardware = new GBHardwareState(rtype.getHardware());
		hardware.radio.reset(getOwner());
		calcColors();
		recalculate();
		makeImages();
	}

	void makeImages() {
		miniImage = new BufferedImage(5, 5, BufferedImage.TYPE_INT_ARGB);
		smallImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D) miniImage.getGraphics();
		g2d.setPaint(minimapColor);
		g2d.fillRect(0, 0, 5, 5);
		g2d = (Graphics2D) smallImage.getGraphics();
		g2d.setPaint(this.getOwner().getColor());
		g2d.fillOval(0, 0, 10, 10);
		g2d.setColor(type.getColor());
		g2d.drawOval(0, 0, 10, 10);
	}

	public RobotType getRobotType() {
		return type;
	}

	public int getID() {
		return id;
	}

	public int getParentID() {
		return parent;
	}

	@Override
	public String toString() {
		return type.toString() + " #" + id;
	}

	@Override
	public String getDetails() {
		String dets = String.format("%.0f energy, %.0f armor", getEnergy(),
				hardware.armor);
		if (hardware.constructor.getProgress() != 0)
			dets += String.format(", %.1f%% %s",
					hardware.constructor.getProgress()
							/ hardware.constructor.getRobotType().getCost() * 100,
					hardware.constructor.getRobotType().name);
		return dets;
	}

	public int getCollisions() {
		return friendlyCollisions + enemyCollisions + wallCollisions;
	}

	public int getFriendlyCollisions() {
		return friendlyCollisions;
	}

	public int getEnemyCollisions() {
		return enemyCollisions;
	}

	public int getFoodCollisions() {
		return foodCollisions;
	}

	public int getShotCollisions() {
		return shotCollisions;
	}

	public int getWallCollisions() {
		return wallCollisions;
	}

	public Side getLastHit() {
		return lastHit;
	}

	public Brain getBrain() {
		return brain;
	}

	public double getShieldFraction() {
		if (hardware.getActualShield() != 0)
			// return 1 / (square(hardware.ActualShield() * kShieldEffectiveness
			// / mass) + 1);
			return 1 / (Math.pow(hardware.getActualShield() * kShieldEffectiveness
					/ mass, 2) + 1);
		return 1;
	}

	@Override
	public void takeDamage(double amount, Side origin) {
		double actual = amount * type.getMassiveDamageMultiplier(mass)
				* getShieldFraction();
		hardware.takeDamage(actual);
		lastHit = origin;
		recentDamage = kRecentDamageTime;
		if (origin == getOwner())
			getOwner().getScores().reportFriendlyFire(actual);
		else {
			getOwner().getScores().reportDamageTaken(actual);
			if (origin != null)
				origin.getScores().reportDamageDone(actual);
		}
	}

	@Override
	public double takeEnergy(double amount) {
		return hardware.useEnergyUpTo(amount);
	}

	@Override
	public double giveEnergy(double amount) {
		return hardware.giveEnergy(amount);
	}

	@Override
	public double maxTakeEnergy() {
		return hardware.getEnergy();
	}

	@Override
	public double maxGiveEnergy() {
		return hardware.getMaxEnergy() - hardware.getEnergy();
	}

	public void doEngineSeek(FinePoint pos, FinePoint vel) {
		FinePoint delta = pos.subtract(getPosition());
		if (vel.isZero()
				&& (delta.add(getVelocity().multiply(11))).norm() < radius)
			hardware.setEnginePower(0);
		else {
			hardware.setEnginePower(hardware.getEngineMaxPower());
			hardware.setEngineVelocity(vel.add(delta.multiply(0.09))); // FIXME:
																		// better
																		// seek
		}
	}

	public void die(Side killer) {
		dead = true;
		lastHit = killer;
	}

	@Override
	public void move() {
		friendlyCollisions = 0;
		enemyCollisions = 0;
		foodCollisions = 0;
		shotCollisions = 0;
		wallCollisions = 0;
		super.move();
		drag(kFriction, kLinearDragFactor, kQuadraticDragFactor);
	}

	@Override
	public void collideWithWall() {
		wallCollisions++;
	}

	@Override
	public void collideWith(GBObject other) {
		switch (other.getObjectClass()) {
		case ocRobot:
			if (other.getOwner() == getOwner())
				friendlyCollisions++;
			else
				enemyCollisions++;
			break;
		case ocFood: {
			foodCollisions++;
			// FIXME shields don't affect eaters
			double eaten = other.takeEnergy(hardware.getEaterLimit());
			hardware.eat(eaten);
			Side source = other.getOwner();
			if (source == null)
				getOwner().reportTheotrophy(eaten);
			else if (source == getOwner())
				getOwner().reportCannibalism(eaten);
			else
				getOwner().reportHeterotrophy(eaten);
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
	public void think(GBWorld world) {
		if (brain != null)
			brain.think(this, world);
	}

	@Override
	public void act(GBWorld world) {
		hardware.act(this, world);
		if (dead) {
			if (lastHit == null)
				; // no reports for accidents
			else if (lastHit == getOwner())
				lastHit.reportSuicide(getBiomass());
			else
				lastHit.reportKilled(getBiomass());
			getOwner().reportDead(getBiomass());
		}
		if (recentDamage != 0)
			--recentDamage;
		recalculate();
	}

	@Override
	public void collectStatistics(ScoreKeeper keeper) {
		double bm = getBiomass();
		getOwner().reportRobot(bm, type, getPosition());
		type.reportRobot(bm);
		keeper.reportRobot(bm);
	}

	@Override
	public GBObjectClass getObjectClass() {
		if (dead)
			return GBObjectClass.ocDead;
		else
			return GBObjectClass.ocRobot;
	}

	@Override
	public Side getOwner() {
		return type.side;
	}

	@Override
	public double getEnergy() {
		return hardware.getEnergy();
	}

	public double getBiomass() {
		return type.getCost() - type.getHardware().InitialEnergy()
				+ hardware.getEnergy() + hardware.constructor.getProgress();
	}

	@Override
	public double getInterest() {
		double interest = getBiomass() * (0.001 + getSpeed() * 0.01)
				+ hardware.getActualShield() / 2;
		if (hardware.blaster.getCooldown() != 0)
			interest += hardware.blaster.getDamage() * 10
					/ hardware.blaster.getReloadTime();
		if (hardware.grenades.getCooldown() != 0)
			interest += hardware.grenades.getDamage() * 10
					/ hardware.grenades.getReloadTime();
		if (hardware.blaster.getCooldown() != 0)
			interest += Math.abs(hardware.forceField.getPower()) * 15 + 1;
		if (hardware.syphon.getRate() != 0)
			interest += Math.abs(hardware.syphon.getRate()) + 1;
		if (hardware.enemySyphon.getRate() != 0)
			interest += Math.abs(hardware.enemySyphon.getRate()) * 5 + 2;
		return interest;
	}

	// This color is only used for the minimap, so it has built-in contrast
	// handling.
	@Override
	public Color getColor() {
		return GBColor.Mix(GBColor.EnsureContrastWithBlack(getOwner().getColor(),
				kMinMinimapBotContrast), 0.9f, type.getColor());
		// return
		// Owner().Color().EnsureContrastWithBlack(kMinMinimapBotContrast)
		// .Mix(0.9f, type.Color());
	}

	// Precalculation of colors that won't change over the robot's life
	void calcColors() {
		minimapColor = GBColor.Mix(getColor(), 0.5f, type.color);
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
		baseDecorationColor = type.getDecoration() == GBRobotDecoration.none ? getOwner()
				.getColor() : /*GBColor.ChooseContrasting(type.decorationColor,
				Owner().Color(), Color.white, .7f);*/
					type.decorationColor;
		shieldColor = new Color(0.3f, 0.5f, 1);
		/*
		 * Center big enough to hold the decoration, a lighter ring around
		 * center, Wide spot of colors that change based on game conditions
		 * but are based on the side color, then
		 * outer ring same color as bot type
		 */
		radialSteps = new float[] { .2f, .25f, .7f, .95f };
		radialColors = new Color[] { baseDecorationColor,
				new Color(1f, 1f, 1f, .8f), getOwner().getColor(), type.color };
	}

	// Draw a meter with whichever color gives better contrast. If pulse, make
	// the meter flash.
	void drawMeter(BufferedImage img, double fraction, int inset, int zeroAngle,
			int oneAngle, int width, Color color1, Color color2, Color bgcolor,
			boolean pulse) {
		Graphics2D g2d = (Graphics2D)img.getGraphics();
		g2d.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_MITER));
		Color color = GBColor.ChooseContrasting(bgcolor, color1, color2,
				kMinMeterContrast);
		int angle = (int) Math.ceil(fraction * (oneAngle - zeroAngle));
		float phase = System.currentTimeMillis() * 6.28f / 500;
		g2d.setColor(GBColor.multiply(color,
				pulse ? 0.85f + 0.15f * (float) Math.sin(phase) : 1.0f));
		g2d.drawArc(inset, inset, img.getWidth() - 2 * inset,
				img.getWidth() - 2 * inset, zeroAngle, angle);
	}

	@Override
	public void drawUnderlay(Graphics g, GBProjection proj,
			boolean detailed) {
		// Get the basics
		Graphics2D g2d = (Graphics2D) g;
		Ellipse2D.Double halo = getScreenEllipse(proj);
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
		if (detailed && hardware.getEnginePower() != 0
				&& hardware.getEngineVelocity().isNonzero()) {
			FinePoint dv = hardware.getEngineVelocity().subtract(getVelocity());
			if (dv.normSquare() > 0.0001) {
				FinePoint head = getPosition().add(
						dv.unit().multiply(
								getRadius() + hardware.getEnginePower()
										/ Math.sqrt(getMass()) * 30));
				g2d.setStroke(new BasicStroke(2));
				g2d.setColor(Color.green);
				g2d.drawLine(proj.toScreenX(position.x),
						proj.toScreenY(position.y), proj.toScreenX(head.x),
						proj.toScreenY(head.y));
			}
		}
		drawShadow(g2d, proj, new FinePoint(0, -.25), GBColor.shadow);
		// weapon ranges? //sensor results?
	}

	void drawRangeCircle(Graphics g, GBProjection proj, double r,
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

	public void drawRangeCircles(Graphics g, GBProjection proj) {
		((Graphics2D) g).setStroke(new BasicStroke(1));
		drawRangeCircle(g, proj, hardware.blaster.getMaxRange(),
				blasterRangeCircleColor);
		drawRangeCircle(g, proj, hardware.grenades.getMaxRange(),
				grenadesRangeCircleColor);
		if (hardware.syphon.getMaxRate() > 0)
			drawRangeCircle(g, proj, hardware.syphon.getMaxRange(),
					syphonRangeCircleColor);
		if (hardware.enemySyphon.getMaxRate() > 0)
			drawRangeCircle(g, proj, hardware.enemySyphon.getMaxRange(),
					enemySyphonRangeCircleColor);
		drawRangeCircle(g, proj, hardware.forceField.getMaxRange(),
				forcefieldRangeCircleColor);
	}
	
	/**
	 * Draws the robot to scale in the provided BufferedImage
	 * @param imageIn
	 * @param scale
	 * @param detailed
	 */
	public BufferedImage getScaledDrawing(int scale, boolean detailed ) {
		int size = (int) (Math.max(Math.round(radius * 2 * scale), 1));
		BufferedImage imageIn = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		double centerPoint = size / 2;
		int meterWidth = Math.max(1, size / 20);
		Graphics2D g2d = (Graphics2D) imageIn.getGraphics();
		// background and rim
		// flash decoration when reloading or sensing
		Color color = baseDecorationColor;
		if (hardware.grenades.getCooldown() != 0)
			color = GBColor.Mix(Color.yellow,
					(float) hardware.grenades.getCooldown()
							/ hardware.grenades.getReloadTime(),
					baseDecorationColor);
		else if (hardware.blaster.getCooldown() != 0)
			color = GBColor.Mix(
					Color.magenta,
					(float) hardware.blaster.getCooldown()
							/ hardware.blaster.getReloadTime(),
					baseDecorationColor);
		radialColors[0] = color;
		radialColors[2] = GBColor.Mix(Color.red, 0.8f * recentDamage
				/ kRecentDamageTime, getOwner().getColor());
		FinePoint speedOffset = velocity.multiply(size / radius
				* kRadialPaintOffsetFactor);
		FinePoint focus = new FinePoint(centerPoint + speedOffset.x,
				centerPoint - speedOffset.y);
		painter = new RadialGradientPaint(new Point2D.Double(centerPoint,
				centerPoint), size / 2, focus, radialSteps, radialColors,
				CycleMethod.NO_CYCLE);
		g2d.setPaint(painter);
		g2d.fillOval(0, 0, size, size);
		// meters
		if (detailed) {
			// energy meter
			if (hardware.getMaxEnergy() != 0)
				drawMeter(imageIn, hardware.getEnergy() / hardware.getMaxEnergy(),
						meterWidth, 180, 0, meterWidth, energyMeterColor,
						energyMeterBackgroundColor, getOwner().getColor(),
						hardware.getEaten() != 0
								|| hardware.syphon.getSyphoned() != 0
								|| hardware.enemySyphon.getSyphoned() != 0);
			// damage meter
			if (hardware.getArmor() < hardware.getMaxArmor())
				drawMeter(imageIn,
						1.0 - hardware.getArmor() / hardware.getMaxArmor(),
						meterWidth, 360, 180, meterWidth, damageMeterColor,
						damageMeterBackgroundColor, getOwner().getColor(),
						hardware.getRepairRate() != 0);
			// gestation meter
			if (hardware.constructor.getProgress() != 0) {
				drawMeter(imageIn, hardware.constructor.getFraction(),
						meterWidth * 2, 0, 360, meterWidth,
						gestationMeterColor, gestationMeterBackgroundColor,
						getOwner().getColor(), hardware.constructor.getRate() != 0);
			}
		}
		// decoration appears inside the focus circle or the radial painter
		double decorationWidth = size * radialSteps[0];
		double decorationHeight = size * radialSteps[0];
		Ellipse2D.Double decorationOval = new Ellipse2D.Double(centerPoint
				- decorationWidth / 2, centerPoint - decorationHeight / 2,
				decorationWidth, decorationHeight);
		double r1 = radius * scale / GBMath.sqrt2;
		double cornerStart = centerPoint - r1;
		Rectangle2D.Double decorationSquare = new Rectangle2D.Double(cornerStart, cornerStart, r1*2, r1*2);
		// Draw decoration
		g2d.setColor(color);
		g2d.setPaint(color);
		int thickness = (int) (15 + decorationOval.width) / 15;
		g2d.setStroke(new BasicStroke(thickness));
		switch (type.getDecoration()) {
		case none:
		default:
			//if (hardware.blaster.Cooldown() == 0
			//		&& hardware.grenades.Cooldown() == 0)
				break;
			// if we're flashing, fall through and draw a dot
		case dot:
			g2d.fill(decorationOval);
			break;
		case circle:
			g2d.draw(decorationOval);
			break;
		case square:
			g2d.draw(decorationSquare.getBounds());
			break;
		case triangle:
			double bottom = decorationSquare.y + .75 * decorationSquare.height;
			double bottomHalfX = decorationSquare.width / 4 * GBMath.sqrt3;
			double topMiddleX = decorationSquare.x + decorationSquare.width / 2;
			int[] x = new int[] { (int) (topMiddleX),
					(int) (topMiddleX + bottomHalfX),
					(int) (topMiddleX - bottomHalfX) };
			int[] y = new int[] { (int) decorationSquare.y, (int) bottom,
					(int) bottom };
			g2d.drawPolygon(x, y, 3);
			break;
		case cross:
			g2d.drawLine(
					(int) (decorationSquare.x + thickness / 2),
					(int) (decorationSquare.y + decorationSquare.height / 2 + thickness / 2),
					(int) (decorationSquare.x + decorationSquare.width + thickness / 2),
					(int) (decorationSquare.y + decorationSquare.height / 2 + thickness / 2));
			g2d.drawLine(
					(int) (decorationSquare.x + decorationSquare.width / 2 + thickness / 2),
					(int) (decorationSquare.y + thickness / 2),
					(int) (decorationSquare.x + decorationSquare.width / 2 + thickness / 2),
					(int) (decorationSquare.y + decorationSquare.height + thickness / 2));
			break;
		case x:
			double cornerDist = decorationSquare.width * (GBMath.sqrt2 - 1)
					/ (2 * GBMath.sqrt2);
			g2d.drawLine(
					(int) (decorationSquare.x + cornerDist + thickness / 2),
					(int) (decorationSquare.y + cornerDist + thickness / 2),
					(int) (decorationSquare.x + decorationSquare.width
							- cornerDist + thickness / 2),
					(int) (decorationSquare.y + decorationSquare.height
							- cornerDist + thickness / 2));
			g2d.drawLine((int) (decorationSquare.x + decorationSquare.width
					- cornerDist + thickness / 2), (int) (decorationSquare.y
					+ cornerDist + thickness / 2), (int) (decorationSquare.x
					+ cornerDist + thickness / 2), (int) (decorationSquare.y
					+ decorationSquare.height - cornerDist + thickness / 2));
			break;
		case hline:
			g2d.drawLine((int) decorationSquare.x,
					(int) (decorationSquare.y + decorationSquare.height / 2),
					(int) (decorationSquare.x + decorationSquare.width),
					(int) (decorationSquare.y + decorationSquare.height / 2));
			break;
		case vline:
			g2d.drawLine(
					(int) (decorationSquare.x + decorationSquare.height / 2),
					(int) decorationSquare.y,
					(int) (decorationSquare.x + decorationSquare.height / 2),
					(int) (decorationSquare.y + decorationSquare.height));
			break;
		case slash:
			cornerDist = decorationSquare.width * (GBMath.sqrt2 - 1)
					/ (2 * GBMath.sqrt2);
			g2d.drawLine(
					(int) (decorationSquare.x + cornerDist + thickness / 2),
					(int) (decorationSquare.y + cornerDist + thickness / 2),
					(int) (decorationSquare.x + decorationSquare.width
							- cornerDist + thickness / 2),
					(int) (decorationSquare.y + decorationSquare.height
							- cornerDist + thickness / 2));
			break;
		case backslash:
			cornerDist = decorationSquare.width * (GBMath.sqrt2 - 1)
					/ (2 * GBMath.sqrt2);
			g2d.drawLine((int) (decorationSquare.x + decorationSquare.width
					- cornerDist + thickness / 2), (int) (decorationSquare.y
					+ cornerDist + thickness / 2), (int) (decorationSquare.x
					+ cornerDist + thickness / 2), (int) (decorationSquare.y
					+ decorationSquare.height - cornerDist + thickness / 2));
			break;
		}
		g2d.dispose();
		return imageIn;
	}

	@Override
	public void draw(Graphics g, GBProjection proj, boolean detailed) {
		int size = (int) (Math.max(Math.round(radius * 2 * proj.getScale()), 1));
		if (size <= 5)
			image = miniImage;
		else if (size <= 10)
			image = smallImage;
		else {			
			image = getScaledDrawing(proj.getScale(), detailed);
		}
		drawImage(g, proj);
	}

	@Override
	public void drawOverlay(Graphics g, GBProjection proj,
			boolean detailed) {
		Graphics2D g2d = (Graphics2D) g;
		Ellipse2D.Double halo = getScreenEllipse(proj);
		int scale = proj.getScale();
		// shield
		if (hardware.getActualShield() > 0) {
			halo.height += scale / 2;
			halo.width += scale / 2;
			halo.x -= scale / 2;
			halo.y -= scale / 4;
			g2d.setColor(GBColor.multiply(
					shieldColor,
					(float) (hardware.getActualShield() / (mass * kStandardShieldPerMass))));
			g2d.setStroke(new BasicStroke(1));
			g2d.draw(halo);
		}
		// radio rings
		for (int age = 0; age < GBRadioState.kRadioHistory; ++age) {
			if (hardware.radio.sent[age] == 0
					&& hardware.radio.writes[age] == 0)
				continue;
			double r = kRingGrowthRate * (age + 1);
			Ellipse2D.Double ring = getScreenEllipse(proj);
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
	
	/**
	 * Used prior to generating the type icon so the center color will not show to be
	 * reloading.
	 */
	public void setReloaded() {
		hardware.grenades.setReloaded();
		hardware.blaster.setReloaded();
	}
}
