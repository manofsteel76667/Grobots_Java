/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package ui;

import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import simulation.GBGame;

/* @formatter:off */
enum MenuItems {
	//File menu
	loadSide("Load Side", KeyStroke
			.getKeyStroke('O', InputEvent.CTRL_DOWN_MASK)), 
	duplicateSide("Duplicate Side"), 
	reloadSide("ReLoad Side", KeyStroke
			.getKeyStroke('L', InputEvent.CTRL_DOWN_MASK)),
	removeSide("Remove Side", KeyStroke
			.getKeyStroke('K', InputEvent.CTRL_DOWN_MASK)),
	removeAllSides("Remove All Sides"),
	exit("Exit", KeyStroke
			.getKeyStroke('Q', InputEvent.CTRL_DOWN_MASK)),
	//Window menu
	showRoster("Roster"),
	showMinimap("Minimap", KeyStroke
			.getKeyStroke('M', InputEvent.CTRL_DOWN_MASK)),
	showStatistics("Statistics", KeyStroke
			.getKeyStroke('S', InputEvent.CTRL_DOWN_MASK)),
	showTypes("Types", KeyStroke
			.getKeyStroke('Y', InputEvent.CTRL_DOWN_MASK)),
	showDebugger("Debugger", KeyStroke
			.getKeyStroke('D', InputEvent.CTRL_DOWN_MASK)),
	showTournament("Tournament Scores", KeyStroke
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
			.getKeyStroke("DELETE")),
	addSeeds("Add Seeds", KeyStroke
			.getKeyStroke('E', InputEvent.CTRL_DOWN_MASK)),
	reseedDeadSides("Reseed Dead Sides"),
	rules("Rules"),
	tournament("Tournament", KeyStroke
			.getKeyStroke('T', InputEvent.CTRL_DOWN_MASK)),
	saveScores("Save Tournament Scores"),
	resetScores("Reset Tournament Scores"),
	//Views Menu
	zoomIn("Zoom In", KeyStroke.getKeyStroke('+')),
	zoomOut("Zoom Out", KeyStroke.getKeyStroke('-')),
	zoomStandard("Zoom Standard", KeyStroke.getKeyStroke('0')),
	showSensors("Show Sensors"),
	showDecorations("Show Decorations"),
	showMeters("Show Meters"),
	showMiniMapTrails("Minimap Trails", KeyStroke.getKeyStroke('T')),
	showRobotErrors("Show Robot Errors"),
	showPrints("Show Prints"),
	refollow("Refollow"),
	followRandom("Follow Random", KeyStroke
			.getKeyStroke("F1")),
	randomNear("Random Near", KeyStroke
			.getKeyStroke("F2")),
	autoFollow("Autofollow", KeyStroke
			.getKeyStroke('A', InputEvent.CTRL_DOWN_MASK)),
	nextPage("Next Page", KeyStroke.getKeyStroke('T')),
	previousPage("Previous Page", KeyStroke
			.getKeyStroke("PAGE_UP")),
	firstPage("First Page", KeyStroke
			.getKeyStroke("PAGE_DOWN")),
	scrollUp("Scroll Up", KeyStroke
			.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK)),
	scrollDown("Scroll Down", KeyStroke
			.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK)),
	//Tools menu
	scrollFollow("Scroll/Follow"),
	addRobot("Add Robot", KeyStroke.getKeyStroke('M')),
	addManna("Add Manna", KeyStroke.getKeyStroke('R')),
	addSeed("Add Seed", KeyStroke.getKeyStroke('S')),
	move("Move", KeyStroke.getKeyStroke('V')),
	pull("Pull", KeyStroke.getKeyStroke('P')),
	smite("Smite", KeyStroke.getKeyStroke('X')),
	blasts("Blasts", KeyStroke.getKeyStroke('B')),
	erase("Erase", KeyStroke.getKeyStroke('W')),
	erasearea("Erase Area", KeyStroke.getKeyStroke('A')),
	//Help menu
	gotoDocs("Documentation"),
	gotoWebsite("Website"),
	gotoWiki("Wiki"),
	gotoGroup("Discuss"),
	gotoSides("More Sides"),
	showAbout("About Grobots"),
	//Buttons found on the simulation toolbar
	play("playpause"),
	speedup("speedup"),
	slowdown("slowdown"),
	mute("mute")
	;
	
	
	
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

	/**
	 * List of all buttons created for this command 
	 */
	List<AbstractButton> buttons = new ArrayList<AbstractButton>();
	
	public JMenuItem asJMenuItem() {
		JMenuItem ret = new JMenuItem(description);
		if (accelerator != null)
			ret.setAccelerator(accelerator);
		buttons.add(ret);
		return ret;
	}

	public JRadioButtonMenuItem asJRadioButtonMenuItem() {
		JRadioButtonMenuItem ret = new JRadioButtonMenuItem(description);
		if (accelerator != null)
			ret.setAccelerator(accelerator);
		buttons.add(ret);
		return ret;
	}

	public JCheckBoxMenuItem asJCheckBoxMenuItem() {
		JCheckBoxMenuItem ret = new JCheckBoxMenuItem(description);
		if (accelerator != null)
			ret.setAccelerator(accelerator);
		buttons.add(ret);
		return ret;
	}

	public JButton asJButton() {
		JButton ret = new JButton();
		ret.setActionCommand(description);
		// No accelerator key for a button
		buttons.add(ret);
		return ret;
	}

	public static MenuItems byDescription(String _description) {
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
	
	public void setEnabled(boolean value) {
		for (AbstractButton b : buttons)
			b.setEnabled(value);
	}
	
	public void setIcon(Icon value) {
		for (AbstractButton b : buttons)
			if (b.getIcon() != null)
				b.setIcon(value);
	}
}

