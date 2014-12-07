/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import sides.RobotType;
import sides.Side;
import sides.SideReader;
import simulation.GBGame;
import simulation.GBObject;
import simulation.GBObjectClass;
import simulation.GBRobot;
import support.FinePoint;
import views.AboutBox;
import views.Debugger;
import views.GBPortal;
import views.GBPortal.toolTypes;
import views.GBRosterView;
import views.GBScoresView;
import views.GBTournamentView;
import views.RobotTypeView;
import brains.Brain;
import brains.BrainStatus;
import exception.GBAbort;
import exception.GBError;

public class GBApplication extends JFrame implements Runnable, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -955217075865755516L;
	public GBGame game;

	// Views
	GBPortal portal;
	GBPortal minimap;
	GBRosterView roster;
	GBTournamentView tournament;
	GBScoresView statistics;
	AboutBox about;
	RobotTypeView type;
	JDialog tournDialog;
	JDialog aboutDialog;
	JDialog debugDialog;
	JToolBar portalControls;
	JToolBar debugControls;
	JPanel center;
	Debugger debug;

	public enum StepRates {
		slow(10), normal(30), fast(60), unlimited(10000);
		public final int value;

		StepRates(int val) {
			value = val;
		}
	}

	public StepRates stepRate;
	public long lastTime;
	int fastInterval = 20;// Repaint at 25Hz
	javax.swing.Timer fastTimer;
	ActionListener fastUpdate;
	int slowInterval = 1500;
	javax.swing.Timer slowTimer;
	ActionListener slowUpdate;

	GBMenu mainMenu;

	long prevFrameTime;
	Side selectedSide;
	RobotType selectedType;
	GBObject selectedObject;

	int rendering; // Rendering, don't run a turn
	boolean running; // Running a turn, don't render

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new GBApplication());
	}

	public GBApplication() {
		// Creation code found in run() method per recommended java Swing
		// practices
	}

	@Override
	public void run() {
		// Create world and initial conditions
		game = new GBGame(this);
		// world = new GBWorld();
		stepRate = StepRates.fast;

		// Create supporting views and menu.
		mainMenu = new GBMenu(this);
		this.setJMenuBar(mainMenu);
		updateMenu();
		createChildViews();

		// Arrange the screen
		this.getContentPane().setLayout(new GridBagLayout());
		List<BufferedImage> icons = new ArrayList<BufferedImage>();
		String[] resources = new String[] { "grobots16.png", "grobots32.png",
				"grobots48.png", "grobots64.png", "grobots128.png" };
		for (String s : resources) {
			try {
				icons.add(ImageIO.read(getClass().getResourceAsStream(s)));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		setIconImages(icons);
		this.setTitle("Grobots");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
		statistics.setVisible(false);
		setLayouts();
		this.pack();
		setVisible(true);
		createTimers();

	}

	void createTimers() {
		fastUpdate = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!running) {
					rendering++;
					if (portal.isVisible())
						portal.repaint();
					if (minimap.isVisible())
						minimap.repaint();
					if (debug.isVisible())
						debug.repaint();
					rendering--;
					fastTimer.setDelay(fastInterval);
				} else
					fastTimer.setDelay(1);
				;
			};
		};
		fastTimer = new javax.swing.Timer(fastInterval, fastUpdate);
		fastTimer.setRepeats(true);
		fastTimer.setCoalesce(true);
		fastTimer.start();
		slowUpdate = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!running) {
					rendering++;
					if (roster.isVisible())
						roster.repaint();
					if (tournament.isVisible())
						tournament.repaint();
					if (type.isVisible())
						type.repaint();
					if (statistics.isVisible())
						statistics.repaint();
					rendering--;
					slowTimer.setDelay(slowInterval);
				} else
					slowTimer.setDelay(1);
			}
		};
		slowTimer = new javax.swing.Timer(slowInterval, slowUpdate);
		slowTimer.setRepeats(true);
		slowTimer.setCoalesce(true);
		slowTimer.start();
	}

	void setLayouts() {
		for (Component c : this.getContentPane().getComponents())
			this.getContentPane().remove(c);
		GridBagConstraints c = new GridBagConstraints();

		// Roster
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 1;
		c.weightx = 0;
		c.gridheight = minimap.isVisible() ? 3 : 4;
		this.getContentPane().add(roster, c);

		// Portal and toolbar
		center.setPreferredSize(new Dimension(1, 1));
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.ABOVE_BASELINE;
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1;
		c.gridheight = statistics.isVisible() ? 2 : 4;
		this.getContentPane().add(center, c);

		// Statistics
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.BELOW_BASELINE;
		c.gridx = 1;
		c.gridy = 2;
		c.weightx = 0;
		c.weighty = 0;
		c.gridheight = 2;
		c.gridwidth = 1;
		this.getContentPane().add(statistics, c);

		// Type View
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.ABOVE_BASELINE;
		c.gridx = 2;
		c.gridy = 0;
		c.weighty = 1;
		c.gridwidth = 1;
		c.gridheight = 4;
		this.getContentPane().add(type, c);

		// Minimap
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.SOUTHWEST;
		c.gridx = 0;
		c.gridy = 3;
		// c.weighty = .5;
		c.gridheight = 1;
		minimap.setPreferredSize(new Dimension(200, 200));
		this.getContentPane().add(minimap, c);
	}

	void createChildViews() {
		about = new AboutBox();
		about.setPreferredSize(new Dimension(270, 290));
		portal = new GBPortal(this, false);
		portal.setPreferredSize(new Dimension(600, 600));
		minimap = new GBPortal(this, true);
		roster = new GBRosterView(this);
		tournament = new GBTournamentView(this);
		type = new RobotTypeView(this);
		statistics = new GBScoresView(this);
		center = new JPanel();
		center.setLayout(new BorderLayout());
		portalControls = mainMenu.simToolbar(this);
		portalControls.setFloatable(false);
		portalControls.setOrientation(JToolBar.VERTICAL);
		center.add(portal);
		center.add(portalControls, BorderLayout.LINE_START);
		debugControls = mainMenu.debugToolbar(this);
		debugControls.setFloatable(false);
		debug = new Debugger(debugControls);
		debug.setPreferredSize(new Dimension(debug.getPreferredWidth(), debug
				.getPreferredHeight()));
	}

	void updateMenu() {
		setMenuItem(MenuItems.removeAllSides, game.sides.size() > 0
				&& !game.running);
		setMenuItem(MenuItems.reloadSide, selectedSide != null && !game.running);
		setMenuItem(MenuItems.duplicateSide, selectedSide != null
				&& !game.running);
		setMenuItem(MenuItems.removeSide, selectedSide != null && !game.running);
		setMenuItem(MenuItems.addRobot, selectedType != null);
		setMenuItem(MenuItems.addSeed, selectedSide != null);
		// Unimplemented items
		setMenuItem(MenuItems.showSharedMemory, false);
	}

	public void simulate() {
		// Create and start a game running thread. The thread stops
		// whenever world.running() becomes false.
		Thread gameThread = new Thread() {
			@Override
			public void run() {
				// Removing an active side causes a crash
				// setMenuItem(MenuItems.removeSide, false);
				// setMenuItem(MenuItems.removeAllSides, false);
				while (game.running) {
					long frameRate = 1000000000L / stepRate.value; // nanoseconds
																	// per
																	// frame

					if (System.nanoTime() > prevFrameTime + frameRate) {
						try {
							while (rendering > 0) {
								Thread.sleep(1);
							}
							running = true;
							game.advanceFrame();
							running = false;
							prevFrameTime = System.nanoTime();
						} catch (Exception e) {
							try {
								GBError.NonfatalError("Error simulating: "
										+ e.getMessage());
							} catch (GBAbort a) {
								// Retry
							}
						}
					}
				}
				updateMenu();
			}
		};
		gameThread.start();
	}

	void setMenuItem(MenuItems item, boolean state) {
		try {
			mainMenu.menuButtons.get(item).setEnabled(state);
		} catch (Exception e) {
			// Some menu options may not be mapped to buttons
		}

	}

	public Side getSelectedSide() {
		return selectedSide;
	}

	public void setSelectedType(RobotType _type) {
		if (_type == null || selectedType == null) {
			selectedType = _type;
			repaint();
			updateMenu();
			return;
		}
		if (!selectedType.equals(_type)) {
			selectedType = _type;
			repaint();
		}
		updateMenu();
	}

	public GBObject getSelectedObject() {
		return selectedObject;
	}

	public void setSelectedObject(GBObject _obj) {
		if (debug.isVisible()) {
			debug.setTarget(_obj);
			debug.setPreferredSize(new Dimension(debug.getWidth(), debug
					.getPreferredHeight()));
		}
		selectedObject = _obj;
		repaint();
	}

	public RobotType getSelectedType() {
		return selectedType;
	}

	public void setSelectedSide(Side _side) {
		if (_side == null) {
			selectedSide = _side;
			repaint();
			updateMenu();
			setSelectedType(null);
			return;
		} else if (!_side.equals(selectedSide)) {
			selectedSide = _side;
			setSelectedType(_side.types.size() > 0 ? _side.types.get(0) : null);
			repaint();
		}
		updateMenu();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Handle Menu click
		if (MenuItems.byDescription(e.getActionCommand()) != null) {
			MenuItems mi = MenuItems.byDescription(e.getActionCommand());
			try {
				switch (mi) {
				case addManna:
					portal.currentTool = toolTypes.ptAddManna;
					portal.setCursor(Cursor
							.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					break;
				case addRobot:
					portal.currentTool = toolTypes.ptAddRobot;
					portal.setCursor(Cursor
							.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					break;
				case addSeed:
					portal.currentTool = toolTypes.ptAddSeed;
					portal.setCursor(Cursor
							.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					break;
				case addSeeds:
					game.addSeeds();
					break;
				case autoFollow:
					portal.autofollow = true;
					portal.FollowRandom();
					break;
				case blasts:
					portal.currentTool = toolTypes.ptBlasts;
					portal.setCursor(Cursor
							.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					break;
				case clearMap:
					game.Reset();
					break;
				case duplicateSide:
					if (selectedSide == null)
						return;
					Side newside = SideReader.Load(selectedSide.filename);
					game.AddSide(newside);
					repaint();
					break;
				case erase:
					portal.currentTool = toolTypes.ptErase;
					portal.setCursor(Cursor
							.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					break;
				case erasearea:
					portal.currentTool = toolTypes.ptEraseBig;
					portal.setCursor(Cursor
							.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					break;
				case exit:
					if (JOptionPane.showConfirmDialog(null,
							"Are you sure you want to exit the game?",
							"Confirm Exit", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
						System.exit(0);
					break;
				case fast:
					stepRate = StepRates.fast;
					break;
				case firstPage:
					break;
				case followRandom:
					portal.FollowRandom();
					break;
				case gotoDocs:
					support.Toolbox.openWebpage(URI
							.create("http://grobots.sourceforge.net/docs/"));
					break;
				case gotoGroup:
					support.Toolbox
							.openWebpage(URI
									.create("http://groups.yahoo.com/neo/groups/grobots/info"));
					break;
				case gotoSides:
					support.Toolbox.openWebpage(URI
							.create("http://grobots.sourceforge.net/sides/"));
					break;
				case gotoWebsite:
					support.Toolbox.openWebpage(URI
							.create("http://grobots.sourceforge.net"));
					break;
				case gotoWiki:
					support.Toolbox
							.openWebpage(URI
									.create("http://grobots.wikia.com/wiki/Grobots_Wiki"));
					break;
				case loadSide:
					JFileChooser fc = new JFileChooser();
					fc.setMultiSelectionEnabled(true);
					// TODO default this to the app directory and only show .gb
					// files
					fc.setCurrentDirectory(new File("." + "\\src\\test\\sides"));
					int retval = fc.showOpenDialog(this);
					if (retval == JFileChooser.APPROVE_OPTION) {
						for (File f : fc.getSelectedFiles()) {
							try {
								Side _newside;
								_newside = SideReader.Load(f.getPath());
								game.AddSide(_newside);
							} catch (Exception fileEx) {
								GBError.NonfatalError(String.format(
										"%s could not be loaded.", f.getName()));
							}
						}
					}
					updateMenu();
					repaint();
					break;
				case move:
					portal.currentTool = toolTypes.ptMove;
					portal.setCursor(Cursor
							.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					break;
				case newRound:
					if (game.running) {
						game.running = false;
						Thread.sleep(100); // Let simulate thread end
					}
					game.Reset();
					game.addSeeds();
					game.running = true;
					repaint();
					simulate();
					break;
				case nextPage:
					break;
				case normal:
					stepRate = StepRates.normal;
					break;
				case pause:
					game.running = false;
					repaint();
					break;
				case play:
					if (running)
						break;
					if (game.getSidesSeeded() > 0) {
						game.running = true;
						simulate();
					} else {
						game.Reset();
						game.addSeeds();
						game.running = true;
						repaint();
						simulate();
					}
					break;
				case previousPage:
					break;
				case pull:
					portal.currentTool = toolTypes.ptPull;
					portal.setCursor(Cursor
							.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					break;
				case randomNear:
					portal.FollowRandomNear();
					break;
				case refollow:
					portal.Refollow();
					break;
				case reloadSide:
					if (getSelectedSide() == null)
						break;
					Side reload = SideReader.Load(selectedSide.filename);
					game.ReplaceSide(selectedSide, reload);
					roster.repaint();
					setSelectedSide(reload);
					break;
				case removeAllSides:
					game.Reset();
					game.RemoveAllSides();
					game.running = false;
					setSelectedSide(null);
					updateMenu();
					repaint();
					break;
				case removeSide:
					if (getSelectedSide() == null)
						break;
					game.RemoveSide(selectedSide);
					setSelectedSide(null);
					updateMenu();
					repaint();
					break;
				case reseedDeadSides:
					game.ReseedDeadSides();
					break;
				case resetScores:
					game.ResetTournamentScores();
					break;
				case rules:
					break;
				case run:
					if (!game.running) {
						game.running = true;
						simulate();
					}
					break;
				case saveScores:
					game.DumpTournamentScores(true);
					break;
				case scrollDown:
					break;
				case scrollFollow:
					portal.currentTool = toolTypes.ptScroll;
					portal.setCursor(Cursor
							.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					break;
				case scrollUp:
					break;
				case showAbout:
					if (aboutDialog == null) {
						aboutDialog = new JDialog(this, "About Grobots");
						aboutDialog.setResizable(false);
						about.repaint();
						aboutDialog.getContentPane().add(about);
						aboutDialog.pack();
						aboutDialog.setLocation(
								getWidth() / 2 - aboutDialog.getWidth() / 2,
								getHeight() / 2 - aboutDialog.getHeight() / 2);
					}
					aboutDialog.setVisible(true);
					break;
				case showDebugger:
					if (debugDialog == null) {
						debugDialog = new JDialog(this, "Debug");
						// TODO: do the setsize step when Followed() changes,
						// then make debug
						// not resizable again
						// debugDialog.setResizable(false);
						debug.repaint();
						debugDialog.getContentPane().add(debug);
						debugDialog.pack();
						debugDialog.setLocation(
								getWidth() / 2 - debugDialog.getWidth() / 2,
								getHeight() / 2 - debugDialog.getHeight() / 2);
					}
					debug.setSize(debug.getPreferredSize());
					debugDialog.pack();
					debugDialog.setVisible(true);
					break;
				case showDecorations:
					portal.showDecorations = mainMenu.viewOptions.get(
							ui.MenuItems.showDecorations).isSelected();
					break;
				case showMeters:
					portal.showDetails = mainMenu.viewOptions.get(
							ui.MenuItems.showMeters).isSelected();
					break;
				case showMiniMapTrails:
					break;
				case showMinimap:
					minimap.setVisible(!minimap.isVisible());
					setLayouts();
					break;
				case showPrints:
					game.reportPrints = mainMenu.viewOptions.get(
							ui.MenuItems.showPrints).isSelected();
					break;
				case showRobotErrors:
					game.reportErrors = mainMenu.viewOptions.get(
							ui.MenuItems.showRobotErrors).isSelected();
					break;
				case showRoster:
					roster.setVisible(!roster.isVisible());
					setLayouts();
					break;
				case showSensors:
					portal.showSensors = mainMenu.viewOptions.get(
							ui.MenuItems.showSensors).isSelected();
					break;
				case showSharedMemory:
					break;
				case showStatistics:
					statistics.setVisible(!statistics.isVisible());
					setLayouts();
					break;
				case showTournament:
					if (tournDialog == null) {
						tournDialog = new JDialog(this, "Tournament Scores");
						tournament.drawInBackground();
						tournDialog.getContentPane().add(tournament);
						tournDialog.pack();
						tournDialog.setLocation(
								getWidth() / 2 - tournDialog.getWidth() / 2,
								getHeight() / 2 - tournDialog.getHeight() / 2);
						tournDialog.setVisible(true);
					} else {
						tournament.drawInBackground();
						tournDialog.pack();
						tournDialog.setVisible(true);
					}
					break;
				case showTypes:
					type.setVisible(!type.isVisible());
					setLayouts();
					break;
				case singleFrame:
					game.advanceFrame();
					game.running = false;
					repaint();
					break;
				case slow:
					stepRate = StepRates.slow;
					break;
				case slowdown:
					if (stepRate.ordinal() > 0) {
						int i = stepRate.ordinal();
						stepRate = StepRates.values()[i - 1];
					}
					break;
				case smite:
					portal.currentTool = toolTypes.ptSmite;
					portal.setCursor(Cursor
							.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					break;
				case speedup:
					if (stepRate.ordinal() < StepRates.values().length - 1) {
						int i = stepRate.ordinal();
						stepRate = StepRates.values()[i + 1];
					}
					break;
				case stepBrain:
					GBObject obj = getSelectedObject();
					if (obj == null)
						return;
					if (obj instanceof GBRobot
							&& obj.Class() != GBObjectClass.ocDead) {
						game.running = false;
						Thread.sleep(100);
						GBRobot target = (GBRobot) obj;
						Brain brain = target.Brain();
						if (brain == null)
							return;
						if (!brain.Ready())
							return;
						brain.Step(target, game.getWorld());
						if (debug.isVisible())
							debug.repaint();
					}
					break;
				case stopStartBrain:
					if (getSelectedObject() == null)
						return;
					if (getSelectedObject() instanceof GBRobot
							&& getSelectedObject().Class() != GBObjectClass.ocDead) {
						Brain brain = ((GBRobot) getSelectedObject()).Brain();
						if (brain == null)
							return;
						brain.status = brain.status == BrainStatus.bsOK ? BrainStatus.bsStopped
								: BrainStatus.bsOK;
						if (debug.isVisible())
							debug.repaint();
					}
					break;
				case tournament:
					game.tournament = mainMenu.cbTournament.isSelected();
					game.tournamentLength = -1;
					break;
				case unlimited:
					stepRate = StepRates.unlimited;
					break;
				case zoomIn:
					portal.doZoom(1);
					break;
				case zoomOut:
					portal.doZoom(-1);
					break;
				case zoomStandard:
					portal.scale = GBPortal.kScale;
					break;
				default:
					break;
				}
			} catch (Exception err) {
				try {
					GBError.NonfatalError(err.getMessage());
				} catch (GBAbort e1) {
					// Retry
				} catch (Exception ex) {
					GBError.FatalError(ex.getMessage());
				}
			}
		}
	}

	public void setViewWindow(FinePoint p) {
		portal.setViewWindow(p);
	}

	public void setVisibleWorld(Rectangle r) {
		minimap.setVisibleWorld(r);
	}
}
