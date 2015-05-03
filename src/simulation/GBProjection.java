/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;


/**
 * Used for mapping GBWorld coordinates to screen locations. Could be included
 * in ui instead but that would make headless mode dependant on the ui.
 * 
 * @author mike
 * 
 */
public interface GBProjection {
	public int toScreenX(double x);

	public int toScreenY(double y);

	public double fromScreenX(int h);

	public double fromScreenY(int v);

	public int getScale();

}