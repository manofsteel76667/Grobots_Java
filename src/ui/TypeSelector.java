package ui;

import sides.RobotType;

/**
 * Interface for a class that can select a ro type. In some cases, changing the
 * selected type can also change the selected side so that interfaces are
 * inherited.
 * 
 * @author mike
 * 
 */
public interface TypeSelector extends SideSelector {
	public RobotType getSelectedType();

	public void addTypeSelectionListener(TypeSelectionListener listener);

	public void removeTypeSelectionListener(TypeSelectionListener listener);
}
