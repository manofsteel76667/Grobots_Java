/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

import simulation.GBHardwareState;
import simulation.GBObject;
import simulation.GBObjectClass;
import simulation.GBRobot;
import ui.ObjectSelectionListener;
import brains.BrainStatus;
import brains.GBStackBrain;

public class Debugger extends JPanel implements ObjectSelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7388724419204956062L;

	GBRobot selectedObject;
	GBStackBrain brain;

	JTextPane status = new JTextPane();
	JTextPane hardwareVariables = new JTextPane(){
        /**
		 * 
		 */
		private static final long serialVersionUID = -6242748078637577591L;
		@Override
        public boolean getScrollableTracksViewportHeight() {
           return false;
        }
        @Override
        public boolean getScrollableTracksViewportWidth() {
           return false;
        }
     };
	JScrollPane hwScrollPane;
	JTextPane instructions = new JTextPane();
	JTextPane stack = new JTextPane();
	JTextPane returnStack = new JTextPane();
	JTextPane variables = new JTextPane(){
        /**
		 * 
		 */
		private static final long serialVersionUID = -7101122340397798123L;
		@Override
        public boolean getScrollableTracksViewportHeight() {
           return false;
        }
        @Override
        public boolean getScrollableTracksViewportWidth() {
           return false;
        }
     };
	JScrollPane varScrollPane;
	JLabel lastPrint = new JLabel("Last Print:");

	Style basicStyle;
	SimpleAttributeSet basicAttr;
	SimpleAttributeSet bold;
	SimpleAttributeSet blue;
	SimpleAttributeSet green;
	SimpleAttributeSet red;
	SimpleAttributeSet gray;

	// Stuff to draw the robot with
	int iconHeight = 64;
	int scale = iconHeight / 2;
	BufferedImage robotImage = new BufferedImage(iconHeight, iconHeight,
			BufferedImage.TYPE_INT_ARGB);
	JLabel robotIcon = new JLabel();

	public Debugger(JToolBar bar) {
		setMinimumSize(new Dimension(600, 620));
		JToolBar toolbar = bar;
		if (bar == null)
			toolbar = new JToolBar();
		basicAttr = new SimpleAttributeSet();
		// FIXME: WTF doesn't this change the tab size?
		StyleConstants.setTabSet(basicAttr, new TabSet(
				new TabStop[] { new TabStop(10, TabStop.ALIGN_DECIMAL,
						TabStop.LEAD_NONE) }));
		setAttributes();

		// Set sizes on the subpanels
		status.setPreferredSize(new Dimension(200, 55));
		status.setMaximumSize(status.getPreferredSize());
		instructions.setPreferredSize(new Dimension(200, 150));
		instructions.setMaximumSize(instructions.getPreferredSize());
		stack.setPreferredSize(new Dimension(100, 150));
		stack.setMaximumSize(stack.getPreferredSize());
		returnStack.setPreferredSize(stack.getPreferredSize());
		returnStack.setMaximumSize(returnStack.getPreferredSize());
		variables.setPreferredSize(new Dimension(270, 400));
		hardwareVariables.setPreferredSize(variables.getPreferredSize());
		setDefaultTextPaneStyle(status);
		setDefaultTextPaneStyle(hardwareVariables);
		setDefaultTextPaneStyle(instructions);
		setDefaultTextPaneStyle(stack);
		setDefaultTextPaneStyle(returnStack);
		setDefaultTextPaneStyle(variables);

		// Add scrollpanes for variable windows
		varScrollPane = new JScrollPane(variables);
		varScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		DefaultCaret caret = (DefaultCaret) variables.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		hwScrollPane = new JScrollPane(hardwareVariables);
		hwScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		caret = (DefaultCaret) hardwareVariables.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

		// Lay out the panels
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout
				.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(
						layout.createSequentialGroup()
								.addComponent(robotIcon)
								.addComponent(status,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, 200)
								.addComponent(toolbar))
				.addGroup(
						layout.createSequentialGroup()
								.addComponent(instructions,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE,
										Integer.MAX_VALUE)
								.addComponent(stack,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE,
										Integer.MAX_VALUE)
								.addComponent(returnStack,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE,
										Integer.MAX_VALUE))
				.addGroup(
						layout.createSequentialGroup()
								.addComponent(varScrollPane, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE,
										Integer.MAX_VALUE)
								.addComponent(hwScrollPane,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE,
										Integer.MAX_VALUE))
				.addComponent(lastPrint));
		layout.setVerticalGroup(layout
				.createSequentialGroup()
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.BASELINE)
								.addComponent(robotIcon).addComponent(status)
								.addComponent(toolbar))
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.BASELINE)
								.addComponent(instructions).addComponent(stack)
								.addComponent(returnStack))
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.BASELINE)
								.addComponent(varScrollPane, 400,
										GroupLayout.PREFERRED_SIZE,
										Integer.MAX_VALUE)
								.addComponent(hwScrollPane, 400,
										GroupLayout.PREFERRED_SIZE,
										Integer.MAX_VALUE))
				.addComponent(lastPrint));
		setIgnoreRepaint(true);
	}

	void setDefaultTextPaneStyle(JTextPane pane) {
		pane.setEditable(false);
		// FIXME: This doesn't affect the margins
		pane.setMargin(new Insets(10, 10, 10, 10));
		pane.setBorder(BorderFactory.createLineBorder(getForeground(), 1));
		pane.setMinimumSize(pane.getPreferredSize());
	}

	void setAttributes() {
		bold = new SimpleAttributeSet(basicAttr);
		StyleConstants.setBold(bold, true);
		green = new SimpleAttributeSet(basicAttr);
		StyleConstants.setForeground(green, Color.green);
		blue = new SimpleAttributeSet(basicAttr);
		StyleConstants.setForeground(blue, Color.blue);
		red = new SimpleAttributeSet(basicAttr);
		StyleConstants.setForeground(red, Color.red);
		gray = new SimpleAttributeSet(basicAttr);
		StyleConstants.setForeground(gray, Color.gray);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		buildStatus();
		buildInstructions();
		buildStack();
		buildReturnStack();
		buildVariables();
		buildHardwareVariables();
		lastPrint.setText(String.format("Last Print: %s",
				hasBrain() ? brain.getLastPrint() : "None"));
		updateRobotImage();
		robotIcon.setIcon(new ImageIcon(robotImage));
	}

	boolean hasBrain() {
		if (selectedObject == null)
			return false;
		return selectedObject.getBrain() != null;
	}

	void buildStatus() {
		Document doc = new DefaultStyledDocument();
		try {
			doc.remove(0, doc.getLength());
			if (selectedObject != null) {
				doc.insertString(0, selectedObject.toString(), basicAttr);
			} else {
				doc.insertString(0, "No Robot Selected.", basicAttr);
				return;
			}
			doc.insertString(doc.getLength(), "\n", basicAttr);
			doc.insertString(doc.getLength(),
					(hasBrain() ? (brain != null ? "Stack brain."
							: "Unknown brain.") : "No brain.") + "\t",
					basicAttr);
			doc.insertString(doc.getLength(), "Status: ", basicAttr);
			if (brain != null) {
				doc.insertString(doc.getLength(), brain.status.name,
						brain.status == BrainStatus.bsOK ? green
								: brain.status == BrainStatus.bsError ? red
										: basicAttr);
			}
			doc.insertString(doc.getLength(), "  \n", basicAttr);
			doc.insertString(doc.getLength(), "Remaining: ", basicAttr);
			if (brain != null) {
				doc.insertString(doc.getLength(),
						Integer.toString(brain.getRemaining()), basicAttr);
			}
			status.setDocument(doc);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	void buildInstructions() {
		Document doc = new DefaultStyledDocument();
		try {
			doc.remove(0, doc.getLength());
			doc.insertString(0, "PC:\t", bold);
			if (brain == null)
				return;
			int pc = brain.getPC();
			doc.insertString(
					doc.getLength(),
					String.format("%s (line %d)\n",
							brain.getAddressDescription(pc), brain.getPCLine()), bold);
			for (int i = -4; i <= 3; i++) {
				if (brain.isValidAddress(pc + i))
					doc.insertString(doc.getLength(), String.format(
							"%s:\t%s\n", brain.getAddressName(pc + i),
							brain.getDisassembleAddress(pc + i)), i == 0 ? blue
							: basicAttr);
			}
			instructions.setDocument(doc);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

	}

	void buildStack() {
		Document doc = new DefaultStyledDocument();
		try {
			doc.remove(0, doc.getLength());
			doc.insertString(0, "Stack:\n", bold);
			if (brain == null)
				return;
			int height = brain.getStackHeight();
			if (height != 0) {
				for (int i = 1; i < 5 && i <= height; i++)
					doc.insertString(
							doc.getLength(),
							String.format("%d:\t%.4f\n", height - i,
									brain.getStackAt(height - i)), basicAttr);
			} else
				doc.insertString(doc.getLength(), "Empty", basicAttr);
			stack.setDocument(doc);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	void buildReturnStack() {
		Document doc = new DefaultStyledDocument();
		try {
			doc.remove(0, doc.getLength());
			doc.insertString(0, "Return Stack:\n", bold);
			if (brain == null)
				return;
			int height = brain.getReturnStackHeight();
			if (height != 0) {
				for (int i = 1; i < 5 && i <= height; i++)
					doc.insertString(doc.getLength(), String.format(
							"%d:\t%s\n",
							height - i,
							brain.getAddressLastLabel(brain.getReturnStackAt(height
									- i))), basicAttr);
			} else
				doc.insertString(doc.getLength(), "Empty", basicAttr);
			returnStack.setDocument(doc);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	void buildVariables() {
		Document doc = variables.getDocument();
		variables.setDocument(new DefaultStyledDocument());
		int scrollPosition = varScrollPane.getVerticalScrollBar().getValue();
		try {
			doc.remove(0, doc.getLength());
			doc.insertString(0, "Variables:\n", bold);
			if (brain == null)
				return;
			int vars = brain.getNumVariables();
			int vvars = brain.getNumVectorVariables();
			if (vars != 0 || vvars != 0) {
				for (int i = 0; i < vars; i++) {
					doc.insertString(doc.getLength(), String.format(
							"%s:\t%.4f\n", brain.getVariableName(i),
							brain.readVariable(i)), basicAttr);
				}
				for (int i = 0; i < vvars; i++) {
					doc.insertString(doc.getLength(), String.format(
							"%s:\t%s\n", brain.getVectorVariableName(i), brain
									.readVectorVariable(i).toString(4)),
							basicAttr);
				}
			} else
				doc.insertString(doc.getLength(), "No variables", basicAttr);
			variables.setDocument(doc);
			varScrollPane.getVerticalScrollBar().setValue(scrollPosition);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	void buildHardwareVariables() {
		Document doc = hardwareVariables.getDocument();
		hardwareVariables.setDocument(new DefaultStyledDocument());
		int scrollPosition = hwScrollPane.getVerticalScrollBar().getValue();
		try {
			doc.remove(0, doc.getLength());
			doc.insertString(0, "Hardware Variables:\n", bold);
			if (selectedObject == null) {
				return;
			}
			GBHardwareState hw = selectedObject.hardware;
			String varFormat = "  %s:\t%.4f\n";
			String vectorFormat = "  %s:\t%s\n";
			String intFormat = "  %s:\t%d\n";
			String strFormat = "  %s:\t%s\n";
			// Physics section
			doc.insertString(doc.getLength(), "Physics\n", bold);
			doc.insertString(
					doc.getLength(),
					String.format(vectorFormat, "Position",
							selectedObject.getPosition()), basicAttr);
			doc.insertString(doc.getLength(),
					String.format(varFormat, "Mass", selectedObject.getMass()),
					basicAttr);
			doc.insertString(
					doc.getLength(),
					String.format(varFormat, "Radius", selectedObject.getRadius()),
					basicAttr);
			doc.insertString(doc.getLength(), String.format(vectorFormat,
					"Velocity", selectedObject.getVelocity().toString(4)),
					basicAttr);
			doc.insertString(doc.getLength(),
					String.format(varFormat, "Speed", selectedObject.getSpeed()),
					basicAttr);
			doc.insertString(doc.getLength(), String.format(vectorFormat,
					"Engine Vel. ", hw.getEngineVelocity().toString(4)), basicAttr);
			// Energy section
			doc.insertString(doc.getLength(), "Energy\n", bold);
			doc.insertString(
					doc.getLength(),
					String.format(varFormat, "Energy", selectedObject.getEnergy()),
					basicAttr);
			if (hw.getEater() > 0)
				doc.insertString(doc.getLength(),
						String.format(varFormat, "Eaten", hw.getEaten()), green);
			if (hw.syphon.getMaxRate() > 0)
				doc.insertString(doc.getLength(), String.format(varFormat,
						"Syphoned", hw.syphon.getSyphoned()),
						hw.syphon.getSyphoned() > 0 ? green
								: hw.syphon.getSyphoned() == 0 ? basicAttr : red);
			if (hw.enemySyphon.getMaxRate() > 0)
				doc.insertString(
						doc.getLength(),
						String.format(varFormat, "Enemy Syphoned",
								hw.enemySyphon.getSyphoned()),
						hw.enemySyphon.getSyphoned() > 0 ? green : hw.enemySyphon
								.getSyphoned() == 0 ? basicAttr : red);
			// Defense section
			doc.insertString(doc.getLength(), "Defense\n", bold);
			doc.insertString(doc.getLength(),
					String.format(varFormat, "Armor", hw.getArmor()), basicAttr);
			if (hw.getActualShield() != 0)
				doc.insertString(
						doc.getLength(),
						String.format(varFormat, "Shield",
								selectedObject.getShieldFraction()), basicAttr);
			// Constructor Section
			if (hw.constructor.getMaxRate() != 0) {
				doc.insertString(doc.getLength(), "Constructor\n", bold);
				doc.insertString(doc.getLength(),
						String.format(
								strFormat,
								"Type",
								hw.constructor.getRobotType() != null ? hw.constructor
										.getRobotType().name : "None"), basicAttr);
				doc.insertString(
						doc.getLength(),
						String.format(varFormat, "Rate", hw.constructor.getRate()),
						basicAttr);
				doc.insertString(
						doc.getLength(),
						String.format(varFormat, "Progress",
								hw.constructor.getFraction()), basicAttr);
			}
			// Sensor section
			String resultFormat = "%s-%s";
			if (hw.sensor1.getFiringCost() != 0 || hw.sensor2.getFiringCost() != 0
					|| hw.sensor3.getFiringCost() != 0) {
				doc.insertString(doc.getLength(), "Sensors\n", bold);
				if (hw.sensor1.getFiringCost() != 0) {
					doc.insertString(
							doc.getLength(),
							String.format(intFormat, "robot-found",
									hw.sensor1.getNumResults()), basicAttr);
					doc.insertString(doc.getLength(),
							String.format(intFormat, "current-robot-result",
									hw.sensor1.getCurrentResult()), basicAttr);
					doc.insertString(doc.getLength(), String.format("  "
							+ vectorFormat,
							String.format(resultFormat, "robot", "position"),
							hw.sensor1.getWhereFound().toString(4)), basicAttr);
					doc.insertString(doc.getLength(), String.format("  "
							+ vectorFormat,
							String.format(resultFormat, "robot", "velocity"),
							hw.sensor1.getVelocity().toString(4)), basicAttr);
				}
				if (hw.sensor2.getFiringCost() != 0) {
					doc.insertString(
							doc.getLength(),
							String.format(intFormat, "food-found",
									hw.sensor2.getNumResults()), basicAttr);
					doc.insertString(doc.getLength(), String.format(intFormat,
							"current-food-result", hw.sensor2.getCurrentResult()),
							basicAttr);
					doc.insertString(doc.getLength(), String.format("  "
							+ vectorFormat,
							String.format(resultFormat, "food", "position"),
							hw.sensor2.getWhereFound().toString(4)), basicAttr);
					doc.insertString(doc.getLength(), String.format("  "
							+ vectorFormat,
							String.format(resultFormat, "food", "velocity"),
							hw.sensor2.getVelocity().toString(4)), basicAttr);
				}
				if (hw.sensor3.getFiringCost() != 0) {
					doc.insertString(
							doc.getLength(),
							String.format(intFormat, "shot-found",
									hw.sensor3.getNumResults()), basicAttr);
					doc.insertString(doc.getLength(), String.format(intFormat,
							"current-shot-result", hw.sensor3.getCurrentResult()),
							basicAttr);
					doc.insertString(doc.getLength(), String.format("  "
							+ vectorFormat,
							String.format(resultFormat, "shot", "position"),
							hw.sensor3.getWhereFound().toString(4)), basicAttr);
					doc.insertString(doc.getLength(), String.format("  "
							+ vectorFormat,
							String.format(resultFormat, "shot", "velocity"),
							hw.sensor3.getVelocity().toString(4)), basicAttr);
				}
			}
			// Miscellaneous hardware values
			doc.insertString(doc.getLength(), "Misc.\n", bold);
			if (hw.blaster.getDamage() > 0)
				doc.insertString(
						doc.getLength(),
						String.format(intFormat, "blaster-cooldown",
								hw.blaster.getCooldown()), basicAttr);
			if (hw.grenades.getDamage() > 0)
				doc.insertString(doc.getLength(), String.format(intFormat,
						"grenades-cooldown", hw.grenades.getCooldown()), basicAttr);
			if (hw.forceField.getMaxPower() > 0) {
				doc.insertString(doc.getLength(), String.format(varFormat,
						"force-field-angle", hw.forceField.getAngle()), basicAttr);
				doc.insertString(doc.getLength(), String.format(varFormat,
						"force-field-distance", hw.forceField.getDistance()),
						basicAttr);
			}
			doc.insertString(doc.getLength(),
					String.format(varFormat, "flag", selectedObject.flag),
					basicAttr);
			hardwareVariables.setDocument(doc);
			hwScrollPane.getVerticalScrollBar().setValue(scrollPosition);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	void updateRobotImage() {
		Graphics g = robotImage.getGraphics();
		g.clearRect(0, 0, robotImage.getWidth(), robotImage.getHeight());
		if (selectedObject != null) {
			BufferedImage img = selectedObject.getScaledDrawing(scale, true);
			g.drawImage(img, scale - img.getWidth() / 2,
					scale - img.getHeight() / 2, img.getWidth(),
					img.getHeight(), null);
			g.dispose();
		}
	}

	@Override
	public void setSelectedObject(GBObject obj) {
		if (obj == selectedObject)
			return;
		if (obj instanceof GBRobot) {
			selectedObject = (GBRobot) obj;
			if (selectedObject.getObjectClass() == GBObjectClass.ocDead)
				selectedObject = null;
			if (hasBrain())
				brain = (GBStackBrain) selectedObject.getBrain();
			else
				brain = null;
		} else {
			selectedObject = null;
			brain = null;
		}
		repaint();
	}
}
