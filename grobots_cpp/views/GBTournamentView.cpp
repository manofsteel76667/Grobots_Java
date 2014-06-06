// GBTournamentView.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBTournamentView.h"
#include "GBSide.h"
#include "GBWorld.h"
#include "GBStringUtilities.h"
#include <algorithm>


const short kNameLeft = 25;
const short kPercentRight = 200;
const short kErrorRight = kPercentRight + 40;
const short kSurvivalRight = kErrorRight + 50;
const short kEarlyDeathRight = kSurvivalRight + 35;
const short kLateDeathRight = kEarlyDeathRight + 35;
const short kEarlyScoreRight = kLateDeathRight + 40;
const short kFractionRight = kEarlyScoreRight + 40;
const short kKillsRight = kFractionRight + 50;
const short kRoundsRight = kKillsRight + 40;
const short kWidth = kRoundsRight + 10;

GBColor GBTournamentView::RangeColor(float value, float min, float max,
		const GBColor & low, const GBColor & high,
		long rounds, long minrounds) {
	if ( rounds < minrounds ) return GBColor::gray;
	if ( value < min ) return low;
	if ( value > max ) return high;
	return GBColor::black;
}

GBTournamentView::GBTournamentView(GBWorld & target)
	: world(target), lastRounds(-1), numSides(0)
{}

void GBTournamentView::Draw() {
	sorted.empty();
	for (const GBSide * s = world.Sides(); s; s = s->next)
		sorted.push_back(s);
	std::sort(sorted.begin(), sorted.end(), GBSide::Better);
	GBListView::Draw();
	sorted.clear();
// record
	lastRounds = world.TournamentScores().Rounds();
	numSides = world.CountSides();
}

bool GBTournamentView::InstantChanges() const {
	return world.TournamentScores().Rounds() != lastRounds || numSides != world.CountSides();
}

short GBTournamentView::PreferredWidth() const {
	return kWidth;
}

const string GBTournamentView::Name() const {
	return "Tournament";
}

long GBTournamentView::Items() const {
	return world.CountSides();
}

short GBTournamentView::HeaderHeight() const {
	return 24;
}

short GBTournamentView::ItemHeight() const {
	return 15;
}

short GBTournamentView::FooterHeight() const {
	return 15;
}

void GBTournamentView::DrawHeader(const GBRect & box) {
	DrawBox(box);
	DrawStringLeft("Side", box.left + kNameLeft, box.bottom - 3, 10, GBColor::black);
// draw various numbers
	DrawStringRight("Score", box.left + kPercentRight, box.bottom - 3, 10, GBColor::black, true);
	DrawStringRight("Error", box.left + kErrorRight, box.bottom - 3, 10, GBColor::black);
	DrawStringRight("Survival", box.left + kSurvivalRight, box.bottom - 3, 10, GBColor::black);
	DrawStringRight("Death rates:", box.left + kLateDeathRight - 5, box.bottom - 13, 10, GBColor::black);
	DrawStringRight("Early", box.left + kEarlyDeathRight, box.bottom - 3, 10, GBColor::black);
	DrawStringRight("Late", box.left + kLateDeathRight, box.bottom - 3, 10, GBColor::black);
	DrawStringRight("Early", box.left + kEarlyScoreRight, box.bottom - 13, 10, GBColor::black);
	DrawStringRight("Score", box.left + kEarlyScoreRight, box.bottom - 3, 10, GBColor::black);
	DrawStringRight("Fraction", box.left + kFractionRight + 10, box.bottom - 3, 10, GBColor::black);
	DrawStringRight("Kills", box.left + kKillsRight, box.bottom - 3, 10, GBColor::black);
	DrawStringRight("Rounds", box.left + kRoundsRight, box.bottom - 3, 10, GBColor::black);
}

