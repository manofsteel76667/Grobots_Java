/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package views;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.DefaultCaret;

import sides.GBScores;
import sides.Side;
import simulation.GBGame;
import support.GBColor;
import support.StringUtilities;

public class GBTournamentView extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4253425791054675659L;

	GBGame game;
	JTextPane tournamentResults = new JTextPane();
	
	static final String headerText = "<thead class=header><tr>"
			+ "<th></th>"
			+ "<th>Side</th>"
			+ "<th>Score</th>"
			+ "<th>Error</th>"
			+ "<th>Survival</th>"
			+ "<th>Early<br/>Death</th>"
			+ "<th>Late<br/>Death</th>"
			+ "<th>Early<br/>Score</th>"
			+ "<th>Fraction</th>"
			+ "<th>Kills</th>"
			+ "<th>Rounds</th>"
			+ "</tr></thead>";
			
	static final String tableFormat = "%s<table rules=groups>%s<tbody>%s</tbody>%s</table>";
	
	static final String inlineStyle = "<style type=text/css>"
			+ ".header {border: 1px solid;}"
			+ ".header td {valign: bottom;}"
			+ ".bodyrow {border: 1px solid}"
			+ "td {text-align: center; }"
			+ "th {text-align: center; valign: bottom;}"
			+ "</style>";

	public GBTournamentView(GBGame _game) {
		game = _game;
		tournamentResults.setContentType("text/html");
		tournamentResults.setEditable(false);
		update();
		DefaultCaret caret = (DefaultCaret)tournamentResults.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		JScrollPane scroll = new JScrollPane(tournamentResults);
		scroll.setPreferredSize(new Dimension(600,400));
		add(scroll);
	}
	
	public void update(){
		tournamentResults.setText(String.format(tableFormat, 
				inlineStyle,
				headerText,
				buildTableBody(),
				buildFooter()));
	}
	
	static final int kMinColorRounds = 10;

	private Color rangeColor(double value, double min, double max, Color low,
			Color high, int rounds, int minrounds) {
		if (rounds < minrounds)
			return Color.gray;
		if (value < min)
			return low;
		if (value > max)
			return high;
		return Color.black;
	}
	
	public String buildTableBody(){
		List<Side> list = new ArrayList<Side>();
		list.addAll(game.sides);
		Collections.sort(list);
		StringBuilder sb = new StringBuilder();
		for(int i = 0;i< list.size();i++){
			Side side = list.get(i);
			GBScores scores = side.TournamentScores();
			int rounds = scores.rounds;
			int survived = scores.survived;
			int notearly = rounds - scores.earlyDeaths;
			sb.append("<tr class=bodyrow>");
			//ID and name
			sb.append(StringUtilities.makeTableCell("%d.", i+1, GBColor.ContrastingTextColor(side.Color())));
			sb.append(StringUtilities.makeTableCell(150, "%s", side.Name(), null));
			//Score error
			if (rounds + survived >= 10)
			sb.append(StringUtilities.makeTableCell("%.1f%%", scores.BiomassFraction() * 100, 
					survived > 10 ? null : Color.gray));
			else
				sb.append(StringUtilities.makeEmptyTableCell());
			// Other scores
			if (rounds > 0){
				sb.append(StringUtilities.makeTableCell("%.1f%%", scores.BiomassFractionError() * 100, 
						(rounds + survived < kMinColorRounds * 2 || scores.BiomassFractionError() < scores
								.BiomassFractionError() * 2) ? Color.gray
								: null));
				sb.append(StringUtilities.makeTableCell("%.0f%%", scores.SurvivalNotSterile() * 100,
						rangeColor(scores.SurvivalNotSterile(), 0.2, 0.4, GBColor.darkRed,
								GBColor.darkGreen, rounds, 0)));
				sb.append(StringUtilities.makeTableCell("%.0f%%", scores.EarlyDeathRate() * 100,
						rangeColor(scores.EarlyDeathRate(), 0.2, 0.4, GBColor.darkGreen,
								GBColor.darkRed, rounds, 0)));
				if (notearly > 0) {
					sb.append(StringUtilities.makeTableCell("%.0f%%", scores.LateDeathRate() * 100,
							rangeColor(scores.LateDeathRate(), 0.4, 0.6, GBColor.darkGreen,
									GBColor.darkRed, notearly, 0)));
				} else {
					sb.append(StringUtilities.makeEmptyTableCell());
				}
				sb.append(StringUtilities.makeTableCell("%.0f%%", scores.EarlyBiomassFraction() * 100,
						rangeColor(scores.EarlyBiomassFraction(), 0.08f, 0.12f, GBColor.darkRed,
								GBColor.darkGreen, rounds + notearly,
								kMinColorRounds * 2)));
				if (survived > 0) {
					sb.append(StringUtilities.makeTableCell("%.0f%%", scores.SurvivalBiomassFraction() * 100,
							rangeColor(scores.SurvivalBiomassFraction(), 0.2, 0.4, Color.blue, GBColor.purple,
									survived, 0)));
				}
				else {
					sb.append(StringUtilities.makeEmptyTableCell());
				}
				sb.append(StringUtilities.makeTableCell("%.0f%%", scores.KilledFraction() * 100,
						rangeColor(scores.KilledFraction(), 0.05, 0.15, Color.blue, GBColor.purple,
								survived, 0)));				
			} else 
				sb.append(StringUtilities.makeEmptyTableCells(7));
			sb.append(StringUtilities.makeTableCell("%d", rounds, rounds < kMinColorRounds ? GBColor.darkRed : null));
		}
		sb.append("</tr>");
		return sb.toString();
	}

	public String buildFooter(){
		StringBuilder sb = new StringBuilder();
		GBScores scores = game.TournamentScores();
		int rounds = scores.rounds;
		int notearly = scores.survived;
		sb.append("<tr>");
		sb.append(StringUtilities.makeEmptyTableCell());
		sb.append(StringUtilities.makeTableCell(150, "%s", "Overall:", null));
		sb.append(StringUtilities.makeEmptyTableCells(2));
		if (rounds > 0){
			sb.append(StringUtilities.makeTableCell("%.0f%%", scores.SurvivalNotSterile() * 100,
					rangeColor(scores.SurvivalNotSterile(), 0.25, 0.5, GBColor.darkRed,
							GBColor.darkGreen, rounds, 0)));
			sb.append(StringUtilities.makeTableCell("%.0f%%", scores.EarlyDeathRate() * 100,
					rangeColor(scores.EarlyDeathRate(), 0.2, 0.4, GBColor.darkGreen,
							GBColor.darkRed, rounds, 0)));
			if (notearly > 0) {
			sb.append(StringUtilities.makeTableCell("%.0f%%", scores.LateDeathRate() * 100,
					rangeColor(scores.LateDeathRate(), 0.45, 0.6, GBColor.darkGreen,
							GBColor.darkRed, rounds, 0)));
			} else {
				sb.append(StringUtilities.makeEmptyTableCell());
			}			
			sb.append(StringUtilities.makeEmptyTableCells(2));
			sb.append(StringUtilities.makeTableCell("%.0f%%", scores.KillRate() * 100,
					rangeColor(scores.KillRate(), 1.2, 1.8, Color.blue, GBColor.purple,
							rounds, 0)));
		}
		else
			sb.append(StringUtilities.makeEmptyTableCells(6));
		sb.append(StringUtilities.makeTableCell("%d", rounds, rounds < kMinColorRounds ? GBColor.darkRed : null));
		sb.append("</tr>");
		return sb.toString();
	}
	
	/*@Override
	Rectangle drawOneItem(Graphics2D g, int index) {
		// draw various numbers
		int rounds = scores.rounds;
		int survived = scores.survived;
		int notearly = rounds - scores.earlyDeaths;
		if (rounds + survived >= 10)
			StringUtilities.drawStringRight(
					g,
					StringUtilities.toPercentString(
							scores.BiomassFractionError(), 1), box.x
							+ kErrorRight, box.y + box.height, 10,
					survived > 10 ? Color.black : Color.gray);
		if (rounds > 0) {
			double score = scores.BiomassFraction();
			StringUtilities.drawStringRight(g, StringUtilities.toPercentString(
					score, 1), box.x + kPercentRight, box.y + box.height, 10,
					(rounds + survived < kMinColorRounds * 2 || score < scores
							.BiomassFractionError() * 2) ? Color.gray
							: Color.black);
			double survival = scores.SurvivalNotSterile();
			StringUtilities.drawStringRight(
					g,
					StringUtilities.toPercentString(survival, 0),
					box.x + kSurvivalRight,
					box.y + box.height,
					10,
					rangeColor(survival, 0.2, 0.4, GBColor.darkRed,
							GBColor.darkGreen, rounds, 0));
			double early = scores.EarlyDeathRate();
			StringUtilities.drawStringRight(
					g,
					StringUtilities.toPercentString(early, 0),
					box.x + kEarlyDeathRight,
					box.y + box.height,
					10,
					rangeColor(early, 0.2, 0.4, GBColor.darkGreen,
							GBColor.darkRed, rounds, 0));
		}
		if (notearly > 0) {
			double late = scores.LateDeathRate();
			StringUtilities.drawStringRight(
					g,
					StringUtilities.toPercentString(late, 0),
					box.x + kLateDeathRight,
					box.y + box.height,
					10,
					rangeColor(late, 0.4, 0.6, GBColor.darkGreen,
							GBColor.darkRed, notearly, 0));
		}
		if (rounds > 0) {
			double early = scores.EarlyBiomassFraction();
			StringUtilities.drawStringRight(
					g,
					StringUtilities.toPercentString(early, 0),
					box.x + kEarlyScoreRight,
					box.y + box.height,
					10,
					rangeColor(early, 0.08f, 0.12f, GBColor.darkRed,
							GBColor.darkGreen, rounds + notearly,
							kMinColorRounds * 2));
		}
		if (survived > 0) {
			double fraction = scores.SurvivalBiomassFraction();
			StringUtilities.drawStringRight(
					g,
					StringUtilities.toPercentString(fraction, 0),
					box.x + kFractionRight,
					box.y + box.height,
					10,
					rangeColor(fraction, 0.2, 0.4, Color.blue, GBColor.purple,
							survived, 0));
		}
		if (rounds > 0) {
			double kills = scores.KilledFraction();
			StringUtilities.drawStringRight(
					g,
					StringUtilities.toPercentString(kills, 0),
					box.x + kKillsRight,
					box.y + box.height,
					10,
					rangeColor(kills, 0.05, 0.15, Color.blue, GBColor.purple,
							survived, 0));
		}
		StringUtilities.drawStringRight(g, Integer.toString(rounds), box.x
				+ kRoundsRight, box.y + box.height, 10,
				rounds < kMinColorRounds ? GBColor.darkRed : Color.black);
		box.grow(padding * 2, padding * 2);
		return box;
	}*/

	/*@Override
	Rectangle drawFooter(Graphics2D g) {
		Rectangle box = getStartingFooterRect(10, false);
		drawBox(g, box);
		box.grow(-padding * 2, -padding * 2);
		StringUtilities.drawStringLeft(g, "Overall:", box.x + kNameLeft, box.y
				+ box.height, 10, Color.black);
		// draw various numbers
		int rounds = game.TournamentScores().rounds;
		int notearly = game.TournamentScores().survived;// .SurvivedEarly();
		if (rounds > 0) {
			double survival = game.TournamentScores().SurvivalNotSterile();
			StringUtilities.drawStringRight(
					g,
					StringUtilities.toPercentString(survival, 0),
					box.x + kSurvivalRight,
					box.y + box.height,
					10,
					rangeColor(survival, 0.25, 0.5, GBColor.darkRed,
							GBColor.darkGreen, rounds, 0));
			double early = game.TournamentScores().EarlyDeathRate();
			StringUtilities.drawStringRight(
					g,
					StringUtilities.toPercentString(early, 0),
					box.x + kEarlyDeathRight,
					box.y + box.height,
					10,
					rangeColor(early, 0.2, 0.4, GBColor.darkGreen,
							GBColor.darkRed, rounds, 0));
		}
		if (notearly > 0) {
			double late = game.TournamentScores().LateDeathRate();
			StringUtilities.drawStringRight(
					g,
					StringUtilities.toPercentString(late, 0),
					box.x + kLateDeathRight,
					box.y + box.height,
					10,
					rangeColor(late, 0.45, 0.6, GBColor.darkGreen,
							GBColor.darkRed, rounds, 0));
		}
		if (rounds > 0) {
			double kills = game.TournamentScores().KillRate();
			StringUtilities.drawStringRight(
					g,
					StringUtilities.toPercentString(kills, 0),
					box.x + kKillsRight,
					box.y + box.height,
					10,
					rangeColor(kills, 1.2, 1.8, Color.blue, GBColor.purple,
							rounds, 0));
		}
		StringUtilities.drawStringRight(g, Integer.toString(rounds), box.x
				+ kRoundsRight, box.y + box.height, 10,
				rounds < kMinColorRounds ? GBColor.darkRed : Color.blue);
		box.grow(padding * 2, padding * 2);
		return box;
	}*/
}
