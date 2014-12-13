package ui;

import simulation.GBObject;

/**
 * Interface for a class that can select an object. In most cases, changing the
 * selected object can also change the selected side or type so those interfaces
 * are inherited.
 * 
 * @author mike
 * 
 */
public interface ObjectSelector extends TypeSelector, SideSelector {
	public GBObject getSelectedObject();

	public void addObjectSelectionListener(ObjectSelectionListener listener);

	public void removeObjectSelectionListener(ObjectSelectionListener listener);
}
