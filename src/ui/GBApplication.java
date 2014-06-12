package ui;

import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import sides.RobotType;
import sides.Side;
import simulation.GBWorld;
import views.GBPortal;
import views.GBRosterView;
import exception.GBAbort;
import exception.GBBadArgumentError;
import exception.GBError;
import exception.GBNilPointerError;

enum StepRates {
	slow(100), normal(33), fast(17), unlimited(1);
	public final int value;

	StepRates(int val) {
		value = val;
	}
}

public class GBApplication extends JFrame implements Runnable {
	public static void main(String[] args) {
		try {
			new GBApplication().run();
		} catch (GBError e) {
			try {
				GBError.NonfatalError(e.toString());
			} catch (GBAbort e1) {
				System.exit(1);
			}
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -955217075865755516L;
	public GBWorld world;
	public GBPortal portal;
	public GBRosterView roster;
	public StepRates stepRate;
	public long lastStep;
	GBMenu mainMenu;
	public Side selectedSide;
	public RobotType selectedType;

	public GBApplication() throws GBNilPointerError, GBBadArgumentError {
		world = new GBWorld();
		portal = new GBPortal(this);
		roster = new GBRosterView(world);
		stepRate = StepRates.normal;
		this.setJMenuBar(new GBMenu(this));
		Image icon = new ImageIcon(getClass().getResource("grobots 32x32.png"))
				.getImage();
		setIconImage(icon);
		this.setTitle("Grobots");
		this.getContentPane().add(portal);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setLocation(200, 100);
		setVisible(true);
	}

	@Override
	public void run() {
		while (true) {
			lastStep = System.currentTimeMillis();
			if (!world.running) {
				lastStep += 1000; // hack to prevent taking so much time when
									// paused at Unlimited speed
			}
			try {
				int steps = 0;
				//TODO: check how this works; seems more complex than it should be
				while (world.running
						&& (stepRate.value <= 0 || stepRate.value <= 10
								&& steps < 3L)
						&& System.currentTimeMillis() <= lastStep + 50L) {
					world.AdvanceFrame();
					++steps;
				}
			} catch (GBError err) {
				try {
					GBError.NonfatalError("Error simulating: " + err.toString());
				} catch (GBAbort e) {
					System.exit(1);
				}
			}
		}
	}
}
