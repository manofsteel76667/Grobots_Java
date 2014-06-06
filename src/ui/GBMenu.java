package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import simulation.GBWorld;


public class GBMenu extends JMenuBar implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3330599322475052070L;
	public GBApplication app;
	JMenu fileMenu;
	JMenu windowMenu;
	JMenu simulationMenu;

	public GBMenu(GBApplication _app) {
		app = _app;
		buildFileMenu();
		buildWindowMenu();
		buildSimulationMenu();
	}

	void buildFileMenu() {
		JMenu ret = new JMenu("File");
		this.add(ret);
	}

	void buildWindowMenu() {
		JMenu ret = new JMenu("Window");
		JMenuItem mi = new JMenuItem("Roster");
		mi.addActionListener(this);
		ret.add(mi);
		this.add(ret);
	}

	void buildSimulationMenu() {
		JMenu ret = new JMenu("Simulation");
		
		this.add(ret);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch(e.getActionCommand()){
			default:
				break;
		}
	}
}
