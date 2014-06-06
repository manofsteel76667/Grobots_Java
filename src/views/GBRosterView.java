package views;

import javax.swing.JPanel;

import simulation.GBWorld;

public class GBRosterView extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6135247814368773456L;
	GBWorld world;

	public GBRosterView(GBWorld _world) {
		world = _world;
	}
}
