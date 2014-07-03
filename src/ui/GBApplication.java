package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import sides.RobotType;
import sides.Side;
import sides.SideReader;
import simulation.GBWorld;
import views.GBPortal;
import views.GBPortal.toolTypes;
import views.GBRosterView;
import views.GBTournamentView;
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
	
	//Views
	public GBPortal portal;
	public GBRosterView roster;
	public GBTournamentView tournament;
	
	public StepRates stepRate;
	public long lastTime;
	int redrawInterval = 40;// Repaint at 25Hz
	GBMenu mainMenu;
	public Side selectedSide;
	public RobotType selectedType;

	long prevFrameTime;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new GBApplication());
	}

	public GBApplication() {
		// Creation code found in run() method per recommended java Swing
		// practices
	}

	@Override
	public void run() {
		//Create world and initial conditions
		world = new GBWorld();
		stepRate = StepRates.normal;
		
		//Create supporting views and menu
		portal = new GBPortal(this);
		portal.setIgnoreRepaint(true);
		roster = new GBRosterView(this);
		tournament = new GBTournamentView(this);
		mainMenu = new GBMenu(this);
		this.setJMenuBar(mainMenu);
		
		//Arrange the screen
		this.setLayout(new BorderLayout());
		setExtendedState( getExtendedState()|JFrame.MAXIMIZED_BOTH );
		Image icon = new ImageIcon(getClass().getResource("grobots 32x32.png"))
				.getImage();
		setIconImage(icon);
		this.setTitle("Grobots");
		this.getContentPane().add(portal, BorderLayout.CENTER);
		portal.setPreferredSize(new Dimension(600,400));
		JPanel bottom = new JPanel();
		bottom.setLayout(new FlowLayout());
		bottom.add(roster);
		bottom.add(tournament);
		Color backColor = Color.gray;
		portal.setBackground(backColor);
		bottom.setBackground(backColor);
		this.getContentPane().add(bottom, BorderLayout.PAGE_END);
		roster.setPreferredSize(new Dimension(270,260));
		tournament.setPreferredSize(new Dimension(560,260));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		setVisible(true);
		
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
					}
				});
		otherTimer.setRepeats(true);
		otherTimer.setCoalesce(true);
		otherTimer.start();
	}

	public void simulate() {
		// Create and start a game running thread. The thread stops
		// whenever world.running() becomes false.
		Thread gameThread = new Thread() {
			@Override
			public void run() {
				while (world.running) {
					long frameRate = 1000000000L / stepRate.value; // nanoseconds
																	// per
																	// frame

					if (System.nanoTime() > prevFrameTime + frameRate) {
						try {
							world.AdvanceFrame();
							prevFrameTime = System.nanoTime();
						} catch (GBError err) {
							try {
								GBError.NonfatalError("Error simulating: "
										+ err.toString());
							} catch (GBAbort e) {
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
			}
		};
		gameThread.start();
	}

	void enableMenuItem(MenuItems item) {
		mainMenu.menuButtons.get(item).setEnabled(true);
	}

	void disableMenuItem(MenuItems item) {
		mainMenu.menuButtons.get(item).setEnabled(false);
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
					Side newside = SideReader.Load(selectedSide.filename);
					world.AddSide(newside);
					roster.repaint();
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
				case loadSide:
					JFileChooser fc = new JFileChooser();
					fc.setMultiSelectionEnabled(true);
					// TODO default this to the app directory and only show .gb
					// files
					fc.setCurrentDirectory(new File("." + "\\src\\test\\sides"));
					int retval = fc.showOpenDialog(this);
					if (retval == JFileChooser.APPROVE_OPTION) {
						// JOptionPane.showMessageDialog(this,
						// fc.getSelectedFiles());
						for (File f : fc.getSelectedFiles()) {
							Side _newside;
							_newside = SideReader.Load(f.getPath());
							world.AddSide(_newside);
						}
					}
					roster.repaint();
					break;
				case move:
					portal.currentTool = toolTypes.ptMove;
					portal.setCursor(Cursor
							.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					break;
				case newRound:
					world.Reset();
					world.AddSeeds();
					world.running = true;
					roster.repaint();
					simulate();
					break;
				case nextPage:
					break;
				case normal:
					stepRate = StepRates.normal;
					break;
				case pause:
					world.running = false;
					roster.repaint();
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

					break;
				case removeAllSides:
					world.Reset();
					world.RemoveAllSides();
					world.running = false;
					roster.repaint();
					break;
				case removeSide:
					world.Sides().remove(selectedSide);
					roster.repaint();
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
					break;
				case showSensors:
					portal.showSensors = mainMenu.viewOptions.get(
							ui.MenuItems.showSensors).isSelected();
					break;
				case showSharedMemory:
					break;
				case showStatistics:
					break;
				case showTournament:
					tournament.setVisible(!tournament.isVisible());
					break;
				case showTypes:
					break;
				case singleFrame:
					world.AdvanceFrame();
					world.running = false;
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
					world.tournamentLength = 100;
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
			} catch (GBError | IOException err) {
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
