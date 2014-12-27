/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package views;

import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.DefaultCaret;

import simulation.GBGame;

public class GBTournamentView extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4253425791054675659L;

	GBGame game;
	JTextPane tournamentResults = new JTextPane();

	public GBTournamentView(GBGame _game) {
		game = _game;
		tournamentResults.setContentType("text/html");
		tournamentResults.setEditable(false);
		update();
		DefaultCaret caret = (DefaultCaret) tournamentResults.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		JScrollPane scroll = new JScrollPane(tournamentResults);
		scroll.setPreferredSize(new Dimension(710, 430));
		scroll.setMinimumSize(scroll.getPreferredSize());
		add(scroll);
	}

	public void update() {
		tournamentResults.setText(game.htmlTournamentResults(false));
	}
}
