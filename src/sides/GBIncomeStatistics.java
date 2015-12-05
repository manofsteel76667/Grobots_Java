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

	public void reset() {
		autotrophy = 0;
		theotrophy = 0;
		heterotrophy = 0;
		cannibalism = 0;
		kleptotrophy = 0;
	}

	public void reportAutotrophy(double en) {
		autotrophy += en;
	}

	public void reportTheotrophy(double en) {
		theotrophy += en;
	}

	public void reportHeterotrophy(double en) {
		heterotrophy += en;
	}

	public void reportCannibalism(double en) {
		cannibalism += en;
	}

	public void reportKleptotrophy(double en) {
		kleptotrophy += en;
	}

	public int total() {
		// excludes cannibalism and seeded
		return (int) (autotrophy + theotrophy + heterotrophy + kleptotrophy);
	}

	public void add(GBIncomeStatistics other) {
		autotrophy += other.autotrophy;
		theotrophy += other.theotrophy;
		heterotrophy += other.heterotrophy;
		cannibalism += other.cannibalism;
		kleptotrophy += other.kleptotrophy;
	}
};
