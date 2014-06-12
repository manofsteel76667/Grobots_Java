package simulation;

import support.FinePoint;
import exception.GBBadArgumentError;

public class GBManna extends GBFood {
	// public:
	public GBManna(FinePoint where, double val) throws GBBadArgumentError {
		super(where, val);
	}

	@Override
	public void CollectStatistics(GBWorld world) {
		world.ReportManna(value);
	}

	@Override
	public String toString() {
		return "Manna (" + value + ')';
	}
	// TODO: GUI
	/*
	 * public static final GBColor Color() { return GBColor::green; }
	 */
}