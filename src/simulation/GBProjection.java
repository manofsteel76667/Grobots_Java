/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import support.FinePoint;

/**
 * Used for mapping GBWorld coordinates to screen locations. Could be included
 * in ui instead but that would make headless mode dependant on the ui.
 * 
 * @author mike
 * 
 */
public interface GBProjection {
	public int ToScreenX(double x);

	public int ToScreenY(double y);

	public double FromScreenX(int h);

	public double FromScreenY(int v);

	public FinePoint FromScreen(int x, int y);

	public int getScale();

}