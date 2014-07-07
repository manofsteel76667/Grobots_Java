/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import sides.HardwareSpec;
import sides.RobotType;
import sides.Side;
import simulation.GBWorld;
import support.GBColor;
import support.StringUtilities;
import ui.GBApplication;
import brains.BrainSpec;

public class RobotTypeView extends ListView {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3759451228873831301L;
	GBApplication app;
	GBWorld world;

	public RobotTypeView(GBApplication _app) {
		app = _app;
		world = app.world;
		preferredWidth = 250;
		setPreferredSize(new Dimension(preferredWidth, getPreferredHeight()));
		MouseAdapter ma = new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				// Select side on click
				app.setSelectedType(null);
				for (int i = 0; i < itemlist.size() && i < app.getSelectedSide().types.size(); i++)
					if (itemlist.get(i).contains(arg0.getPoint())) {
						app.setSelectedType(app.getSelectedSide().types.get(i));
						break;
					}
				repaint();
			}
		};
		addMouseListener(ma);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		super.draw((Graphics2D) g);
	}

	public static final int kTypeStatsWidth = 80;
	public static final int kTypePopulationLeft = 5;
	public static final int kTypeBiomassLeft = 80;
	public static final int kHardwareNameLeft = 5;
	public static final int kHardwareArgumentsLeft = 85;
	public static final int kHardwareCostRight = 60;
	public static final int kHardwarePercentRight = 38;
	public static final int kHardwareMassRight = 5;

	void DrawHardwareLine(Graphics2D g, Rectangle box, int base, String name,
			Color color, String arg1, String arg2, String arg3, String arg4,
			double cost, double mass) {
		HardwareSpec hw = app.getSelectedType().Hardware();
		base += (base > 0 ? box.y : box.y + box.height);
		// name and args
		Color textcolor = cost != 0 ? color : Color.lightGray;
		StringUtilities.drawStringLeft(g, name, box.x + kHardwareNameLeft,
				base, 9, textcolor);
		if (cost != 0)
			StringUtilities.drawStringLeft(g, arg1 + ' ' + arg2 + ' ' + arg3
					+ ' ' + arg4, box.x + kHardwareArgumentsLeft, base, 9,
					textcolor);
		// cost and mass
		Color numcolor = (cost != 0 ? Color.black : Color.lightGray);
		String fmt = cost < 10 ? "%.1f" : "%.0f";
		StringUtilities.drawStringRight(g, String.format(fmt, cost), box.x
				+ box.width - kHardwareCostRight, base, 9, numcolor);
		if (cost != 0)
			StringUtilities.drawStringRight(g,
					String.format("%.0f", cost / hw.BaseCost() * 100), box.x
							+ box.width - kHardwarePercentRight, base, 9,
					Color.gray);
		StringUtilities.drawStringRight(g, String.format("%.2f", mass), box.x
				+ box.width - kHardwareMassRight, base, 9, numcolor);
	}

	void DrawNumericHardwareLine(Graphics2D g, Rectangle box, int base,
			String name, Color color, double arg, double cost, double mass) {
		DrawHardwareLine(g, box, base, name, color, Double.toString(arg),
				"", "", "", cost, mass);
	}

	void DrawNumericHardwareLine(Graphics2D g, Rectangle box, int base,
			String name, Color color, double arg1, double arg2, double cost,
			double mass) {
		DrawHardwareLine(g, box, base, name, color,
				Double.toString(arg1), Double.toString(arg2), "",
				"", cost, mass);
	}

	void DrawNumericHardwareLine(Graphics2D g, Rectangle box, int base,
			String name, Color color, double arg1, double arg2, double arg3,
			double cost, double mass) {
		DrawHardwareLine(g, box, base, name, color,
				Double.toString(arg1), Double.toString(arg2),
				Double.toString(arg3), "", cost, mass);
	}

	void DrawHardwareSummaryLine(Graphics2D g, Rectangle box, int base,
			String name, Color color, double cost, double mass) {
		base += (base > 0 ? box.y : box.y + box.height);
		StringUtilities.drawStringLeft(g, name, box.x + kHardwareNameLeft,
				base, 10, color, true);
		StringUtilities.drawStringRight(g, String.format("%.0f", cost), box.x
				+ box.width - kHardwareCostRight, base, 10, color, true);
		StringUtilities.drawStringRight(g, String.format("%.2f", mass), box.x
				+ box.width - kHardwareMassRight, base, 10, color, true);
	}

	@Override
	Rectangle drawHeader(Graphics2D g) {
		Rectangle box = new Rectangle(margin, margin, getWidth() - margin * 2, 
				g.getFontMetrics(new Font("Serif", Font.PLAIN, 12)).getHeight() + padding * 2);
		Side side = app.getSelectedSide();
		if (side != null) {
			drawBox(g, box);
			box.grow(-padding, -padding);
			StringUtilities.drawStringLeft(g, side.Name(), box.x,
					box.y + box.height, 12, side.Color().ContrastingTextColor());
			StringUtilities.drawStringRight(g, side.Author(), box.x + box.width, 
					box.y + box.height, 12, Color.black);
		} else
			StringUtilities.drawStringLeft(g, "No side selected", box.x,
					box.y + box.height, 12, Color.black);
		box.grow(padding, padding);
		return box;
	}

	@Override
	protected Rectangle drawOneItem(Graphics2D g, int index) {
		Rectangle box = getItemRect(index, 12, false);
		box.setBounds(new Rectangle(box.x, box.y, box.width, box.height + 
				g.getFontMetrics(new Font("Serif", Font.PLAIN, 9)).getHeight()));
		Side side = app.getSelectedSide();
		if (side == null)
			return new Rectangle(0,0,0,0);
		RobotType type = side.GetType(index+1);
		boolean selected = type == app.getSelectedType();
		if (type == null)
			return new Rectangle(0,0,0,0);
		drawBox(g, box, selected);
		box.grow(-padding, -padding);
		// draw ID and name and color
		StringUtilities.drawStringLeft(g, Integer.toString(type.ID()) + '.',
				box.x, box.y + g.getFontMetrics().getHeight() + padding, 12, type.Color());
		StringUtilities.drawStringLeft(g, type.name, box.x + 20, box.y + g.getFontMetrics().getHeight() + padding,
				12, selected ? Color.white : Color.black);
		// stuff
		StringUtilities.drawStringPair(g, "Cost:",
				String.format("%.0f", type.Cost()), box.x + box.width
						- kTypeStatsWidth, box.x + box.width,
				box.y + padding + g.getFontMetrics(new Font("Serif", Font.PLAIN, 9)).getHeight(), 9, selected ? Color.green : GBColor.darkGreen,
				false);
		HardwareSpec hw = type.Hardware();
		StringUtilities
				.drawStringRight(
						g,
						StringUtilities.toPercentString(
								hw.GrowthCost() / hw.BaseCost(), 0)
								+ " economy, "
								+ StringUtilities.toPercentString(
										hw.CombatCost() / hw.BaseCost(), 0)
								+ " combat", box.x + box.width, box.y
								+ box.height, 9, selected ? Color.white
								: Color.black);
		// StringUtilities.drawStringPair("Mass:", ToString(type.Mass(), 1),
		// box.x + box.width - kTypeStatsWidth + 5, box.x + box.width - 2, box.y
		// + box.height - 3,
		// 9, selected ? Color.white : Color.black);
		if (side.Scores().Seeded() != 0) {
			StringUtilities.drawStringPair(g, "Population:",
					Integer.toString(type.population), box.x
							+ kTypePopulationLeft,
					box.x + kTypeBiomassLeft, box.y + box.height, 9,
					selected ? Color.white : Color.blue, false);
			// StringUtilities.drawStringLongPair("Biomass:", type.Biomass(),
			// box.x + kTypeBiomassLeft, box.x + box.width - kTypeStatsWidth -
			// 4, box.y + box.height - 3,
			// 9, selected ? Color.green : Color.darkGreen);
		}
		box.grow(padding, padding);
		return box;
	}

	@Override
	Rectangle drawFooter(Graphics2D g) {
		int textHeightNormal = g.getFontMetrics(new Font("Serif", Font.PLAIN, 10)).getHeight();
		int textHeightSmall = g.getFontMetrics(new Font("Serif", Font.PLAIN, 9)).getHeight();
		Rectangle box = new Rectangle(margin, margin + header.height + items.height,
				getWidth() - margin * 2, textHeightNormal + padding * 2);
		box.setBounds(new Rectangle(box.x, box.y, box.width, box.height + 
				textHeightNormal * 3 + 
				textHeightSmall * 22 +
				padding * 6));
		RobotType type = app.getSelectedType();
		if (type == null)
			return new Rectangle(0,0,0,0);
		drawBox(g, box);
		box.grow(-padding, -padding);
		// hardware
		HardwareSpec hw = type.Hardware();
		int y = box.y + textHeightNormal;
		StringUtilities.drawStringLeft(g, "Hardware:", box.x
				+ kHardwareNameLeft, y, 10, Color.black, true);
		StringUtilities.drawStringRight(g, "Cost", box.x + box.width
				- kHardwareCostRight, y, 10, Color.black, true);
		StringUtilities.drawStringRight(g, "%", box.x + box.width
				- kHardwarePercentRight, y, 10, Color.black, true);
		StringUtilities.drawStringRight(g, "Mass", box.x + box.width
				- kHardwareMassRight, y, 10, Color.black, true);
		g.setColor(Color.black);
		y += padding;
		g.drawLine(box.x, y, box.x + box.width, y);
		// basic
		y = y + textHeightSmall - box.y;
		DrawHardwareLine(g, box, y, "chassis", Color.black, "", "", "", "",
				hw.ChassisCost(), hw.ChassisMass());
		y += textHeightSmall;
		DrawNumericHardwareLine(g, box, y, "processor", Color.blue,
				hw.Processor(), hw.Memory(), hw.ProcessorCost(),
				hw.ProcessorMass());
		y += textHeightSmall;
		DrawNumericHardwareLine(g, box, y, "engine", Color.black, hw.Engine(),
				hw.EngineCost(), hw.EngineMass());
		y += textHeightSmall;
		DrawNumericHardwareLine(g, box, y, "constructor", GBColor.darkGreen,
				hw.constructor.Rate(), hw.constructor.Cost(),
				hw.constructor.Mass());
		y += padding;
		// energy-related
		y += textHeightSmall;
		DrawNumericHardwareLine(g, box, y, "energy", GBColor.darkGreen,
				hw.MaxEnergy(), hw.InitialEnergy(), hw.EnergyCost(),
				hw.EnergyMass());
		y += textHeightSmall;
		DrawNumericHardwareLine(g, box, y, "solar-cells", GBColor.darkGreen,
				hw.SolarCells(), hw.SolarCellsCost(), hw.SolarCellsMass());
		y += textHeightSmall;
		DrawNumericHardwareLine(g, box, y, "eater", GBColor.darkGreen,
				hw.Eater(), hw.EaterCost(), hw.EaterMass());
		y += textHeightSmall;
		DrawNumericHardwareLine(g, box, y, "syphon", Color.blue,
				hw.syphon.Power(), hw.syphon.Range(), hw.syphon.Cost(),
				hw.syphon.Mass());
		y += padding;
		// sensors
		y += textHeightSmall;
		DrawNumericHardwareLine(g, box, y, "robot-sensor", Color.blue,
				hw.sensor1.Range(), hw.sensor1.NumResults(), hw.sensor1.Cost(),
				hw.sensor1.Mass());
		y += textHeightSmall;
		DrawNumericHardwareLine(g, box, y, "food-sensor", Color.blue,
				hw.sensor2.Range(), hw.sensor2.NumResults(), hw.sensor2.Cost(),
				hw.sensor2.Mass());
		y += textHeightSmall;
		DrawNumericHardwareLine(g, box, y, "shot-sensor", Color.blue,
				hw.sensor3.Range(), hw.sensor3.NumResults(), hw.sensor3.Cost(),
				hw.sensor3.Mass());
		y += padding;
		// defense
		y += textHeightSmall;
		DrawNumericHardwareLine(g, box, y, "armor", Color.black, hw.Armor(),
				hw.ArmorCost(), hw.ArmorMass());
		y += textHeightSmall;
		DrawNumericHardwareLine(g, box, y, "repair-rate", Color.darkGray,
				hw.RepairRate(), hw.RepairCost(), hw.RepairMass());
		y += textHeightSmall;
		DrawNumericHardwareLine(g, box, y, "shield", Color.blue, hw.Shield(),
				hw.ShieldCost(), hw.ShieldMass());
		y += padding;
		// weapons
		y += textHeightSmall;
		DrawNumericHardwareLine(g, box, y, "blaster", Color.black,
				hw.blaster.Damage(), hw.blaster.Range(),
				hw.blaster.ReloadTime(), hw.blaster.Cost(), hw.blaster.Mass());
		y += textHeightSmall;
		DrawNumericHardwareLine(g, box, y, "grenades", Color.black,
				hw.grenades.Damage(), hw.grenades.Range(),
				hw.grenades.ReloadTime(), hw.grenades.Cost(),
				hw.grenades.Mass());
		y += textHeightSmall;
		DrawNumericHardwareLine(g, box, y, "force-field", Color.blue,
				hw.forceField.Power(), hw.forceField.Range(),
				hw.forceField.Cost(), hw.forceField.Mass());
		y += textHeightSmall;
		DrawNumericHardwareLine(g, box, y, "enemy-syphon", Color.blue,
				hw.enemySyphon.Power(), hw.enemySyphon.Range(),
				hw.enemySyphon.Cost(), hw.enemySyphon.Mass());
		y += textHeightSmall;
		DrawNumericHardwareLine(g, box, y, "bomb", Color.red, hw.Bomb(),
				hw.BombCost(), hw.BombMass());
		y += padding;
		// totals and brain
		y += textHeightNormal;
		DrawHardwareSummaryLine(g, box, y, "Ordinary hardware:", Color.black,
				hw.BaseCost(), hw.Mass() - hw.CoolingMass());
		y += textHeightSmall;
		DrawHardwareLine(
				g,
				box,
				y,
				StringUtilities.toPercentString(
						hw.CoolingCost() / hw.BaseCost(), 0)
						+ " cooling charge", hw.BaseCost() > 1000 ? Color.red
						: Color.black, "", "", "", "", hw.CoolingCost(),
				hw.CoolingMass());
		y += textHeightSmall;
		BrainSpec brain = type.Brain();
		if (brain != null)
			DrawHardwareLine(g, box, y, "code", Color.black, "", "", "", "",
					brain.Cost(), brain.Mass());
		else
			StringUtilities.drawStringLeft(g, "No brain", box.x
					+ kHardwareNameLeft, y, 10,
					Color.lightGray);
		g.setColor(Color.black);
		y += padding;
		g.drawLine(box.x, box.y + y, box.x + box.width, box.y + y);
		y += textHeightNormal;
		DrawHardwareSummaryLine(g, box, y, "Total:", Color.black,
				type.Cost(), type.Mass());
		double damageMult = type.MassiveDamageMultiplier(type.Mass());
		double pregnantMult = type.MassiveDamageMultiplier(type.Mass() * 2);
		y += textHeightSmall;
		StringUtilities
				.drawStringLeft(
						g,
						"Mass-based damage multiplier: "
								+ StringUtilities
										.toPercentString(damageMult, 0)
								+ ((hw.constructor.Rate() != 0 && pregnantMult > 1) ? " to "
										+ StringUtilities.toPercentString(
												pregnantMult, 0)
										: ""), box.x + kHardwareNameLeft, box.y + y, 9, damageMult > 1 ? Color.red
								: Color.lightGray);
		box.grow(padding,  padding);
		return box;
	}

	@Override
	Rectangle drawItems(Graphics2D g) {
		Side side = app.getSelectedSide();
		if (side == null)
			return new Rectangle(0, 0, 0, 0);
		items = getStartingItemsRect(10, false);
		for (int i = 0; i < side.types.size(); i++) {
			Rectangle item = drawOneItem(g, i);
			if (item != null)
				addItem(item);
		}
		return items;
	}

}
