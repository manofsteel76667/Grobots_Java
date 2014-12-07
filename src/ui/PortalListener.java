package ui;

import java.awt.Rectangle;

import support.FinePoint;

public interface PortalListener {
	public void setViewWindow(Object source, FinePoint p);

	public void setVisibleWorld(Object source, Rectangle r);

	public void addPortalListener(PortalListener pl);
}
