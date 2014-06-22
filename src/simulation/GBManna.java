package simulation;

import support.FinePoint;
import support.GBColor;
import java.awt.Color;
import exception.GBBadArgumentError;

public class GBManna extends GBFood {
	// public:
	public GBManna(FinePoint where, double val) {
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
	public GBColor Color() { return new GBColor(Color.green); }
}