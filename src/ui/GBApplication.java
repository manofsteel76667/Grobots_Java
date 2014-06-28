package ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import sides.RobotType;
import sides.Side;
import sides.SideReader;
import simulation.GBWorld;
import views.GBPortal;
import views.GBPortal.toolTypes;
import views.GBRosterView;
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
	public GBPortal portal;
	public GBRosterView roster;
	public StepRates stepRate;
	public long lastTime;
	public long redrawInterval = 20L;
	GBMenu mainMenu;
	public Side selectedSide;
	public RobotType selectedType;
	int stepCount;
	long updateCount;
	BufferStrategy bs;
	long prevFrameTime;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new GBApplication());
	}

	public GBApplication() {
		world = new GBWorld();
		portal = new GBPortal(this);
		portal.setIgnoreRepaint(true);
		roster = new GBRosterView(world);
		stepRate = StepRates.normal;
		mainMenu = new GBMenu(this);
		stepCount = 0;
		this.setLayout(new BorderLayout());
		this.setJMenuBar(mainMenu);
		Image icon = new ImageIcon(getClass().getResource("grobots 32x32.png"))
				.getImage();
		setIconImage(icon);
		this.setTitle("Grobots");
		this.setContentPane(portal);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		setVisible(true);
		createBufferStrategy(2);
		bs = getBufferStrategy();
		javax.swing.Timer refreshTimer = new javax.swing.Timer(40,
		// Repaint the world at 25Hz
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Graphics2D g = (Graphics2D) bs.getDrawGraphics();
						paint(g);
						portal.draw(g);
						bs.show();
						Toolkit.getDefaultToolkit().sync();
						g.dispose();
					}
				});
		refreshTimer.setRepeats(true);
		refreshTimer.setCoalesce(true);
		refreshTimer.start();
	}

	@Override
	public void run() {
		// Create and start a game running thread. The thread stops and
		// must be recreated whenever world.running() becomes false
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
							Thread.sleep((prevFrameTime + frameRate - System
									.nanoTime()) / 1000000);
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
					break;
				case blasts:
					portal.currentTool = toolTypes.ptBlasts;
					portal.setCursor(Cursor
							.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					break;
				case clearMap:
					break;
				case duplicateSide:
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
					break;
				case loadSide:
					JFileChooser fc = new JFileChooser();
					fc.setMultiSelectionEnabled(true);
					fc.setCurrentDirectory(new File("." + "\\src\\test\\sides"));
					int retval = fc.showOpenDialog(this);
					if (retval == JFileChooser.APPROVE_OPTION) {
						// JOptionPane.showMessageDialog(this,
						// fc.getSelectedFiles());
						for (File f : fc.getSelectedFiles()) {
							Side newside;
							newside = SideReader.Load(f.getPath());
							world.AddSide(newside);
						}
					}
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
					run();
					break;
				case nextPage:
					break;
				case normal:
					stepRate = StepRates.normal;
					break;
				case pause:
					world.running = false;
					break;
				case previousPage:
					break;
				case pull:
					portal.currentTool = toolTypes.ptPull;
					portal.setCursor(Cursor
							.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
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
					run();
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
					portal.showDecorations = !portal.showDecorations;
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
					roster.setVisible(true);
					break;
				case showSensors:
					portal.showSensors = !portal.showSensors;
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
					world.tournament = !world.tournament;
					world.tournamentLength = 100;
					break;
				case unlimited:
					stepRate = StepRates.unlimited;
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
					// Retry
				} catch (Exception ex) {
					GBError.FatalError(ex.getMessage());
				}
			}
		}
	}

}
