package simulation;

import support.FinePoint;
import exception.GBBadArgumentError;

public class GBManna extends GBFood {
	// public:
	GBManna(FinePoint where, double val) throws GBBadArgumentError {
		super(where, val);
	}

	public void CollectStatistics(GBWorld world) {
		world.ReportManna(value);
	}

	public String Description() {
		return "Manna (" + value + ')';
	}
	// TODO: GUI
	/*
	 * public static final GBColor Color() { return GBColor::green; }
	 */
}