class GBMenu extends JMenuBar {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3330599322475052070L;
	public GBApplication app;
	JMenu fileMenu;
	JMenu windowMenu;
	JMenu simulationMenu;
	JMenu toolMenu;
	JMenu helpMenu;
	GBGame game;
	public ButtonGroup speedControls;
	public ButtonGroup toolSelectors;
	public JCheckBoxMenuItem cbTournament;
	public Map<MenuItems, JCheckBoxMenuItem> viewOptions;
	public Map<MenuItems, AbstractButton> menuButtons;

	public GBMenu(GBApplication _app) {
		app = _app;
		game = app.game;
		menuButtons = new HashMap<MenuItems, AbstractButton>();
		buildFileMenu();
		buildWindowMenu();
		buildSimulationMenu();
		buildViewMenu();
		buildToolsMenu();
		buildButtonGroups();
		buildHelpMenu();
		for (MenuItems mi : MenuItems.values())
			if (menuButtons.containsKey(mi))
				menuButtons.get(mi).addActionListener(app);
	}

	void buildButtonGroups() {
		speedControls = new ButtonGroup();
		speedControls.add(menuButtons.get(MenuItems.slow));
		speedControls.add(menuButtons.get(MenuItems.normal));
		speedControls.add(menuButtons.get(MenuItems.fast));
		speedControls.add(menuButtons.get(MenuItems.unlimited));
		speedControls.setSelected(menuButtons.get(MenuItems.fast).getModel(),
				true);
		toolSelectors = new ButtonGroup();
		toolSelectors.add(menuButtons.get(MenuItems.scrollFollow));
		toolSelectors.add(menuButtons.get(MenuItems.addRobot));
		toolSelectors.add(menuButtons.get(MenuItems.addManna));
		toolSelectors.add(menuButtons.get(MenuItems.addSeed));
		toolSelectors.add(menuButtons.get(MenuItems.move));
		toolSelectors.add(menuButtons.get(MenuItems.pull));
		toolSelectors.add(menuButtons.get(MenuItems.smite));
		toolSelectors.add(menuButtons.get(MenuItems.blasts));
		toolSelectors.add(menuButtons.get(MenuItems.erase));
		toolSelectors.add(menuButtons.get(MenuItems.erasearea));
		toolSelectors.setSelected(menuButtons.get(MenuItems.scrollFollow)
				.getModel(), true);
		viewOptions = new HashMap<MenuItems, JCheckBoxMenuItem>();
		viewOptions.put(MenuItems.showSensors,
				(JCheckBoxMenuItem) menuButtons.get(MenuItems.showSensors));
		viewOptions.put(MenuItems.showDecorations,
				(JCheckBoxMenuItem) menuButtons.get(MenuItems.showDecorations));
		viewOptions.put(MenuItems.showMeters,
				(JCheckBoxMenuItem) menuButtons.get(MenuItems.showMeters));
		viewOptions.put(MenuItems.showMiniMapTrails,
				(JCheckBoxMenuItem) menuButtons
						.get(MenuItems.showMiniMapTrails));
		viewOptions.put(MenuItems.showRobotErrors,
				(JCheckBoxMenuItem) menuButtons.get(MenuItems.showRobotErrors));
		viewOptions.put(MenuItems.showPrints,
				(JCheckBoxMenuItem) menuButtons.get(MenuItems.showPrints));
		viewOptions.get(MenuItems.showDecorations).setSelected(true);
		viewOptions.get(MenuItems.showMeters).setSelected(true);
		viewOptions.get(MenuItems.showMiniMapTrails).setSelected(true);
		viewOptions.get(MenuItems.showRobotErrors).setSelected(
				game.getWorld().reportErrors);
		viewOptions.get(MenuItems.showPrints).setSelected(
				game.getWorld().reportPrints);
	}

