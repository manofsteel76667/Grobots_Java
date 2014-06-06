// GBScoresView.h
// scores and statistics
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBScoresView_h
#define GBScoresView_h

#include "GBListView.h"
#include "GBWorld.h"


class GBScoresView : public GBView {
	GBWorld & world;
	GBChangeCount lastDrawnWorld;
	const GBSide * lastSideDrawn;
	bool graphAllLastDrawn;
	
	void DrawIncome(const GBIncomeStatistics & income, short left, short right, short top);
	void DrawExpenditures(const GBExpenditureStatistics & spent, short left, short right, short top);
	void DrawDeaths(const GBScores & scores, short left, short right, short top);
	void DrawGraph(const GBRect & box, long vscale, int hscale,
		const std::vector<long> & hist, const GBColor & color);
	void DrawGraphs(const GBRect & box);
	void DrawRoundScores(const GBScores & scores, const GBRect & box);
	void DrawTournamentScores(const GBScores & tscores, const GBRect & box);
public:
	bool graphAllRounds;
	
	explicit GBScoresView(GBWorld & rost);

	void Draw();
	
	GBMilliseconds RedrawInterval() const;
	bool InstantChanges() const;
	bool DelayedChanges() const;
	
	short PreferredWidth() const;
	short PreferredHeight() const;
	
	const string Name() const;
};

#endif
