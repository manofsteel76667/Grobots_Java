package simulation;

import support.FinePoint;

/**
 * Used for mapping GBWorld coordinates to screen locations.  Could be 
 * included in ui instead but that would make headless mode dependant
 * on the ui.
 * @author mike
 *
 */
public interface GBProjection {
	public int ToScreenX(double x);

	public int ToScreenY(double y);

	public double FromScreenX(int h);

	public double FromScreenY(int v);

	public FinePoint FromScreen(int x, int y);
}