	/**
	 * Builds the file portion of the main menu
	 */
	void buildFileMenu() {
		JMenu ret = new JMenu("File");
		menuButtons.put(MenuItems.loadSide,
				ret.add(MenuItems.loadSide.asJMenuItem()));
		menuButtons.put(MenuItems.duplicateSide,
				ret.add(MenuItems.duplicateSide.asJMenuItem()));
		menuButtons.put(MenuItems.reloadSide,
				ret.add(MenuItems.reloadSide.asJMenuItem()));
		ret.addSeparator();
		menuButtons.put(MenuItems.removeSide,
				ret.add(MenuItems.removeSide.asJMenuItem()));
		menuButtons.put(MenuItems.removeAllSides,
				ret.add(MenuItems.removeAllSides.asJMenuItem()));
		ret.addSeparator();
		menuButtons.put(MenuItems.exit, ret.add(MenuItems.exit.asJMenuItem()));
		this.add(ret);
	}

	/**
	 * Builds the Window portion of the main menu, for showing and hiding views
	 */
	void buildWindowMenu() {
		JMenu ret = new JMenu("Window");
		menuButtons.put(MenuItems.showRoster,
				ret.add(MenuItems.showRoster.asJMenuItem()));
		menuButtons.put(MenuItems.showMinimap,
				ret.add(MenuItems.showMinimap.asJMenuItem()));
		menuButtons.put(MenuItems.showStatistics,
				ret.add(MenuItems.showStatistics.asJMenuItem()));
		menuButtons.put(MenuItems.showTypes,
				ret.add(MenuItems.showTypes.asJMenuItem()));
		menuButtons.put(MenuItems.showDebugger,
				ret.add(MenuItems.showDebugger.asJMenuItem()));
		menuButtons.put(MenuItems.showTournament,
				ret.add(MenuItems.showTournament.asJMenuItem()));
		menuButtons.put(MenuItems.showSharedMemory,
				ret.add(MenuItems.showSharedMemory.asJMenuItem()));
		this.add(ret);
	}

	/**
	 * Builds the simulation portion of the main menu
	 */
	void buildSimulationMenu() {
		JMenu ret = new JMenu("Simulation");
		menuButtons.put(MenuItems.run, ret.add(MenuItems.run.asJMenuItem()));
		menuButtons.put(MenuItems.singleFrame,
				ret.add(MenuItems.singleFrame.asJMenuItem()));
		menuButtons.put(MenuItems.stepBrain,
				ret.add(MenuItems.stepBrain.asJMenuItem()));
		menuButtons
				.put(MenuItems.pause, ret.add(MenuItems.pause.asJMenuItem()));
		menuButtons.put(MenuItems.stopStartBrain,
				ret.add(MenuItems.stopStartBrain.asJMenuItem()));
		ret.addSeparator();
		menuButtons.put(MenuItems.slow,
				ret.add(MenuItems.slow.asJCheckBoxMenuItem()));
		menuButtons.put(MenuItems.normal,
				ret.add(MenuItems.normal.asJCheckBoxMenuItem()));
		menuButtons.put(MenuItems.fast,
				ret.add(MenuItems.fast.asJCheckBoxMenuItem()));
		menuButtons.put(MenuItems.unlimited,
				ret.add(MenuItems.unlimited.asJCheckBoxMenuItem()));
		ret.addSeparator();
		menuButtons.put(MenuItems.newRound,
				ret.add(MenuItems.newRound.asJMenuItem()));
		menuButtons.put(MenuItems.clearMap,
				ret.add(MenuItems.clearMap.asJMenuItem()));
		menuButtons.put(MenuItems.addSeeds,
				ret.add(MenuItems.addSeeds.asJMenuItem()));
		menuButtons.put(MenuItems.reseedDeadSides,
				ret.add(MenuItems.reseedDeadSides.asJMenuItem()));
		ret.addSeparator();
		menuButtons
				.put(MenuItems.rules, ret.add(MenuItems.rules.asJMenuItem()));
		cbTournament = MenuItems.tournament.asJCheckBoxMenuItem();
		cbTournament.setSelected(true);
		game.tournament = true;
		menuButtons.put(MenuItems.tournament, ret.add(cbTournament));
		menuButtons.put(MenuItems.saveScores,
				ret.add(MenuItems.saveScores.asJMenuItem()));
		menuButtons.put(MenuItems.resetScores,
				ret.add(MenuItems.resetScores.asJMenuItem()));
		this.add(ret);
	}

