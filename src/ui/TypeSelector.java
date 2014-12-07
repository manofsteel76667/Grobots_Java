package ui;

import sides.RobotType;

public interface TypeSelector {
	public RobotType getSelectedType();

	public void addTypeSelectionListener(TypeSelectionListener listener);

	public void removeTypeSelectionListener(TypeSelectionListener listener);
}