void GBTournamentView::DrawItem(long index, const GBRect & box) {
	DrawBox(box);
	const GBSide * side = sorted.at(index - 1);
	if ( ! side ) return;
	const GBScores & scores = side->TournamentScores();
// draw ID and name
	DrawStringRight(ToString(index) + '.', box.left + kNameLeft - 5, box.bottom - 4, 10, side->Color().ContrastingTextColor());
	DrawStringLeft(side->Name(), box.left + kNameLeft, box.bottom - 4, 10, GBColor::black);
// draw various numbers
	long rounds = scores.Rounds();
	long survived = scores.Survived();
	long notearly = rounds - scores.EarlyDeaths();
	if (rounds + survived >= 10)
		DrawStringRight(ToPercentString(scores.BiomassFractionError(), 1, true),
			box.left + kErrorRight, box.bottom - 4, 10, survived > 10 ? GBColor::black : GBColor::gray);
	if ( rounds > 0 ) {
		GBNumber score = scores.BiomassFraction();
		DrawStringRight(ToPercentString(score, 1), box.left + kPercentRight, box.bottom - 4,
			10, (rounds + survived < kMinColorRounds * 2 || score.ToDouble() < scores.BiomassFractionError() * 2)
			? GBColor::gray : GBColor::black, true);
		float survival = scores.SurvivalNotSterile();
		DrawStringRight(ToPercentString(survival),
			box.left + kSurvivalRight, box.bottom - 4,
			10, RangeColor(survival, 0.2f, 0.4f, GBColor::darkRed, GBColor::darkGreen, rounds));
		float early = scores.EarlyDeathRate();
		DrawStringRight(ToPercentString(early, 0),
			box.left + kEarlyDeathRight, box.bottom - 4,
			10, RangeColor(early, 0.2f, 0.4f, GBColor::darkGreen, GBColor::darkRed, rounds));
	}
	if ( notearly > 0 ) {
		float late = scores.LateDeathRate();
		DrawStringRight(ToPercentString(late, 0),
			box.left + kLateDeathRight, box.bottom - 4,
			10, RangeColor(late, 0.4f, 0.6f, GBColor::darkGreen, GBColor::darkRed, notearly));
	}
	if ( rounds > 0 ) {
		GBNumber early = scores.EarlyBiomassFraction();
		DrawStringRight(ToPercentString(early), box.left + kEarlyScoreRight, box.bottom - 4,
			10, RangeColor(early.ToDouble(), 0.08f, 0.12f, GBColor::darkRed, GBColor::darkGreen,
				rounds + notearly, kMinColorRounds * 2));
	}
	if ( survived > 0 ) {
		float fraction = scores.SurvivalBiomassFraction();
		DrawStringRight(ToPercentString(fraction, 0),
			box.left + kFractionRight, box.bottom - 4,
			10, RangeColor(fraction, 0.2f, 0.4f, GBColor::blue, GBColor::purple, survived));
	}
	if ( rounds > 0 ) {
		float kills = scores.KilledFraction();
		DrawStringRight(ToPercentString(kills, 0), box.left + kKillsRight, box.bottom - 4,
			10, RangeColor(kills, 0.05f, 0.15f, GBColor::blue, GBColor::purple, survived));
	}
	DrawStringRight(ToString(rounds), box.left + kRoundsRight, box.bottom - 4,
			10, rounds < kMinColorRounds ? GBColor::darkRed : GBColor::black);
}

void GBTournamentView::DrawFooter(const GBRect & box) {
	DrawBox(box);
	DrawStringLeft("Overall:", box.left + kNameLeft, box.bottom - 4, 10, GBColor::black, true);
// draw various numbers
	long rounds = world.TournamentScores().Rounds();
	long notearly = world.TournamentScores().SurvivedEarly();
	if ( rounds > 0 ) {
		float survival = world.TournamentScores().SurvivalNotSterile();
		DrawStringRight(ToPercentString(survival, 0), box.left + kSurvivalRight, box.bottom - 4,
			10, RangeColor(survival, 0.25f, 0.5f, GBColor::darkRed, GBColor::darkGreen, rounds));
		float early = world.TournamentScores().EarlyDeathRate();
		DrawStringRight(ToPercentString(early, 0),
			box.left + kEarlyDeathRight, box.bottom - 4,
			10, RangeColor(early, 0.2f, 0.4f, GBColor::darkGreen, GBColor::darkRed, rounds));
	}
	if ( notearly > 0 ) {
		float late = world.TournamentScores().LateDeathRate();
		DrawStringRight(ToPercentString(late, 0), box.left + kLateDeathRight, box.bottom - 4,
			10, RangeColor(late, 0.45f, 0.6f, GBColor::darkGreen, GBColor::darkRed, rounds));
	}
	if ( rounds > 0 ) {
		float kills = world.TournamentScores().KillRate();
		DrawStringRight(ToPercentString(kills, 0), box.left + kKillsRight, box.bottom - 4,
			10, RangeColor(kills, 1.2f, 1.8f, GBColor::blue, GBColor::purple, rounds));
	}
	DrawStringRight(ToString(rounds), box.left + kRoundsRight, box.bottom - 4,
			10, rounds < kMinColorRounds ? GBColor::darkRed : GBColor::blue);
}

