/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package views;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import sides.Side;
import sides.SideReader;
import simulation.GBGame;
import support.GBColor;
import ui.SideSelectionListener;
import ui.SideSelector;
import exception.GBError;

/**
 * http://java.sun.com/products/jfc/tsc...t_1/jlist.html
 * 
 * @author mike
 * 
 */

class ToggleSelectionModel extends DefaultListSelectionModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7368341084169767401L;
	boolean gestureStarted = false;

	public void setSelectionInterval(int index0, int index1) {
		if (isSelectedIndex(index0) && !gestureStarted) {
			super.removeSelectionInterval(index0, index1);
		} else {
			super.setSelectionInterval(index0, index1);
		}
		gestureStarted = true;
	}

	public void setValueIsAdjusting(boolean isAdjusting) {
		if (isAdjusting == false) {
			gestureStarted = false;
		}
	}
}

public class GBRosterView extends JPanel implements SideSelectionListener,
		SideSelector, ListSelectionListener {
	/**
	 * 
	 */
	static final int kFramecounterHeight = 15;
	static final int kSideBoxHeight = 17;
	static final int kPopulationWidth = 50;

	private static final long serialVersionUID = 6135247814368773456L;

	GBGame game;
	int fps;
	long lastFrame;
	long lastTime;
	Side selectedSide;
	JList<Side> list;
	JLabel header;
	Side lastSelection;

	DefaultListModel<Side> model = new DefaultListModel<Side>();

	List<SideSelectionListener> sideListeners;

	String headerLine = "<html><table margin-left=0><tr>"
			+ "<td align=center width=75>Frame<br>%d</td>"
			+ "<td align=center width=100 valign=middle>%s</td>"
			+ "<td align=right width = 75>%d fps</td>" + "</tr></table></html>";

	public GBRosterView(GBGame _game) {
		game = _game;
		sideListeners = new ArrayList<SideSelectionListener>();
		lastTime = System.currentTimeMillis();
		for (Side s : _game.sides)
			model.addElement(s);
		list = new JList<Side>(model);
		list.setCellRenderer(new SideRenderer());
		list.setSelectionModel(new ToggleSelectionModel());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(Color.white);
		header = new JLabel();
		recalculate();
		list.addListSelectionListener(this);
		add(header);
		JScrollPane scroll = new JScrollPane(list);
		scroll.setPreferredSize(new Dimension(240, 600));
		add(scroll);
		setPreferredSize(new Dimension(240, 400));
		setVisible(true);
	}

	public void recalculate() {
		if (game.sides.size() == 0) {
			header.setText("<html><div align=center>" + "No sides loaded.</br>"
					+ "Why not download some from the help menu?"
					+ "</div></html>");
		} else {
			int fps = 0;
			String status = game.tournament ? "Tournament: </br>" : "";
			status += game.running ? "Running" : "Paused";
			if (game.running && lastTime >= 0 && game.totalFrames > lastFrame) {
				long frames = game.totalFrames - lastFrame;
				long ms = Math.max(System.currentTimeMillis() - lastTime, 1);
				fps = (int) (frames * 1000 / ms);
			}
			header.setText(String.format(headerLine, game.currentFrame(),
					status, fps));
		}
		lastFrame = game.totalFrames;
		lastTime = System.currentTimeMillis();
	}

	@Override
	public void setSelectedSide(Side side) {
		selectedSide = side;
		if (side != null)
			list.setSelectedIndex(model.indexOf(side));
		else
			list.setSelectedIndex(-1);
		repaint();
	}

	void notifySideListeners() {
		for (SideSelectionListener l : sideListeners)
			if (l != null)
				l.setSelectedSide(selectedSide);
	}

	@Override
	public Side getSelectedSide() {
		return selectedSide;
	}

	@Override
	public void addSideSelectionListener(SideSelectionListener listener) {
		if (listener != null)
			sideListeners.add(listener);

	}

	@Override
	public void removeSideSelectionListener(SideSelectionListener listener) {
		sideListeners.remove(listener);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		int index = list.getSelectedIndex();
		if (index == -1 || index > game.sides.size()) {
			selectedSide = null;
		} else
			selectedSide = game.sides.get(index);
		notifySideListeners();
		lastSelection = selectedSide;
	}

	public void loadSide() {
		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		FileFilter filter = new FileNameExtensionFilter("Grobots sides", "gb");
		fc.setFileFilter(filter);
		// fc.setCurrentDirectory(new File("." + "\\src\\test\\sides"));
		int retval = fc.showOpenDialog(this);
		if (retval == JFileChooser.APPROVE_OPTION) {
			for (File f : fc.getSelectedFiles()) {
				try {
					Side newside;
					newside = SideReader.loadFromFile(f.getPath());
					game.addSide(newside);
					model.addElement(newside);
				} catch (Exception fileEx) {
					GBError.NonfatalError(String.format(
							"%s could not be loaded.", f.getName()));
				}
			}
		}
		repaint();
	}

	public void removeSide() {
		if (selectedSide == null)
			return;
		game.removeSide(selectedSide);
		model.removeElement(selectedSide);
		selectedSide = null;
		notifySideListeners();
	}

	public void removeAllSides() {
		game.reset();
		game.removeAllSides();
		game.running = false;
		model.clear();
		selectedSide = null;
		notifySideListeners();
	}

	public void duplicateSide() {
		if (selectedSide == null)
			return;
		Side newside = SideReader.loadFromFile(selectedSide.filename);
		game.addSide(newside);
		model.addElement(newside);
	}

	public void reloadSide() {
		if (selectedSide == null)
			return;
		Side reload = SideReader.loadFromFile(selectedSide.filename);
		if (game.sides.contains(selectedSide))
			game.replaceSide(selectedSide, reload);
		model.clear();
		for (Side s : game.sides)
			model.addElement(s);
		selectedSide = reload;
		notifySideListeners();
	}

	class SideRenderer extends JLabel implements ListCellRenderer<Side> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5868238292708174971L;

		String cellFormat = "<html><table><tr>"
				+ "<td width=10 align=right color=%s>%d.</td>"
				+ "<td width=120>%s</td>" + "<td width=%d>%s</td>"
				+ "<td width=%d align=right color=%s>%s</td>"
				+ "</tr><table></html>";

		@Override
		public JLabel getListCellRendererComponent(JList<? extends Side> list,
				Side side, int index, boolean isSelected, boolean cellHasFocus) {
			setText("");
			setPreferredSize(new Dimension(240, 25));
			String sidecolor = GBColor.toHex(GBColor.ContrastingTextColor(side
					.getColor()));
			String biopercent = ""; // Biomass percent if healthy, null if
									// sterile or extinct
			String message = ""; // Robot count if healthy, extinct or sterile
									// message if not
			String messagecolor = "blue";
			int bioWidth = 30;
			int messageWidth = 50;
			if (side.getScores().getSeeded() != 0) {
				// Side status
				if (side.getScores().getPopulation() != 0) {
					// Sterile?
					if (side.getScores().sterile != 0) {
						message = String.format("Sterile at %d", side.getScores()
								.getSterileTime());
						messagecolor = "gray";
						messageWidth = 129;
						bioWidth = 1;
					} else {
						// Doing fine
						// Bio percentage
						biopercent = String.format("%.1f%%", side.getScores()
								.getBiomassFraction() * 100);
						// Population
						message = Integer.toString(side.getScores().getPopulation());
					}
				} else {
					// Extinct
					message = String.format("Extinct at %d", side.getScores()
							.getExtinctTime());
					messagecolor = "gray";
					messageWidth = 129;
					bioWidth = 1;
				}
			}
			setText(String.format(cellFormat, sidecolor, side.getID(),
					side.getName(), bioWidth, biopercent, messageWidth,
					messagecolor, message));
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);
			return this;
		}
	}
}
