package ui;

import java.awt.geom.Rectangle2D;

import support.FinePoint;

public interface PortalListener {
	public void setViewpoint(Object source, FinePoint p);

	public void setVisibleWorld(Object source, Rectangle2D.Double r);

	public void addPortalListener(PortalListener pl);
}
