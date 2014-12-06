package views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
import javax.swing.JPanel;
import javax.swing.JToolBar;

import simulation.GBHardwareState;
import simulation.GBObject;
import simulation.GBObjectClass;
import simulation.GBRobot;
import support.GBColor;
import support.StringUtilities;
import brains.Brain;
import brains.BrainStatus;
import brains.GBStackBrain;

public class Debugger extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7388724419204956062L;
	public static final int kStatusBoxHeight = 30;
	public static final int kPCBoxHeight = 95;
	public static final int kStackBoxHeight = 165;
	public static final int kPrintBoxHeight = 15;
	public static final int kColumnWidth = 200;
	// public static final int kHardwareBoxHeight = 300;
	// public static final int kProfileBoxWidth = 110;
	public static final int kProfileBoxHeight = 25;// GBWORLD_PROFILING ? 87 :
													// 25;
	public static final int kEdgeSpace = 4;
	GBRobot target;

	JToolBar toolbar;

	public Debugger(JToolBar bar) {
		toolbar = bar;
		add(toolbar);
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		super.paintComponent(g);
		Draw(g2d);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(getPreferredWidth(), getPreferredHeight());
	}

	void DrawBox(Graphics2D g, Rectangle box) {
		g.setPaint(Color.white);
		g.fill(box);
		g.setColor(Color.black);
		g.draw(box);
	}

	void DrawStatusBox(Graphics2D g, Rectangle box) {
		DrawBox(g, box);
		StringUtilities.drawStringLeft(g, target.toString(), box.x + 4,
				box.y + 13, 12, Color.black);
		Brain brain = target.Brain();
		GBStackBrain sbrain = (GBStackBrain) brain;
		StringUtilities.drawStringLeft(g,
				brain != null ? (sbrain != null ? "Stack brain."
						: "Unknown brain.") : "No brain.", box.x + 5, box.y
						+ box.height - 4, 10, Color.black);
		if (brain != null)
			StringUtilities
					.drawStringPair(
							g,
							"Status:",
							brain.status.name,
							(box.x * 2 + box.x + box.width + 6) / 3,
							(box.x + box.x + box.width * 2 - 6) / 3,
							box.y + box.height - 4,
							10,
							brain.status == BrainStatus.bsOK ? GBColor.darkGreen
									: brain.status == BrainStatus.bsError ? GBColor.darkRed
											: Color.black, false);
		if (sbrain != null)
			StringUtilities.drawStringPair(g, "Remaining:",
					Integer.toString(sbrain.Remaining()), (box.x + box.x
							+ box.width * 2 + 6) / 3, box.x + box.width - 5,
					box.y + box.height - 4, 10, Color.black, false);
	}

	void DrawPCBox(Graphics2D g, Rectangle box, GBStackBrain brain) {
		DrawBox(g, box);
		int pc = brain.PC();
		StringUtilities.drawStringPair(g, "PC:", brain.AddressLastLabel(pc)
				+ " (line " + Integer.toString(brain.PCLine()) + ')',
				box.x + 3, box.x + box.width - 3, box.y + 11, 10, Color.black,
				true);
		for (int i = -4; i <= 3; i++)
			if (brain.ValidAddress(pc + i))
				StringUtilities.drawStringPair(g,
						brain.AddressName(pc + i) + ':',
						brain.DisassembleAddress(pc + i), box.x + 3, box.x
								+ box.width - 3, box.y + box.height - 34 + 10
								* i, 10, i == 0 ? Color.blue : Color.black,
						false);
	}

	void DrawStackBox(Graphics2D g, Rectangle box, GBStackBrain brain) {
		DrawBox(g, box);
		StringUtilities.drawStringLeft(g, "Stack:", box.x + 3, box.y + 11, 10,
				Color.black, true);
		int height = brain.StackHeight();
		if (height != 0) {
			for (int i = 1; i < 5 && i <= height; i++)
				StringUtilities.drawStringPair(g,
						Integer.toString(height - i) + ':',
						String.format("%.6f", brain.StackAt(height - i)),
						box.x + 3, box.x + box.width - 3, box.y + box.height
								- 44 + 10 * i, 10, Color.black, false);
		} else
			StringUtilities.drawStringRight(g, "empty", box.x + box.width - 3,
					box.y + 31, 10, Color.black, false);
	}

	void DrawReturnStackBox(Graphics2D g, Rectangle box, GBStackBrain brain) {
		DrawBox(g, box);
		StringUtilities.drawStringLeft(g, "Return stack:", box.x + 3,
				box.y + 11, 10, Color.black, true);
		int height = brain.ReturnStackHeight();
		if (height != 0) {
			for (int i = 0; i < 5 && i < height; i++)
				StringUtilities.drawStringPair(
						g,
						Integer.toString(height - i) + ':',
						brain.AddressLastLabel((brain.ReturnStackAt(height - i
								- 1))), box.x + 3, box.x + box.width - 3, box.y
								+ box.height - 44 + 10 * i, 10, Color.black,
						false);
		} else
			StringUtilities.drawStringRight(g, "empty", box.x + box.width - 3,
					box.y + 31, 10, Color.black, false);
	}

	void DrawVariablesBox(Graphics2D g, Rectangle box, GBStackBrain brain) {
		int vars = brain.NumVariables();
		int vvars = brain.NumVectorVariables();
		if (vars != 0 || vvars != 0) {
			DrawBox(g, box);
			int y = box.y + 11;
			StringUtilities.drawStringLeft(g, "Variables:", box.x + 3, y, 10,
					Color.black, true);
			int i;
			for (i = 0; i < vars; i++) {
				y += 10;
				StringUtilities.drawStringPair(g, brain.VariableName(i),
						Double.toString(brain.ReadVariable(i)), box.x + 3,
						box.x + box.width - 3, y, 10, Color.black, false);
			}
			for (i = 0; i < vvars; i++) {
				y += 10;
				StringUtilities.drawStringPair(g, brain.VectorVariableName(i),
						brain.ReadVectorVariable(i).toString(), box.x + 3,
						box.x + box.width - 3, y, 10, Color.black, false);
			}
		} else
			StringUtilities.drawStringLeft(g, "No variables", box.x + 3,
					box.y + 11, 10, Color.black, false);
	}

	void DrawPrintBox(Graphics2D g, Rectangle box, GBStackBrain brain) {
		String print = brain.LastPrint();
		if (print != "none")
			DrawBox(g, box);
		StringUtilities.drawStringPair(g, "Last print:", print, box.x + 3,
				box.x + box.width - 3, box.y + 11, 10, Color.black, false);
	}

	void DrawHardwareBox(Graphics2D g, Rectangle box) {
		GBHardwareState hw = target.hardware;
		int right = box.x + box.width - 3;
		int left = box.x + 3;
		DrawBox(g, box);
		StringUtilities.drawStringPair(g, "Mass:",
				String.format("%.1f", target.Mass()), left, right, box.y + 11,
				10, Color.black, false);
		StringUtilities.drawStringPair(g, "Position:", target.Position()
				.toString(1), left, right, box.y + 21, 10, Color.black, false);
		StringUtilities.drawStringPair(g, "Velocity:", target.Velocity()
				.toString(2), left, right, box.y + 31, 10, Color.black, false);
		StringUtilities.drawStringPair(g, "Speed:",
				String.format("%.2f", target.Speed()), left, right, box.y + 41,
				10, Color.black, false);
		if (hw.EnginePower() != 0)
			StringUtilities.drawStringPair(g, "Engine vel:", hw
					.EngineVelocity().toString(2), left, right, box.y + 51, 10,
					Color.black, false);
		StringUtilities.drawStringPair(g, "Energy:",
				String.format("%.1f", hw.Energy()), left, right, box.y + 65,
				10, GBColor.darkGreen, false);
		StringUtilities.drawStringPair(g, "Eaten:",
				String.format("%.1f", hw.Eaten()), left, right, box.y + 75, 10,
				GBColor.darkGreen, false);
		StringUtilities.drawStringPair(
				g,
				"Armor:",
				String.format("%.0f", hw.Armor()) + '/'
						+ String.format("%.0f", hw.MaxArmor()), left, right,
				box.y + 85, 10, Color.black, false);
		if (hw.ActualShield() != 0)
			StringUtilities.drawStringPair(
					g,
					"Shield:",
					String.format("%.0f", hw.ActualShield())
							+ " ("
							+ StringUtilities.toPercentString(
									target.ShieldFraction(), 0) + ')', left,
					right, box.y + 95, 10, Color.blue, false);
		if (hw.constructor.Type() != null) {
			StringUtilities.drawStringLeft(g, "Constructor", left, box.y + 121,
					10, Color.black, true);
			StringUtilities.drawStringPair(g, "type:",
					hw.constructor.Type().name, left, right, box.y + 131, 10,
					GBColor.ContrastingTextColor(hw.constructor.Type().Color())
					/* hw.constructor.Type().Color().ContrastingTextColor() */,
					false);
			StringUtilities.drawStringPair(
					g,
					"progress:",
					String.format("%.0f", hw.constructor.Progress())
							+ '/'
							+ String.format("%.0f", hw.constructor.Type()
									.Cost()), left, right, box.y + 141, 10,
					Color.black, false);
		}
		// sensor times? result details?
		if (hw.sensor1.Radius() != 0)
			StringUtilities.drawStringPair(g, "robot-found:",
					Integer.toString(hw.sensor1.NumResults()), left, right,
					box.y + 161, 10, Color.black, false);
		if (hw.sensor2.Radius() != 0)
			StringUtilities.drawStringPair(g, "food-found:",
					Integer.toString(hw.sensor2.NumResults()), left, right,
					box.y + 171, 10, Color.black, false);
		if (hw.sensor3.Radius() != 0)
			StringUtilities.drawStringPair(g, "shot-found:",
					Integer.toString(hw.sensor3.NumResults()), left, right,
					box.y + 181, 10, Color.black, false);
		StringUtilities.drawStringLeft(g, "Weapons:", left, box.y + 191, 10,
				Color.black, true);
		if (hw.blaster.Damage() != 0)
			StringUtilities.drawStringPair(g, "blaster-cooldown:",
					Integer.toString(hw.blaster.Cooldown()), left, right,
					box.y + 201, 10, Color.black, false);
		if (hw.grenades.Damage() != 0)
			StringUtilities.drawStringPair(g, "grenades-cooldown:",
					Integer.toString(hw.grenades.Cooldown()), left, right,
					box.y + 211, 10, Color.black, false);
		if (hw.forceField.MaxPower() != 0)
			StringUtilities.drawStringPair(g, "force-field-angle:",
					String.format("%.2f", hw.forceField.Angle()), left, right,
					box.y + 221, 10, Color.black, false);
		if (hw.syphon.MaxRate() != 0)
			StringUtilities.drawStringPair(
					g,
					"syphoned:",
					String.format("%.2f", hw.syphon.Syphoned()) + '/'
							+ String.format("%.2f", hw.syphon.Rate()), left,
					right, box.y + 241, 10, Color.black, false);
		if (hw.enemySyphon.MaxRate() != 0)
			StringUtilities.drawStringPair(g, "enemy-syphoned:",
					String.format("%.2f", hw.enemySyphon.Syphoned()) + '/'
							+ String.format("%.2f", hw.enemySyphon.Rate()),
					left, right, box.y + 251, 10, Color.black, false);
		if (target.flag != 0)
			StringUtilities.drawStringPair(g, "flag:",
					String.format("%.2f", target.flag), left, right,
					box.y + 271, 10, Color.black, false);
	}

	void DrawProfileBox(Graphics2D g, Rectangle box) {
		/*
		 * #if GBWORLD_PROFILING DrawBox(g, box);
		 * StringUtilities.drawStringPair(g, g, "Total time:",
		 * world.TotalTime(), box.x + 5, box.x + box.width - 5, box.y + 13, 10);
		 * StringUtilities.drawStringPair(g, g, "Simulation:",
		 * world.SimulationTime(), box.x + 5, box.x + box.width - 5, box.y + 23,
		 * 10); StringUtilities.drawStringPair(g, g, "Move:", world.MoveTime(),
		 * box.x + 5, box.x + box.width - 5, box.y + 33, 10);
		 * StringUtilities.drawStringPair(g, g, "Act:", world.ActTime(), box.x +
		 * 5, box.x + box.width - 5, box.y + 43, 10);
		 * StringUtilities.drawStringPair(g, g, "Collide:", world.CollideTime(),
		 * box.x + 5, box.x + box.width - 5, box.y + 53, 10);
		 * StringUtilities.drawStringPair(g, g, "Think:", world.ThinkTime(),
		 * box.x + 5, box.x + box.width - 5, box.y + 63, 10);
		 * StringUtilities.drawStringPair(g, g, "Resort:", world.ResortTime(),
		 * box.x + 5, box.x + box.width - 5, box.y + 73, 10);
		 * StringUtilities.drawStringPair(g, g, "Statistics:",
		 * world.StatisticsTime(), box.x + 5, box.x + box.width - 5, box.y + 83,
		 * 10); world.ResetTimes(); #endif
		 */
	}

	public void setTarget(GBObject obj) {
		if (obj == target)
			return;
		if (obj instanceof GBRobot) {
			target = (GBRobot) obj;
			if (target.Class() == GBObjectClass.ocDead)
				target = null;
		} else
			target = null;
	}

	void Draw(Graphics2D g) {
		Rectangle box = new Rectangle();
		if (target == null) {
			StringUtilities.drawStringLeft(g, "No robot selected", 4, 20, 12,
					Color.black, false);
			box.x = kEdgeSpace;
			box.y = kEdgeSpace;
			box.height = kProfileBoxHeight;
			box.width = getWidth() - kEdgeSpace * 2;
			DrawProfileBox(g, box);
		} else {
			// draw robot name
			box.x = box.y = kEdgeSpace;
			box.width = getWidth() - kEdgeSpace * 2;
			box.height = kStatusBoxHeight;
			DrawStatusBox(g, box);
			// get brain
			GBStackBrain sbrain = (GBStackBrain) target.Brain();
			if (sbrain != null) {
				// draw pc
				box.y = box.y + box.height + kEdgeSpace;
				box.height = kPCBoxHeight;
				box.width = kColumnWidth;
				DrawPCBox(g, box, sbrain);
				// draw stack
				box.y = box.y + kPCBoxHeight + kEdgeSpace;
				box.height = kStackBoxHeight;
				box.width = kColumnWidth / 2;
				DrawStackBox(g, box, sbrain);
				// draw return stack
				box.x += box.width;
				DrawReturnStackBox(g, box, sbrain);
				// draw variables
				box.y = box.y + box.height + kEdgeSpace;
				box.width = kColumnWidth;
				box.height = (sbrain.NumVariables() + sbrain
						.NumVectorVariables()) * 10 + 15 + 5;
				box.x = kEdgeSpace;
				DrawVariablesBox(g, box, sbrain);
				// draw prints
				box.y = box.y + box.height + kEdgeSpace;
				box.height = box.y + kPrintBoxHeight;
				DrawPrintBox(g, box, sbrain);
			}
			// draw hardware
			box.y = kStatusBoxHeight + kEdgeSpace * 2;
			box.width = kColumnWidth;
			box.x = kColumnWidth + kEdgeSpace * 2;
			box.height = kPCBoxHeight + kStackBoxHeight + kEdgeSpace;
			DrawHardwareBox(g, box);
			// place toolbar
			toolbar.setBounds(box.x, box.y + box.height + kEdgeSpace,
					toolbar.getWidth(), toolbar.getHeight());
		}
	}

	public int getPreferredWidth() {
		return kColumnWidth * 2 + kEdgeSpace * 3;
	}

	public int getPreferredHeight() {
		int kHardwareBoxHeight = kPCBoxHeight + kStackBoxHeight + kEdgeSpace;
		if (target != null) {
			GBStackBrain sbrain = (GBStackBrain) target.Brain();
			int brainheight = sbrain != null ? kPCBoxHeight + kStackBoxHeight
					+ kPrintBoxHeight
					+ (sbrain.NumVariables() + sbrain.NumVectorVariables())
					* 10 + 15 + 5 * kEdgeSpace : 0;
			return kStatusBoxHeight
					+ (brainheight < kHardwareBoxHeight ? kHardwareBoxHeight
							: brainheight) + 3 * kEdgeSpace;
		} else
			return kProfileBoxHeight + kEdgeSpace * 2;
	}

}