	/**
	 * Build the views portion of the main menu
	 */
	void buildViewMenu() {
		JMenu ret = new JMenu("Views");
		menuButtons.put(MenuItems.zoomIn,
				ret.add(MenuItems.zoomIn.asJMenuItem()));
		menuButtons.put(MenuItems.zoomOut,
				ret.add(MenuItems.zoomOut.asJMenuItem()));
		menuButtons.put(MenuItems.zoomStandard,
				ret.add(MenuItems.zoomStandard.asJMenuItem()));
		ret.addSeparator();
		menuButtons.put(MenuItems.showSensors,
				ret.add(MenuItems.showSensors.asJCheckBoxMenuItem()));
		menuButtons.put(MenuItems.showDecorations,
				ret.add(MenuItems.showDecorations.asJCheckBoxMenuItem()));
		menuButtons.put(MenuItems.showMeters,
				ret.add(MenuItems.showMeters.asJCheckBoxMenuItem()));
		menuButtons.put(MenuItems.showMiniMapTrails,
				ret.add(MenuItems.showMiniMapTrails.asJCheckBoxMenuItem()));
		ret.addSeparator();
		menuButtons.put(MenuItems.showRobotErrors,
				ret.add(MenuItems.showRobotErrors.asJCheckBoxMenuItem()));
		menuButtons.put(MenuItems.showPrints,
				ret.add(MenuItems.showPrints.asJCheckBoxMenuItem()));
		ret.addSeparator();
		menuButtons.put(MenuItems.refollow,
				ret.add(MenuItems.refollow.asJMenuItem()));
		menuButtons.put(MenuItems.followRandom,
				ret.add(MenuItems.followRandom.asJMenuItem()));
		menuButtons.put(MenuItems.randomNear,
				ret.add(MenuItems.randomNear.asJMenuItem()));
		menuButtons.put(MenuItems.autoFollow,
				ret.add(MenuItems.autoFollow.asJMenuItem()));
		ret.addSeparator();
		menuButtons.put(MenuItems.nextPage,
				ret.add(MenuItems.nextPage.asJMenuItem()));
		menuButtons.put(MenuItems.previousPage,
				ret.add(MenuItems.previousPage.asJMenuItem()));
		menuButtons.put(MenuItems.firstPage,
				ret.add(MenuItems.firstPage.asJMenuItem()));
		ret.addSeparator();
		menuButtons.put(MenuItems.scrollUp,
				ret.add(MenuItems.scrollUp.asJMenuItem()));
		menuButtons.put(MenuItems.scrollDown,
				ret.add(MenuItems.scrollDown.asJMenuItem()));
		this.add(ret);
	}

	/**
	 * Build the tools portion of the main menu
	 */
	void buildToolsMenu() {
		JMenu ret = new JMenu("Tools");
		menuButtons.put(MenuItems.scrollFollow,
				ret.add(MenuItems.scrollFollow.asJCheckBoxMenuItem()));
		ret.addSeparator();
		menuButtons.put(MenuItems.addManna,
				ret.add(MenuItems.addManna.asJCheckBoxMenuItem()));
		menuButtons.put(MenuItems.addRobot,
				ret.add(MenuItems.addRobot.asJCheckBoxMenuItem()));
		menuButtons.put(MenuItems.addSeed,
				ret.add(MenuItems.addSeed.asJCheckBoxMenuItem()));
		ret.addSeparator();
		menuButtons.put(MenuItems.move,
				ret.add(MenuItems.move.asJCheckBoxMenuItem()));
		menuButtons.put(MenuItems.pull,
				ret.add(MenuItems.pull.asJCheckBoxMenuItem()));
		menuButtons.put(MenuItems.smite,
				ret.add(MenuItems.smite.asJCheckBoxMenuItem()));
		menuButtons.put(MenuItems.blasts,
				ret.add(MenuItems.blasts.asJCheckBoxMenuItem()));
		menuButtons.put(MenuItems.erase,
				ret.add(MenuItems.erase.asJCheckBoxMenuItem()));
		menuButtons.put(MenuItems.erasearea,
				ret.add(MenuItems.erasearea.asJCheckBoxMenuItem()));
		this.add(ret);
	}

