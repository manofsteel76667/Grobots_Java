package ui;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import exception.GBAbort;
import exception.GBError;
import sides.Side;
import sides.SideReader;
import simulation.GBWorld;
import views.GBPortal.toolTypes;

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
	showAbout("About Grobots"),
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

	public JRadioButtonMenuItem asJRadioButtonMenuItem() {
		JRadioButtonMenuItem ret = new JRadioButtonMenuItem(description);
		if (accelerator != null)
			ret.setAccelerator(accelerator);
		return ret;
	}

	public JCheckBoxMenuItem asJCheckBoxMenuItem() {
		JCheckBoxMenuItem ret = new JCheckBoxMenuItem(description);
		if (accelerator != null)
			ret.setAccelerator(accelerator);
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
}

class GBMenu extends JMenuBar implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3330599322475052070L;
	public GBApplication app;
	JMenu fileMenu;
	JMenu windowMenu;
	JMenu simulationMenu;
	JMenu toolMenu;
	GBWorld world;
	public Map<MenuItems, AbstractButton> menuButtons;
	public ButtonGroup speedControls;
	public ButtonGroup toolSelectors;
	public Map<MenuItems, JCheckBoxMenuItem> viewOptions;

	public GBMenu(GBApplication _app) {
		app = _app;
		world = app.world;
		menuButtons = new HashMap<MenuItems, AbstractButton>();
		buildFileMenu();
		buildWindowMenu();
		buildSimulationMenu();
		buildViewMenu();
		buildToolsMenu();
		buildButtonGroups();
		for (MenuItems mi : MenuItems.values())
			if (menuButtons.containsKey(mi))
				menuButtons.get(mi).addActionListener(this);
	}

	/**
	 * Returns the menu item that corresponds to the MenuItem enum passed, or
	 * null if one is not found. Used for enabling and disabling menu options
	 * 
	 * @param cmd
	 * @return
	 */
	public MenuElement getMenuItem(MenuItems cmd) {
		return getMenuItem(this.getSubElements(), cmd);
	}

	MenuElement getMenuItem(MenuElement[] list, MenuItems cmd) {
		MenuElement ret = null;
		for (MenuElement item : list) {
			if (item instanceof JMenuItem)
				if (((JMenuItem) item).getText() == cmd.description)
					ret = item;
				else if (item instanceof JRadioButtonMenuItem)
					if (((JRadioButtonMenuItem) item).getText() == cmd.description)
						ret = item;
					else if (item instanceof JCheckBoxMenuItem)
						if (((JCheckBoxMenuItem) item).getText() == cmd.description)
							ret = item;
			if (ret != null)
				break;
			else if (item.getSubElements().length > 0)
				ret = getMenuItem(item.getSubElements(), cmd);
		}
		return ret;
	}

	void buildButtonGroups() {
		speedControls = new ButtonGroup();
		speedControls.add(menuButtons.get(MenuItems.slow));
		speedControls.add(menuButtons.get(MenuItems.normal));
		speedControls.add(menuButtons.get(MenuItems.fast));
		speedControls.add(menuButtons.get(MenuItems.unlimited));
		speedControls.setSelected(menuButtons.get(MenuItems.normal).getModel(),
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
		menuButtons.put(MenuItems.showAbout,
				ret.add(MenuItems.showAbout.asJMenuItem()));
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
		menuButtons.put(MenuItems.tournament,
				ret.add(MenuItems.tournament.asJMenuItem()));
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

	@Override
	public void actionPerformed(ActionEvent e) {
		// Handle Menu click
		if (MenuItems.byDescription(e.getActionCommand()) != null) {
			MenuItems mi = MenuItems.byDescription(e.getActionCommand());
			try {
				switch (mi) {
				case addManna:
					app.portal.tool = toolTypes.ptAddManna;
					app.portal.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					break;
				case addRobot:
					app.portal.tool = toolTypes.ptAddRobot;
					app.portal.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					break;
				case addSeed:
					app.portal.tool = toolTypes.ptAddSeed;
					app.portal.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					break;
				case addSeeds:
					world.AddSeeds();
					break;
				case autoFollow:
					break;
				case blasts:
					app.portal.tool = toolTypes.ptBlasts;
					app.portal.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					break;
				case clearMap:
					break;
				case duplicateSide:
					break;
				case erase:
					app.portal.tool = toolTypes.ptErase;
					app.portal.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					break;
				case erasearea:
					app.portal.tool = toolTypes.ptEraseBig;
					app.portal.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					break;
				case exit:
					if (JOptionPane.showConfirmDialog(null,
							"Are you sure you want to exit the game?",
							"Confirm Exit", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
						System.exit(0);
					break;
				case fast:
					app.stepRate=StepRates.fast;
					break;
				case firstPage:
					break;
				case followRandom:
					break;
				case loadSide:
					JFileChooser fc = new JFileChooser();
					int retval = fc.showOpenDialog(app);
					if (retval == JFileChooser.APPROVE_OPTION) {
						JOptionPane.showMessageDialog(app,
								fc.getSelectedFiles());
						for (File f : fc.getSelectedFiles()) {
							Side newside;
							newside = SideReader.Load(f.getName());
							world.AddSide(newside);

						}
					}
					break;
				case move:
					app.portal.tool = toolTypes.ptMove;
					app.portal.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					break;
				case newRound:
					world.Reset();
					world.AddSeeds();
					world.running = true;
					break;
				case nextPage:
					break;
				case normal:
					app.stepRate=StepRates.normal;
					break;
				case pause:
					world.running = false;
					break;
				case previousPage:
					break;
				case pull:
					app.portal.tool = toolTypes.ptPull;
					app.portal.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					break;
				case randomNear:
					break;
				case refollow:
					break;
				case reloadSide:
					break;
				case removeAllSides:
					world.Reset();
					world.RemoveAllSides();
					world.running = false;
					break;
				case removeSide:
					break;
				case reseedDeadSides:
					world.ReseedDeadSides();
					break;
				case resetScores:
					world.ResetTournamentScores();
					break;
				case rules:
					break;
				case run:
					world.running = true;
					app.lastStep = System.currentTimeMillis();
					break;
				case saveScores:
					world.DumpTournamentScores(true);
					break;
				case scrollDown:
					break;
				case scrollFollow:
					app.portal.tool = toolTypes.ptScroll;
					app.portal.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					break;
				case scrollUp:
					break;
				case showAbout:
					break;
				case showDebugger:
					break;
				case showDecorations:
					break;
				case showMeters:					
					break;
				case showMiniMapTrails:
					break;
				case showMinimap:
					break;
				case showPrints:
					world.reportPrints = !world.reportPrints;
					break;
				case showRobotErrors:
					world.reportErrors = !world.reportErrors;
					break;
				case showRoster:
					app.roster.setVisible(true);
					break;
				case showSensors:
					break;
				case showSharedMemory:
					break;
				case showStatistics:
					break;
				case showTournament:
					break;
				case showTypes:
					break;
				case singleFrame:
					world.AdvanceFrame();
					world.running = false;
					break;
				case slow:
					app.stepRate=StepRates.slow;
					break;
				case smite:
					app.portal.tool = toolTypes.ptSmite;
					app.portal.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					break;
				case stepBrain:
					break;
				case stopStartBrain:
					break;
				case tournament:
					
					break;
				case unlimited:
					app.stepRate=StepRates.unlimited;
					break;
				case zoomIn:
					break;
				case zoomOut:
					break;
				case zoomStandard:
					break;
				default:
					break;
				}
			} catch (GBError | IOException err) {
				try {
					GBError.NonfatalError(err.getMessage());
				} catch (GBAbort e1) {
					System.exit(1);
				}
				catch(Exception ex){
					GBError.FatalError(ex.getMessage());
				}
			}
		}
	}
}
