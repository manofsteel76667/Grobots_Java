package ui;

import javax.swing.*;

import exception.GBBadArgumentError;
import exception.GBNilPointerError;
import simulation.*;
import views.*;

import java.awt.event.KeyEvent;
import java.util.*;

enum StepRates{
	slow(100), normal(33), fast(17);
	public final int value;
	StepRates(int val){
		value = val;
	}
}
public class GBApplication extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -955217075865755516L;
	public GBWorld world;
	public GBPortal portal;
	public GBRosterView roster;
	public StepRates stepRate;
	GBMenu mainMenu;

	public GBApplication() throws GBNilPointerError, GBBadArgumentError {
		world = new GBWorld();
		portal = new GBPortal(world);
		roster = new GBRosterView(world);
		stepRate = StepRates.normal;
		this.setJMenuBar(new GBMenu(this));
		setContentPane(portal);
	}
}
