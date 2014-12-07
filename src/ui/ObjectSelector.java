package ui;

import simulation.GBObject;

public interface ObjectSelector {
	public GBObject getSelectedObject();

	public void addObjectSelectionListener(ObjectSelectionListener listener);

	public void removeObjectSelectionListener(ObjectSelectionListener listener);
}
