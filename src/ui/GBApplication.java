package ui;

import java.awt.BorderLayout;
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
import javax.swing.SwingUtilities;

import sides.RobotType;
import sides.Side;
import sides.SideReader;
import simulation.GBWorld;
import views.GBPortal;
import views.GBRosterView;
import views.GBPortal.toolTypes;
import exception.GBAbort;
import exception.GBBadArgumentError;
import exception.GBError;
import exception.GBNilPointerError;

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

	public static void main(String[] args) {
		// Why this doesn't work, I have no idea. Sick of messing with it.
		// SwingUtilities.invokeLater(new GBApplication());
		// I know this works:
		new GBApplication().run();
	}

	public GBApplication() {
		world = new GBWorld();
		portal = new GBPortal(this);
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
		javax.swing.Timer refreshTimer = new javax.swing.Timer(40, new ActionListener(){
			public void actionPerformed(ActionEvent e){
				repaint();
				//lastStep = System.currentTimeMillis();
			}
		});
		refreshTimer.setRepeats(true);
		refreshTimer.setCoalesce(true);
		refreshTimer.start();
	}

	@Override
	public void run() {
		while (true) {
			if (!world.running) {
				// lastStep += 1000; // hack to prevent taking so much time when
				// paused at Unlimited speed
			}
			if (System.currentTimeMillis() > lastTime + 1000L){
				lastTime = System.currentTimeMillis();
				stepCount = 0;
			}
			try {
				while (world.running
						&& (stepRate.value >= stepCount)
						&& System.currentTimeMillis() <= lastTime
								+ 1000L) {
					world.AdvanceFrame();
					++stepCount;
				}
			} catch (GBError err) {
				try {
					GBError.NonfatalError("Error simulating: " + err.toString());
				} catch (GBAbort e) {
					// Retry
				}
			}
		}
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
					int retval = fc.showOpenDialog(this);
					if (retval == JFileChooser.APPROVE_OPTION) {
						//JOptionPane.showMessageDialog(this,
						//		fc.getSelectedFiles());
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
					lastTime = System.currentTimeMillis();
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
