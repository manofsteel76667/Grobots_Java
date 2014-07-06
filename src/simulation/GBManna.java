/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import java.awt.Color;

import support.FinePoint;
import support.GBColor;

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
		return String.format("Manna (%.0f)", value);
	}

	public GBColor Color() {
		return new GBColor(Color.green);
	}
}