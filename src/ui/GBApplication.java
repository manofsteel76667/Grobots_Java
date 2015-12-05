/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import sides.RobotType;
import sides.Side;
import simulation.GBGame;
import simulation.GBObject;
import simulation.GBObjectClass;
import simulation.GBRobot;
import sound.SoundManager;
import sound.SoundManager.SoundType;
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
import exception.GBSimulationError;

public class GBApplication extends JFrame implements Runnable, ActionListener,
		TypeSelectionListener, SideSelectionListener, ObjectSelectionListener {
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
	JToolBar debugControls;
	Debugger debug;

	public enum StepRates {
		slow(10), normal(30), fast(60), unlimited(-1);
		public final int value;

		StepRates(int val) {
			value = val;
		}
	}

	public StepRates stepRate;
	public long lastTime;
	int fastInterval = 20;// Repaint at 50Hz
	int slowInterval = 1500;

	GBMenu mainMenu;

	Side selectedSide;
	RobotType selectedType;
	GBObject selectedObject;

	int rendering; // Rendering, don't run a turn
	boolean running; // Running a turn, don't render

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new GBApplication());
	}

	public GBApplication() {
		// Global settings
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		SoundManager.setMuted(false);
		// This does nothing but will call the static initializer to preload all
		// the sounds
		SoundType.stBeep.getClass();
	}

	@Override
	public void run() {
		// Create world and initial conditions
		game = new GBGame();
		stepRate = StepRates.fast;

		// Create supporting views and menu.
		mainMenu = new GBMenu(this);
		this.setJMenuBar(mainMenu);
		createChildViews();

		// Arrange the screen
		List<BufferedImage> icons = new ArrayList<BufferedImage>();
		String[] resources = new String[] { "grobots16.png", "grobots32.png",
				"grobots48.png", "grobots64.png", "grobots128.png" };
		for (String s : resources) {
			try {
				icons.add(ImageIO.read(getClass().getResourceAsStream(s)));
			} catch (IOException e1) {
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
		updateMenu();
	}

	void createTimers() {
		javax.swing.Timer fastTimer = new javax.swing.Timer(fastInterval,
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (!running) {
							drawFastPanels();
						} // else
							// game.isFastDrawRequested = true;
					};
				});
		fastTimer.setRepeats(true);
		fastTimer.setCoalesce(true);
		fastTimer.start();
		javax.swing.Timer slowTimer = new javax.swing.Timer(slowInterval,
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (!running) {
							drawSlowPanels();
						} 
					}
				});
		slowTimer.setRepeats(true);
		slowTimer.setCoalesce(true);
		slowTimer.start();
	}

	public void drawFastPanels() {
		rendering++;
		if (portal.isVisible())
			portal.repaint();
		if (minimap.isVisible())
			minimap.repaint();
		if (debug.isVisible())
			debug.repaint();
		rendering--;
	}

	public void drawSlowPanels() {
		rendering++;
		if (roster.isVisible()) {
			roster.recalculate();
			roster.repaint();
		}
		if (tournament.isVisible())
			tournament.update();
		if (type.isVisible())
			type.repaint();
		if (statistics.isVisible()) {
			statistics.updateScores();
			statistics.repaint();
		}
		rendering--;
	}

	void setLayouts() {
		// 3 Child panels
		JPanel children = new JPanel();
		children.setLayout(new BoxLayout(children, BoxLayout.X_AXIS));
		JPanel left = new JPanel();
		left.setPreferredSize(new Dimension(270, 500));
		left.setMaximumSize(new Dimension(200, 800));
		left.setBorder(BorderFactory.createLineBorder(getForeground(), 1));
		JPanel center = new JPanel();
		JPanel right = new JPanel();
		children.add(left);
		children.add(center);
		children.add(right);
		add(children);

		// Left pane
		left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
		left.add(roster);
		left.add(minimap);

		// Center pane
		JPanel portalpanel = new JPanel();
		portalpanel.setLayout(new BorderLayout());
		portalpanel.add(portal);

		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
		center.add(portalpanel);
		center.add(statistics);

		// Right pane
		right.setLayout(new BorderLayout());
		right.add(type);
		right.setMaximumSize(new Dimension(270, 800));
		right.setBorder(BorderFactory.createLineBorder(getForeground(), 1));

		// Toolbar
		JToolBar main = new JToolBar();
		main.setFloatable(false);
		JToolBar fileControls = mainMenu.fileToolBar(this);
		fileControls.setFloatable(false);
		for (Component btn : fileControls.getComponents())
			if (btn instanceof JButton) {
				((JButton) btn).setBorder(BorderFactory.createLineBorder(
						Color.gray, 1, false));
			}
		main.add(fileControls);
		JToolBar portalControls = mainMenu.simToolbar(this);
		portalControls.setFloatable(false);
		for (Component btn : portalControls.getComponents())
			if (btn instanceof JButton)
				((JButton) btn).setBorder(BorderFactory.createLineBorder(
						Color.gray, 1, false));
		main.add(portalControls);
		add(main, BorderLayout.NORTH);
	}

	void createChildViews() {
		about = new AboutBox();
		about.setPreferredSize(new Dimension(270, 290));
		portal = new GBPortal(game, false);
		minimap = new GBPortal(game, true);
		minimap.setPreferredSize(new Dimension(200, 200));
		roster = new GBRosterView(game);
		tournament = new GBTournamentView(game);
		type = new RobotTypeView();
		type.setPreferredSize(new Dimension(340, 300));
		statistics = new GBScoresView(game);
		debugControls = mainMenu.debugToolbar(this);
		debugControls.setFloatable(false);
		debug = new Debugger(debugControls);
		// Wire up all the listeners and selectors
		// Portal and minimap update each other
		minimap.addPortalListener(portal);
		portal.addPortalListener(minimap);
		portal.addPortalListener(SoundManager.getManager());
		// Portal can select anything
		portal.addSideSelectionListener(roster);
		portal.addSideSelectionListener(type);
		portal.addSideSelectionListener(statistics);
		portal.addSideSelectionListener(this);
		portal.addObjectSelectionListener(debug);
		portal.addObjectSelectionListener(this);
		portal.addTypeSelectionListener(type);
		portal.addTypeSelectionListener(this);
		// Roster selects sides
		roster.addSideSelectionListener(portal);
		roster.addSideSelectionListener(this);
		roster.addSideSelectionListener(type);
		roster.addSideSelectionListener(statistics);
		// Type view selects types
		type.addTypeSelectionListener(this);
		type.addTypeSelectionListener(portal);
		// Game sets selected object to null at the end of each round
		// to clean the graphics up, but no one else cares.
		game.addObjectSelectionListener(portal);
		game.addObjectSelectionListener(debug);
	}

	void updateMenu() {
		MenuItems.removeAllSides.setEnabled(game.sides.size() > 0);
		MenuItems.reloadSide.setEnabled(selectedSide != null);
		MenuItems.duplicateSide.setEnabled(selectedSide != null);
		MenuItems.removeSide.setEnabled(selectedSide != null);
		MenuItems.addRobot.setEnabled(selectedType != null);
		MenuItems.addSeed.setEnabled(selectedSide != null);
		// Unimplemented items
		setMenuItem(MenuItems.showSharedMemory, false);
	}

	public void simulate() {
		// Create and start a game running thread. The thread stops
		// whenever world.running() becomes false.
		Thread gameThread = new Thread() {
			@Override
			public void run() {
				running = true;
				long prevFrameTime = System.currentTimeMillis();
				long prevFastDrawTime = System.currentTimeMillis();
				long prevSlowDrawTime = System.currentTimeMillis();
				while (game.running) {
					long frameRate = 1000L / stepRate.value;
					if (System.currentTimeMillis() >= prevFrameTime + frameRate) {
						if (rendering == 0) {
							try {
								game.advanceFrame();
								prevFrameTime = System.currentTimeMillis();
							} catch (GBSimulationError e) {
								try {
									GBError.NonfatalError("Error simulating: "
											+ e.getMessage());
								} catch (GBAbort a) {
									// Retry
								}
							} catch (Exception e) {
								try {
									GBError.NonfatalError("Java Error: "
											+ e.getMessage()
											+ "\nTrace:\n"
											+ Arrays.toString(e.getStackTrace()));
								} catch (GBAbort a) {
									// Retry
								}
							}
						}
					} else
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					try {
						if (System.currentTimeMillis() >= prevFastDrawTime
								+ fastInterval) {
							SwingUtilities.invokeAndWait(new Runnable() {
								@Override
								public void run() {
									drawFastPanels();
								}
							});
							prevFastDrawTime = System.currentTimeMillis();
						}
						if (System.currentTimeMillis() >= prevSlowDrawTime
								+ slowInterval) {
							SwingUtilities.invokeAndWait(new Runnable() {
								@Override
								public void run() {
									drawSlowPanels();
								}
							});
							prevSlowDrawTime = System.currentTimeMillis();
						}
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				running = false;
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

	@Override
	public void setSelectedType(RobotType _type) {
		selectedType = _type;
	}

	@Override
	public void setSelectedObject(GBObject _obj) {
		selectedObject = _obj;
		if (_obj != null)
			setSelectedSide(_obj.getOwner());
		if (_obj instanceof GBRobot)
			setSelectedType(((GBRobot) _obj).getRobotType());
		else
			setSelectedType(null);
	}

	@Override
	public void setSelectedSide(Side _side) {
		if (_side == null) {
			selectedSide = _side;
			updateMenu();
			selectedType = null;
			return;
		} else if (!_side.equals(selectedSide)) {
			selectedSide = _side;
			setSelectedType(_side.types.size() > 0 ? _side.types.get(0) : null);
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
					game.reset();
					break;
				case duplicateSide:
					roster.duplicateSide();
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
					roster.loadSide();
					updateMenu();
					repaint();
					break;
				case move:
					portal.currentTool = toolTypes.ptMove;
					portal.setCursor(Cursor
							.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					break;
				case mute:
					SoundManager.setMuted(!SoundManager.getMuted());
					URL imageURL = getClass().getResource(
							!SoundManager.getMuted() ? "unmute.png"
									: "mute.png");
					mi.setIcon(new ImageIcon(imageURL, "Mute/unmute"));
					break;
				case newRound:
					if (game.running) {
						game.running = false;
						Thread.sleep(100); // Let simulate thread end
					}
					game.reset();
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
						game.reset();
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
					if (!running)
						roster.reloadSide();
					break;
				case removeAllSides:
					roster.removeAllSides();
					updateMenu();
					repaint();
					break;
				case removeSide:
					roster.removeSide();
					updateMenu();
					repaint();
					break;
				case reseedDeadSides:
					game.reseedDeadSides();
					break;
				case resetScores:
					game.resetTournamentScores();
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
					game.dumpTournamentScores(true);
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
						debugDialog.setResizable(false);
						debugDialog.getContentPane().add(debug);
						debugDialog.pack();
					}
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
					game.setReportPrints(mainMenu.viewOptions.get(
							ui.MenuItems.showPrints).isSelected());
					break;
				case showRobotErrors:
					game.setReportErrors(mainMenu.viewOptions.get(
							ui.MenuItems.showRobotErrors).isSelected());
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
					statistics.updateScores();
					statistics.setVisible(!statistics.isVisible());
					setLayouts();
					break;
				case showTournament:
					// if (tournDialog == null) {
					tournDialog = new JDialog(this, "Tournament Scores");
					// tournament.drawInBackground();
					tournDialog.getContentPane().add(tournament);
					tournDialog.pack();
					tournDialog.setLocation(
							getWidth() / 2 - tournDialog.getWidth() / 2,
							getHeight() / 2 - tournDialog.getHeight() / 2);
					tournDialog.setVisible(true);
					// } else {
					// tournament.drawInBackground();
					// tournDialog.pack();
					// tournDialog.setVisible(true);
					// }
					// tournament.setVisible(true);
					break;
				case showTypes:
					type.setVisible(!type.isVisible());
					setLayouts();
					break;
				case singleFrame:
					game.advanceFrame();
					game.running = false;
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
					// if (stepRate == StepRates.unlimited)
					// SoundManager.mute();
					break;
				case stepBrain:
					GBObject obj = selectedObject;
					if (obj == null)
						return;
					if (obj instanceof GBRobot
							&& obj.getObjectClass() != GBObjectClass.ocDead) {
						if (game.running) {
							game.running = false;
							Thread.sleep(100);
						}
						GBRobot target = (GBRobot) obj;
						Brain brain = target.getBrain();
						if (brain == null)
							return;
						if (!brain.ready())
							return;
						brain.step(target, game.getWorld());
						if (debug.isVisible())
							debug.repaint();
					}
					break;
				case stopStartBrain:
					if (selectedObject == null)
						return;
					if (selectedObject instanceof GBRobot
							&& selectedObject.getObjectClass() != GBObjectClass.ocDead) {
						Brain brain = ((GBRobot) selectedObject).getBrain();
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
}
