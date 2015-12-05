/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package sides;

/*Common interface for all hardware items
 */
public class HardwareItem {
	double mass;
	double cost;

	public HardwareItem(double _mass, double _cost) {
		mass = _mass;
		cost = _cost;
	}

	public double getMass() {
		return mass;
	}

	public double getCost() {
		return cost;
	}
}
