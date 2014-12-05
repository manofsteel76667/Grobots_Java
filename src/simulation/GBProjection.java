/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import support.FinePoint;

/**
 * Used for mapping GBWorld coordinates to screen locations. Could be included
 * in ui instead but that would make headless mode dependant on the ui.
 * 
 * @author mike
 * 
 */
public interface GBProjection<T> {
	public int toScreenX(double x);

	public int toScreenY(double y);

	public double fromScreenX(int h);

	public double fromScreenY(int v);

	public FinePoint fromScreen(int x, int y);
	
	public FinePoint toScreen(Point2D.Double point);

	public int getScale();
	
	public Rectangle2D.Double toScreenRect(T gameObject);
	
	public Ellipse2D.Double toScreenEllipse(T gameObject);

}