	void buildHelpMenu() {
		JMenu ret = new JMenu("Help");
		menuButtons.put(MenuItems.gotoDocs,
				ret.add(MenuItems.gotoDocs.asJMenuItem()));
		menuButtons.put(MenuItems.gotoWiki,
				ret.add(MenuItems.gotoWiki.asJMenuItem()));
		menuButtons.put(MenuItems.gotoGroup,
				ret.add(MenuItems.gotoGroup.asJMenuItem()));
		menuButtons.put(MenuItems.gotoSides,
				ret.add(MenuItems.gotoSides.asJMenuItem()));
		ret.addSeparator();
		menuButtons.put(MenuItems.gotoWebsite,
				ret.add(MenuItems.gotoWebsite.asJMenuItem()));
		menuButtons.put(MenuItems.showAbout,
				ret.add(MenuItems.showAbout.asJMenuItem()));
		this.add(ret);
	}

	public JToolBar simToolbar(ActionListener l) {
		JToolBar ret = new JToolBar();
		JButton btn;
		btn = makeButton("control-rewind-icon.png",
				MenuItems.slowdown, "Slow Down Simulation", "");
		btn.addActionListener(l);
		ret.add(btn);
		btn = makeButton("control-pause-icon.png", MenuItems.pause,
				"Pause Simulation", "");
		btn.addActionListener(l);
		ret.add(btn);
		btn = makeButton("control-play-icon.png", MenuItems.play,
				"Run Simulation", "");
		btn.addActionListener(l);
		ret.add(btn);
		btn = makeButton("control-fastforward-icon.png",
				MenuItems.speedup, "Speed Up Simulation", "");
		btn.addActionListener(l);
		ret.add(btn);
		btn = makeButton("unmute.png",
				MenuItems.mute, "Mute / Unmute", "");
		btn.addActionListener(l);
		ret.add(btn);
		return ret;
	}

	public JToolBar fileToolBar(ActionListener l) {
		JToolBar ret = new JToolBar();
		JButton btn;
		btn = makeButton("Actions-folder-new-icon.png",
				MenuItems.loadSide, "Load Side", "Load");
		btn.addActionListener(l);
		ret.add(btn);
		btn = makeButton("Actions-edit-copy-icon.png",
				MenuItems.duplicateSide, "Copy Side", "Copy");
		btn.addActionListener(l);
		ret.add(btn);
		btn = makeButton("Actions-edit-redo-icon.png",
				MenuItems.reloadSide, "Reload Side", "Reload");
		btn.addActionListener(l);
		ret.add(btn);
		btn = makeButton("Actions-edit-delete-icon.png",
				MenuItems.removeSide, "Remove Side", "Remove");
		btn.addActionListener(l);
		ret.add(btn);
		return ret;
	}

	public JToolBar debugToolbar(ActionListener l) {
		JToolBar ret = new JToolBar();
		JButton btn;
		btn = makeButton("control-end-icon.png",
				MenuItems.singleFrame, "Advance 1 Frame", "");
		btn.addActionListener(l);
		ret.add(btn);
		btn = makeButton("right_footprint.png",
				MenuItems.stepBrain, "Step Brain", "");
		btn.addActionListener(l);
		ret.add(btn);
		return ret;
	}

	JButton makeButton(String imageName, MenuItems actionCommand,
			String toolTipText, String altText) {
		// Look for the image.
		URL imageURL = getClass().getResource(imageName);

		// Create and initialize the button.
		JButton button = actionCommand.asJButton();
		button.setToolTipText(toolTipText);

		if (imageURL != null) { // image found
			button.setIcon(new ImageIcon(imageURL, altText));
		} else { // no image found
			button.setText(altText);
			System.err.println("Resource not found: " + imageName);
		}

		return button;
	}
}
