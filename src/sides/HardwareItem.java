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

	public double Mass() {
		return mass;
	}

	public double Cost() {
		return cost;
	}
}
