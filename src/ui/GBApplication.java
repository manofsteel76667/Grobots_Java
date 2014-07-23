/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package ui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import sides.RobotType;
import sides.Side;
import sides.SideReader;
import simulation.GBWorld;
import views.AboutBox;
import views.GBPortal;
import views.GBPortal.toolTypes;
import views.GBRosterView;
import views.GBScoresView;
import views.GBTournamentView;
import views.RobotTypeView;
import exception.GBAbort;
import exception.GBError;

enum StepRates {
	slow(10), normal(30), fast(60), unlimited(10000);
	public final int value;

	StepRates(int val) {
		value = val;
	}
}

public class GBApplication extends JFrame implements Runnable, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -955217075865755516L;
	public GBWorld world;

	// Views
	GBPortal portal;
	GBRosterView roster;
	GBTournamentView tournament;
	GBScoresView statistics;
	AboutBox about;
	RobotTypeView type;
	JDialog tournDialog;
	JDialog aboutDialog;

	public StepRates stepRate;
	public long lastTime;
	int redrawInterval = 40;// Repaint at 25Hz
	GBMenu mainMenu;

	long prevFrameTime;
	Side selectedSide;
	RobotType selectedType;

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
		world = new GBWorld();
		stepRate = StepRates.fast;

		// Create supporting views and menu.  
		createChildViews();
		mainMenu = new GBMenu(this);
		this.setJMenuBar(mainMenu);
		updateMenu();

		// Arrange the screen
		this.getContentPane().setLayout(new GridBagLayout());
		setLayouts();
		Image icon = new ImageIcon(getClass().getResource("grobots 32x32.png"))
				.getImage();
		setIconImage(icon);
		this.setTitle("Grobots");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
		setVisible(true);
		statistics.setVisible(false);

		javax.swing.Timer portalTimer = new javax.swing.Timer(redrawInterval,
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (portal.isVisible())
							portal.repaint();
					}
				});
		portalTimer.setRepeats(true);
		portalTimer.setCoalesce(true);
		portalTimer.start();
		javax.swing.Timer otherTimer = new javax.swing.Timer(1500,
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (roster.isVisible())
							roster.repaint();
						if (tournament.isVisible())
							tournament.repaint();
						if (type.isVisible())
							type.repaint();
						if (statistics.isVisible())
							statistics.repaint();
					}
				});
		otherTimer.setRepeats(true);
		otherTimer.setCoalesce(true);
		otherTimer.start();
	}
	
	void setLayouts(){
		for (Component c : this.getContentPane().getComponents())
			this.getContentPane().remove(c);
		GridBagConstraints c = new GridBagConstraints();
				
		//Roster
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 1;
		c.weightx = 0;
		c.gridheight = 2;
		this.getContentPane().add(roster, c);
		
		//Portal
		portal.setPreferredSize(new Dimension(1, 1));
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.ABOVE_BASELINE;
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1;
		c.gridheight = 1;
		this.getContentPane().add(portal, c);
		
		//Statistics
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.BELOW_BASELINE;
		c.gridx = 1;
		c.gridy = 2;
		c.weightx = 0;
		c.weighty = 0;
		c.gridheight = 1;
		c.gridwidth = 1;
		this.getContentPane().add(statistics, c);
		
		//Type View
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.ABOVE_BASELINE;
		c.gridx = 2;
		c.gridy = 0;
		c.weighty = 0;
		c.gridwidth = 1;
		c.gridheight = 3;
		this.getContentPane().add(type, c);				
	}

	void createChildViews() {
		about = new AboutBox();
		about.setPreferredSize(new Dimension(270, 290));
		portal = new GBPortal(this);
		roster = new GBRosterView(this);
		tournament = new GBTournamentView(this);
		type = new RobotTypeView(this);
		statistics = new GBScoresView(this);
	}

	void updateMenu() {
		setMenuItem(MenuItems.removeAllSides, world.Sides().size() > 0
				&& !world.running);
		setMenuItem(MenuItems.reloadSide, selectedSide != null
				&& !world.running);
		setMenuItem(MenuItems.duplicateSide, selectedSide != null
				&& !world.running);
		setMenuItem(MenuItems.removeSide, selectedSide != null
				&& !world.running);
		setMenuItem(MenuItems.addRobot, selectedType != null);
		setMenuItem(MenuItems.addSeed, selectedSide != null);
		//Unimplemented items
		setMenuItem(MenuItems.showDebugger, false);
		setMenuItem(MenuItems.showMinimap, false);
		setMenuItem(MenuItems.showSharedMemory, false);
	}

	public void simulate() {
		// Create and start a game running thread. The thread stops
		// whenever world.running() becomes false.
		Thread gameThread = new Thread() {
			@Override
			public void run() {
				// Removing an active side causes a crash
				//setMenuItem(MenuItems.removeSide, false);
				//setMenuItem(MenuItems.removeAllSides, false);
				while (world.running) {
					long frameRate = 1000000000L / stepRate.value; // nanoseconds
																	// per
																	// frame

					if (System.nanoTime() > prevFrameTime + frameRate) {
						try {
							world.AdvanceFrame();
							prevFrameTime = System.nanoTime();
						} catch (Exception e) {
							try {
								GBError.NonfatalError("Error simulating: "
										+ e.toString());
							} catch (GBAbort a) {
								// Retry
							}
						}
					} else
						try {
							// Sleep until next time
							long snooze = prevFrameTime + frameRate
									- System.nanoTime();
							if (snooze > 0)
								Thread.sleep(snooze / 1000000);
						} catch (InterruptedException e) {
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
					world.AddSeeds();
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
					world.Reset();
					break;
				case duplicateSide:
					if (selectedSide == null)
						return;
					Side newside = SideReader.Load(selectedSide.filename);
					world.AddSide(newside);
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
					support.Toolbox.openWebpage(URI.create("http://grobots.sourceforge.net/docs/"));
					break;
				case gotoGroup:
					support.Toolbox.openWebpage(URI.create("http://groups.yahoo.com/neo/groups/grobots/info"));
					break;
				case gotoSides:
					support.Toolbox.openWebpage(URI.create("http://grobots.sourceforge.net/sides/"));
					break;
				case gotoWebsite:
					support.Toolbox.openWebpage(URI.create("http://grobots.sourceforge.net"));
					break;
				case gotoWiki:
					support.Toolbox.openWebpage(URI.create("http://grobots.wikia.com/wiki/Grobots_Wiki"));
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
							Side _newside;
							_newside = SideReader.Load(f.getPath());
							world.AddSide(_newside);
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
					if (world.running){
						world.running = false;
						Thread.sleep(100); //Let simulate thread end
					}
					world.Reset();
					world.AddSeeds();
					world.running = true;
					repaint();
					simulate();
					break;
				case nextPage:
					break;
				case normal:
					stepRate = StepRates.normal;
					break;
				case pause:
					world.running = false;
					repaint();
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
					if (selectedSide == null)
						break;
					Side reload = SideReader.Load(selectedSide.filename);
					world.ReplaceSide(selectedSide, reload);
					roster.repaint();
					setSelectedSide(reload);
					break;
				case removeAllSides:
					world.Reset();
					world.RemoveAllSides();
					world.running = false;
					setSelectedSide(null);
					updateMenu();
					repaint();
					break;
				case removeSide:
					world.RemoveSide(selectedSide);
					setSelectedSide(null);
					updateMenu();
					repaint();
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
					simulate();
					break;
				case saveScores:
					world.DumpTournamentScores(true);
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
					break;
				case showPrints:
					world.reportPrints = mainMenu.viewOptions.get(
							ui.MenuItems.showPrints).isSelected();
					break;
				case showRobotErrors:
					world.reportErrors = mainMenu.viewOptions.get(
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
					world.AdvanceFrame();
					world.running = false;
					repaint();
					break;
				case slow:
					stepRate = StepRates.slow;
					break;
				case smite:
					portal.currentTool = toolTypes.ptSmite;
					portal.setCursor(Cursor
							.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					break;
				case stepBrain:
					break;
				case stopStartBrain:
					break;
				case tournament:
					world.tournament = mainMenu.cbTournament.isSelected();
					world.tournamentLength = -1;
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
