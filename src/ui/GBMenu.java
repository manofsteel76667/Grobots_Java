package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import simulation.GBWorld;

/* @formatter:off */
enum MenuItems {
	//File menu
	loadside("Load Side", KeyStroke
			.getKeyStroke('O', InputEvent.CTRL_DOWN_MASK)), 
	duplicateside("Duplicate Side"), 
	reloadSide("ReLoad Side", KeyStroke
			.getKeyStroke('L', InputEvent.CTRL_DOWN_MASK)),
	removeSide("Remove Side", KeyStroke
			.getKeyStroke('K', InputEvent.CTRL_DOWN_MASK)),
	removeAllSides("Remove All Sides"),
	exit("Exit", KeyStroke
			.getKeyStroke('Q', InputEvent.CTRL_DOWN_MASK)),
	//Window menu
	showabout("About Grobots"),
	showRoster("Roster"),
	showMinimap("Minimap", KeyStroke
			.getKeyStroke('M', InputEvent.CTRL_DOWN_MASK)),
	showStatistics("Statistics", KeyStroke
			.getKeyStroke('S', InputEvent.CTRL_DOWN_MASK)),
	showTypes("Types", KeyStroke
			.getKeyStroke('Y', InputEvent.CTRL_DOWN_MASK)),
	showDebugger("Debugger", KeyStroke
			.getKeyStroke('D', InputEvent.CTRL_DOWN_MASK)),
	showTournament("Tournament", KeyStroke
			.getKeyStroke('E', InputEvent.CTRL_DOWN_MASK)),
	showSharedMemory("Shared Memory"),
	//Simulation menu
	run("Run", KeyStroke
			.getKeyStroke('R', InputEvent.CTRL_DOWN_MASK)),
	singleFrame("Single Frame", KeyStroke
			.getKeyStroke('F', InputEvent.CTRL_DOWN_MASK)),
	stepBrain("Step Brain", KeyStroke
			.getKeyStroke('B', InputEvent.CTRL_DOWN_MASK)),
	pause("Pause", KeyStroke
			.getKeyStroke('P', InputEvent.CTRL_DOWN_MASK)),
	stopStartBrain("Stop/Start Brain"),
	slow("Slow (10fps)", KeyStroke
			.getKeyStroke('1', InputEvent.CTRL_DOWN_MASK)),
	normal("Normal (30fps)", KeyStroke
			.getKeyStroke('3', InputEvent.CTRL_DOWN_MASK)),
	fast("Fast (60fps)", KeyStroke
			.getKeyStroke('6', InputEvent.CTRL_DOWN_MASK)),
	unlimited("Unlimited", KeyStroke
			.getKeyStroke('U', InputEvent.CTRL_DOWN_MASK)),
	newRound("New Round", KeyStroke
			.getKeyStroke('N', InputEvent.CTRL_DOWN_MASK)),
	clearMap("Clear Map", KeyStroke
			.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.CTRL_DOWN_MASK)),
	addSeeds("Add Seeds", KeyStroke
			.getKeyStroke('E', InputEvent.CTRL_DOWN_MASK)),
	reseedDeadSides("Reseed Dead Sides"),
	rules("Rules"),
	tournament("Tournament", KeyStroke
			.getKeyStroke('T', InputEvent.CTRL_DOWN_MASK)),
	saveScores("Save Tournament Scores"),
	resetScores("Reset Tournament Scores"),
	//Views Menu
	//Tools menu
	erasearea("Erase Area", KeyStroke.getKeyStroke('A'));
	MenuItems(String desc, KeyStroke accel) {
		description = desc;
		accelerator = accel;
	}
	MenuItems(String desc){
		description = desc;
		accelerator = null;
	}
	/* @formatter:on */
	public final String description;
	public final KeyStroke accelerator;

	public JMenuItem asJMenuItem() {
		JMenuItem ret = new JMenuItem(description);
		if (accelerator != null)
			ret.setAccelerator(accelerator);
		return ret;
	}

	public MenuItems byDescription(String _description) {
		if (descriptionLookup.containsKey(_description))
			return descriptionLookup.get(_description);
		else
			return null;
	}

	static final Map<String, MenuItems> descriptionLookup = new HashMap<String, MenuItems>();
	static {
		for (MenuItems item : MenuItems.values())
			descriptionLookup.put(item.description, item);
	}
}

public class GBMenu extends JMenuBar implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3330599322475052070L;
	public GBApplication app;
	JMenu fileMenu;
	JMenu windowMenu;
	JMenu simulationMenu;
	JMenu toolMenu;

	public GBMenu(GBApplication _app) {
		app = _app;
		buildFileMenu();
		buildWindowMenu();
		buildSimulationMenu();
	}

	/**
	 * Builds the file portion of the main menu
	 */
	void buildFileMenu() {
		JMenu ret = new JMenu("File");
		JMenuItem mi = new JMenuItem("Open Side", KeyEvent.VK_O);
		mi.addActionListener(this);
		mi.setAccelerator(KeyStroke
				.getKeyStroke('O', InputEvent.CTRL_DOWN_MASK));
		ret.add(mi);
		mi = new JMenuItem("Duplicate Side");
		mi.addActionListener(this);
		ret.add(mi);
		mi = new JMenuItem("Reload Side", KeyEvent.VK_R);
		mi.addActionListener(this);
		mi.setAccelerator(KeyStroke
				.getKeyStroke('R', InputEvent.CTRL_DOWN_MASK));
		ret.add(mi);
		ret.addSeparator();
		mi = new JMenuItem("Remove Side", KeyEvent.VK_K);
		mi.addActionListener(this);
		mi.setAccelerator(KeyStroke
				.getKeyStroke('K', InputEvent.CTRL_DOWN_MASK));
		ret.add(mi);
		mi = new JMenuItem("Remove All Sides");
		mi.addActionListener(this);
		ret.add(mi);
		ret.addSeparator();
		mi = new JMenuItem("Quit", KeyEvent.VK_Q);
		mi.addActionListener(this);
		mi.setAccelerator(KeyStroke
				.getKeyStroke('Q', InputEvent.CTRL_DOWN_MASK));
		ret.add(mi);
		this.add(ret);
	}

	/**
	 * Builds the Window portion of the main menu, for showing and hiding views
	 */
	void buildWindowMenu() {
		JMenu ret = new JMenu("Window");
		// Roster View
		JMenuItem mi = new JMenuItem("Roster");
		mi.addActionListener(this);
		ret.add(mi);
		this.add(ret);
	}

	/**
	 * Builds the simulation poriton of the main menu
	 */
	void buildSimulationMenu() {
		JMenu ret = new JMenu("Simulation");

		this.add(ret);
	}

	/**
	 * BUild the tools portion of the main menu
	 */
	void buildToolMenu() {
		JMenu ret = new JMenu("Tools");

		this.add(ret);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
		case "Quit":
			System.exit(0);
			break;
		case "Open Side":
			break;
		case "Roster":
			app.roster.setVisible(!app.roster.isVisible());
			break;
		default:
			break;
		}
	}
}
