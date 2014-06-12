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
	public short ToScreenX(double x);

	public short ToScreenY(double y);

	public double FromScreenX(short h);

	public double FromScreenY(short v);

	public FinePoint FromScreen(short x, short y);
}