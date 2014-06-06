// GBTournamentView.h
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBTournamentView_h
#define GBTournamentView_h

#include "GBListView.h"
#include <vector>

const long kMinColorRounds = 10;

class GBWorld;
class GBSide;

class GBTournamentView : public GBListView {
	GBWorld & world;
	long lastRounds;
	int numSides;
	std::vector<const GBSide *> sorted;
	
	static GBColor RangeColor(float value, float min, float max,
		const GBColor & low, const GBColor & high,
		long rounds, long minrounds = kMinColorRounds);
public:
	explicit GBTournamentView(GBWorld & target);

	void Draw();
	bool InstantChanges() const;
	
	short PreferredWidth() const;
	
	const string Name() const;

	long Items() const;
	short HeaderHeight() const;
	short ItemHeight() const;
	short FooterHeight() const;
	void DrawHeader(const GBRect & box);
	void DrawItem(long index, const GBRect & box);
	void DrawFooter(const GBRect & box);
};

#endif
