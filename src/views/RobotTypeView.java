/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package views;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import sides.HardwareTypes;
import sides.RobotType;
import sides.Side;
import support.GBColor;
import ui.SideSelectionListener;
import ui.TypeSelectionListener;
import ui.TypeSelector;

public class RobotTypeView extends JPanel implements TypeSelector,
		TypeSelectionListener, SideSelectionListener, ListSelectionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3759451228873831301L;
	RobotType selectedType;
	Side selectedSide;
	List<TypeSelectionListener> typeListeners;
	JList<RobotType> list;
	DefaultListModel<RobotType> model = new DefaultListModel<RobotType>();
	JLabel hardwareList = new JLabel();
	JLabel header = new JLabel();

	public RobotTypeView() {
		typeListeners = new ArrayList<TypeSelectionListener>();
		list = new JList<RobotType>(model);
		list.setCellRenderer(new TypeRenderer());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(this);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JScrollPane scroll = new JScrollPane(list);
		setBackground(Color.white);
		add(header, Component.CENTER_ALIGNMENT);
		add(scroll);
		add(hardwareList);
		setIgnoreRepaint(true);
		setVisible(true);
	}

	static String hardwareStyle = "<style type=text/css>"
			+ ".head {font-weight: bold; font-height: medium;}"
			+ ".body {font-size: x-small;}"
			+ ".darkgray {color: gray;}"
			+ ".blue {color: blue;}"
			+ ".green {color: green;}"
			+ ".lightgray {color: #D1D0CE;}"
			+ ".red {color: red;}"
			+ ".subtotal {font-weight: bold; font-size: x=small; border-bottom: 1px;}"
			+ ".black {color: black;}"
			+ "table {border-width: 1; background-color: white;}"
			+ "td {padding: 0 0 0 0; } tr {padding: 0 0 0 0;}" + "</style>";
	static String hardwareTable = "<html>" + hardwareStyle + "<table>"
			+ "<thead class=underline><tr>" + "<th>Hardware:</th>"
			+ "<th></th>" + "<th>Cost</th>" + "<th>%%</th>" + "<th>Mass</th>"
			+ "</tr></thead>" + "<tbody class=body>%s</tbody>" + "<tfoot>"
			+ "%s</tfoot></table></html>";

	static String hardwareFormat = "<tr class=body>"
			+ "<td class=%s align=left>%s</td>"
			+ "<td class=%s align=center>%s</td>"
			+ "<td class=%s align=right>%.0f</td>"
			+ "<td class=%s align=right>%.1f</td>"
			+ "<td class=%s align=right>%.2f</td>" + "</tr>";

	void drawHardware() {
		String text;
		if (selectedType == null)
			text = "";
		else
			text = String
					.format(hardwareTable, allHardware(), hardwareFooter());
		hardwareList.setBackground(Color.white);
		hardwareList.setText(text);
	}

	void drawHeader() {
		String text;
		if (selectedSide != null)
			text = String
					.format("<html><table>"
							+ "<td align=left width=100><font size=4 color=%s>%s</font></td>"
							+ "<td align=right width=100>%s</td></tr></table></html>",
							GBColor.toHex(GBColor
									.ContrastingTextColor(selectedSide.Color())),
							selectedSide.Name(), selectedSide.Author());
		else
			text = "No side selected.";
		header.setText(text);
	}

	String hardwareLine(String name, String declarationFormat,
			String colorIfPresent, double cost, double mass,
			Object... declaration) {
		String firstColor = cost == 0 ? "lightgray" : colorIfPresent;
		String secondColor = cost == 0 ? "lightgray" : "black";
		String decl = cost == 0 ? "" : String.format(declarationFormat,
				declaration);
		String thirdColor = cost == 0 ? "lightgray" : "darkgray";
		return String.format(hardwareFormat, firstColor, name, firstColor,
				decl, secondColor, cost, thirdColor, cost / selectedType.Cost()
						* 100, secondColor, mass);
	}

	String allHardware() {
		StringBuilder sb = new StringBuilder();
		sb.append(hardwareLine("chassis", "", "black", 20, .5));
		sb.append(hardwareLine(HardwareTypes.hcProcessor.tagName, "%d", "blue",
				selectedType.hardware.ProcessorCost(),
				selectedType.hardware.ProcessorMass(),
				selectedType.hardware.Processor()));
		sb.append(hardwareLine(HardwareTypes.hcEngine.tagName, "%.2f", "black",
				selectedType.hardware.EngineCost(),
				selectedType.hardware.EngineMass(),
				selectedType.hardware.Engine()));
		sb.append(hardwareLine(HardwareTypes.hcConstructor.tagName, "%.2f",
				"green", selectedType.hardware.constructor.Cost(),
				selectedType.hardware.constructor.Mass(),
				selectedType.hardware.constructor.Rate()));
		sb.append(hardwareLine(HardwareTypes.hcEnergy.tagName, "%.0f %.0f",
				"green", selectedType.hardware.EnergyCost(),
				selectedType.hardware.EnergyMass(),
				selectedType.hardware.MaxEnergy(),
				selectedType.hardware.InitialEnergy()));
		sb.append(hardwareLine(HardwareTypes.hcSolarCells.tagName, "%.2f",
				"green", selectedType.hardware.SolarCellsCost(),
				selectedType.hardware.SolarCellsMass(),
				selectedType.hardware.SolarCells()));
		sb.append(hardwareLine(HardwareTypes.hcEater.tagName, "%.1f", "green",
				selectedType.hardware.EaterCost(),
				selectedType.hardware.EaterMass(),
				selectedType.hardware.Eater()));
		sb.append(hardwareLine(HardwareTypes.hcSyphon.tagName, "%.1f %.0f",
				"blue", selectedType.hardware.syphon.Cost(),
				selectedType.hardware.syphon.Mass(),
				selectedType.hardware.syphon.Power(),
				selectedType.hardware.syphon.Range()));
		sb.append(hardwareLine(HardwareTypes.hcRobotSensor.tagName, "%.0f %d",
				"blue", selectedType.hardware.sensor1.Cost(),
				selectedType.hardware.sensor1.Mass(),
				selectedType.hardware.sensor1.Range(),
				selectedType.hardware.sensor1.NumResults()));
		sb.append(hardwareLine(HardwareTypes.hcFoodSensor.tagName, "%.0f %d",
				"blue", selectedType.hardware.sensor2.Cost(),
				selectedType.hardware.sensor2.Mass(),
				selectedType.hardware.sensor2.Range(),
				selectedType.hardware.sensor2.NumResults()));
		sb.append(hardwareLine(HardwareTypes.hcShotSensor.tagName, "%.0f %d",
				"blue", selectedType.hardware.sensor3.Cost(),
				selectedType.hardware.sensor3.Mass(),
				selectedType.hardware.sensor3.Range(),
				selectedType.hardware.sensor3.NumResults()));
		sb.append(hardwareLine(HardwareTypes.hcArmor.tagName, "%.0f", "black",
				selectedType.hardware.ArmorCost(),
				selectedType.hardware.ArmorMass(),
				selectedType.hardware.Armor()));
		sb.append(hardwareLine(HardwareTypes.hcRepairRate.tagName, "%.1f",
				"black", selectedType.hardware.RepairCost(),
				selectedType.hardware.RepairMass(),
				selectedType.hardware.RepairRate()));
		sb.append(hardwareLine(HardwareTypes.hcShield.tagName, "%.0f", "blue",
				selectedType.hardware.ShieldCost(),
				selectedType.hardware.ShieldMass(),
				selectedType.hardware.Shield()));
		sb.append(hardwareLine(HardwareTypes.hcBlaster.tagName, "%.1f %.0f %d",
				"black", selectedType.hardware.blaster.Cost(),
				selectedType.hardware.blaster.Mass(),
				selectedType.hardware.blaster.Damage(),
				selectedType.hardware.blaster.Range(),
				selectedType.hardware.blaster.ReloadTime()));
		sb.append(hardwareLine(HardwareTypes.hcGrenades.tagName,
				"%.1f %.0f %d", "black", selectedType.hardware.grenades.Cost(),
				selectedType.hardware.grenades.Mass(),
				selectedType.hardware.grenades.Damage(),
				selectedType.hardware.grenades.Range(),
				selectedType.hardware.grenades.ReloadTime()));
		sb.append(hardwareLine(HardwareTypes.hcForceField.tagName, "%.1f %.0f",
				"blue", selectedType.hardware.forceField.Cost(),
				selectedType.hardware.forceField.Mass(),
				selectedType.hardware.forceField.Power(),
				selectedType.hardware.forceField.Range()));
		sb.append(hardwareLine(HardwareTypes.hcEnemySyphon.tagName,
				"%.1f %.0f", "blue", selectedType.hardware.enemySyphon.Cost(),
				selectedType.hardware.enemySyphon.Mass(),
				selectedType.hardware.enemySyphon.Power(),
				selectedType.hardware.enemySyphon.Range()));
		sb.append(hardwareLine(
				"Ordinary Hardware",
				"",
				"subtotal",
				selectedType.hardware.Cost()
						- selectedType.hardware.CoolingCost(),
				selectedType.hardware.Mass()
						- selectedType.hardware.CoolingMass()));
		sb.append(hardwareLine(
				String.format("%.0f%% Cooling Charge",
						selectedType.hardware.CoolingCost()
								/ selectedType.hardware.BaseCost() * 100), "",
				"black", selectedType.hardware.CoolingCost(),
				selectedType.hardware.CoolingMass()));
		if (selectedType.brain == null)
			sb.append(hardwareLine("No brain", "", "black", 0, 0));
		else
			sb.append(hardwareLine("Code", "", "black",
					selectedType.brain.Cost(), selectedType.brain.Mass()));
		return sb.toString();
	}

	static String footerFormat = "<tr>"
			+ "<td>Total:</td>"
			+ "<td></td>"
			+ "<td>%.0f</td>"
			+ "<td></td>"
			+ "<td>%.1f</td>"
			+ "</tr>"
			+ "<tr><td colspan=5 class=%s>Mass-based damage multiplier: %.0f%%-%.0f%%</td></tr>";

	String hardwareFooter() {
		double multi = Math
				.max(selectedType
						.MassiveDamageMultiplier(selectedType.Mass() * 100),
						100);
		double pregMulti = Math
				.max(selectedType
						.MassiveDamageMultiplier(selectedType.Mass() * 200),
						100);
		String color = multi > 100 ? "red" : "lightgray";
		return String.format(footerFormat, selectedType.Cost(), selectedType
				.Mass(), color, multi,
				selectedType.hardware.constructor.Rate() > 0 ? pregMulti
						: multi);
	}

	@Override
	public void setSelectedSide(Side side) {
		if (side != selectedSide) {
			selectedSide = side;
			selectedType = null;
			model.removeAllElements();
			if (selectedSide != null)
				if (selectedSide.types.size() > 0) {
					for (RobotType type : selectedSide.types)
						model.addElement(type);
					list.setSelectedIndex(0);
					selectedType = selectedSide.types.get(0);
				}
			drawHeader();
			drawHardware();
			notifyListeners();
			repaint();
		}
	}

	@Override
	public void setSelectedType(RobotType type) {
		if (type == selectedType)
			return;
		selectedType = type;
		drawHardware();
		repaint();
	}

	@Override
	public RobotType getSelectedType() {
		return selectedType;
	}

	@Override
	public void addTypeSelectionListener(TypeSelectionListener listener) {
		if (listener != null)
			typeListeners.add(listener);
	}

	@Override
	public void removeTypeSelectionListener(TypeSelectionListener listener) {
		typeListeners.remove(listener);

	}

	void notifyListeners() {
		for (TypeSelectionListener l : typeListeners)
			if (l != null)
				l.setSelectedType(selectedType);
	}

	@Override
	public Side getSelectedSide() {
		return selectedSide;
	}

	@Override
	public void addSideSelectionListener(SideSelectionListener listener) {
		// Types view cannot change the selected side so no listeners are
		// required
	}

	@Override
	public void removeSideSelectionListener(SideSelectionListener listener) {
		// Types view cannot change the selected side so no listeners are
		// required
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		int index = list.getSelectedIndex();
		if (index == -1 || index > model.size()) {
			selectedType = null;
		} else
			selectedType = model.get(index);
		drawHardware();
		notifyListeners();
	}

	class TypeRenderer extends JLabel implements ListCellRenderer<RobotType> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1044029084694616742L;
		ImageIcon thumbnail;

		@Override
		public Component getListCellRendererComponent(
				JList<? extends RobotType> list, RobotType type, int index,
				boolean isSelected, boolean cellHasFocus) {
			String textFormat = "<html><tr><td width=20 rowspan=2 color=%s>%d.</td>"
					+ "<td width=100>%s</td>"
					+ "<td color=green>Cost: %.0f</td>"
					+ "</tr><tr>"
					+ "<td><font size=2>%s</font></td>"
					+ "<td><font size=2 color=gray>%.0f%% economy, %.0f%% combat</font></td>"
					+ "</tr></html>";
			String population = "";
			if (type != null)
				if (type.population > 0) {
					population = String.format("population: %d",
							type.population);
				}
			setText(String
					.format(textFormat,
							GBColor.toHex(type.Color()),
							index + 1,
							type.name,
							type.Cost(),
							population,
							type.hardware.GrowthCost()
									/ type.hardware.BaseCost() * 100,
							type.hardware.CombatCost()
									/ type.hardware.BaseCost() * 100));
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			thumbnail = new ImageIcon(type.icon);
			this.setIcon(thumbnail);
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);
			return this;
		}
	}
}
