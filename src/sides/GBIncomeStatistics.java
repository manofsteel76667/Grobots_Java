/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
// GBScores.cpp
// Grobots (c) 2002-2006 Devon and Warren Schudy
// Distributed under the GNU General Public License.
package sides;

public class GBIncomeStatistics {
	public double autotrophy;
	public double theotrophy;
	public double heterotrophy;
	public double cannibalism;
	public double kleptotrophy;

	public GBIncomeStatistics()

	{
	}

	public void Reset() {
		autotrophy = 0;
		theotrophy = 0;
		heterotrophy = 0;
		cannibalism = 0;
		kleptotrophy = 0;
	}

	public void ReportAutotrophy(double en) {
		autotrophy += en;
	}

	public void ReportTheotrophy(double en) {
		theotrophy += en;
	}

	public void ReportHeterotrophy(double en) {
		heterotrophy += en;
	}

	public void ReportCannibalism(double en) {
		cannibalism += en;
	}

	public void ReportKleptotrophy(double en) {
		kleptotrophy += en;
	}

	public long Total() {
		// excludes cannibalism and seeded
		return (long) (autotrophy + theotrophy + heterotrophy + kleptotrophy);
	}

	public void add(GBIncomeStatistics other) {
		autotrophy += other.autotrophy;
		theotrophy += other.theotrophy;
		heterotrophy += other.heterotrophy;
		cannibalism += other.cannibalism;
		kleptotrophy += other.kleptotrophy;
	}
};
