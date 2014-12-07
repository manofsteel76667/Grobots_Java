package ui;

import sides.Side;

public interface SideSelector {
	public Side getSelectedSide();

	public void addSideSelectionListener(SideSelectionListener listener);

	public void removeSideSelectionListener(SideSelectionListener listener);
}
