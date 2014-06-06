package views;

import javax.swing.JPanel;

import simulation.GBWorld;

public class GBPortal extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -861108527551257687L;
	GBWorld world;

	public GBPortal(GBWorld _world) {
		world = _world;
	}
}
