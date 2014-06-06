// GBHeadless.cpp
// main for command-line Grobots.
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBWorld.h"
#include "GBErrors.h"
#include <exception>
#include <iostream>
#include "GBStringUtilities.h"
#include "GBSideReader.h"
#include "GBSound.h"
#include "GBSide.h"
#include "GBRobotType.h"
#include <time.h>
#include <stdlib.h>

using std::cout;
using std::cerr;
using std::endl;

static void ProcessArg(const char * arg, GBWorld & world,
				GBFrames & statsPeriod, const char * name);
static void DieWithUsage(const char * name);
static void PrintStatistics(const GBWorld & world);

static bool dumpHtml = false;

int main(int argc, const char * argv[]) {
	if ( argc < 2 ) DieWithUsage(argv[0]);
	try {
		GBWorld world;
		GBFrames statsPeriod = 500;
	//get args
		for (int i = 1; i < argc; ++ i)
			ProcessArg(argv[i], world, statsPeriod, argv[0]);
	//run
		time_t start = time(0);
		GBFrames totalFrames = 0;
		int round = 1;
		cout << "#round " << round << endl;
		world.AddSeeds();
		world.running = true;
		do {
			if ( statsPeriod && world.CurrentFrame() % statsPeriod == 0 )
				PrintStatistics(world);
			world.SimulateOneFrame();
			if (world.RoundOver()) {
				PrintStatistics(world);
				totalFrames += world.CurrentFrame();
				world.EndRound();
				if (world.tournament)
					cout << "#round " << ++round << endl;
			}
		} while ( world.running );
	//print final statistics
		cout << "#results: score error survival early-death late-death early-score fraction kills rounds name\n";
		for ( const GBSide * s = world.Sides(); s; s = s->next )
			cout << ToPercentString(s->TournamentScores().BiomassFraction()) << '\t'
				<< ToPercentString(s->TournamentScores().BiomassFractionError()) << '\t'
				<< ToPercentString(s->TournamentScores().SurvivalNotSterile(), 0) << '\t'
				<< ToPercentString(s->TournamentScores().EarlyDeathRate(), 0) << '\t'
				<< ToPercentString(s->TournamentScores().LateDeathRate(), 0) << '\t'
				<< ToPercentString(s->TournamentScores().EarlyBiomassFraction()) << '\t'
				<< ToPercentString(s->TournamentScores().SurvivalBiomassFraction(), 0) << '\t'
				<< ToPercentString(s->TournamentScores().KilledFraction(), 0) << '\t'
				<< s->TournamentScores().Rounds() << '\t'
				<< s->Name() << '\n';
		cout << "#total\t\t" << ToPercentString(world.TournamentScores().SurvivalNotSterile(), 0)
			<< '\t' << ToPercentString(world.TournamentScores().EarlyDeathRate(), 0)
			<< '\t' << ToPercentString(world.TournamentScores().LateDeathRate(), 0)
			<< "\t\t\t\t" << world.TournamentScores().Rounds() <<"\n#end" << endl;
		time_t dt = time(0) - start;
		cerr << totalFrames << " frames";
		if (dt)
			cerr << " in " << dt << " s (" << (totalFrames / dt) << " fps)";
		cerr << endl;
		if (dumpHtml)
			world.DumpTournamentScores();
	//clean up
		if ( SoundActive() ) CleanupSound();
		return EXIT_SUCCESS;
	} catch ( GBError & err ) {
		FatalError("Uncaught GBError: " + err.ToString());
	} catch ( GBRestart & r ) {
		FatalError("Uncaught GBRestart: " + r.ToString());
	} catch ( std::exception & e ) {
		FatalError("Uncaught std::exception: " + string(e.what()));
#if !WINDOWS
	} catch ( ... ) {
		FatalError("Uncaught mystery exception.");
#endif
	}
	return EXIT_FAILURE;
}

static void ProcessArg(const char * arg, GBWorld & world,
						GBFrames & statsPeriod, const char * name) {
	if ( '-' == arg[0]) {
		long dimension;
		switch ( arg[1] ) {
			case 't':
				world.tournament = true;
				if ( ! ParseInteger(arg + 2, world.tournamentLength) )
					DieWithUsage(name);
				cout << "#tournament " << world.tournamentLength << endl;
				break;
			case 'S':
				SetupSound();
				SetSoundActive(true);
				break;
			case 'l':
				if ( ! ParseInteger(arg + 2, world.timeLimit) )
					DieWithUsage(name);
				break;
			case 'b':
				if ( ! ParseInteger(arg + 2, statsPeriod) )
					DieWithUsage(name);
				break;
			case 'w':
				if ( ! ParseInteger(arg + 2, dimension) )
					DieWithUsage(name);
				world.Resize(GBFinePoint(kBackgroundTileSize * dimension, world.Top()));
				break;
			case 'h':
				if ( ! ParseInteger(arg + 2, dimension) )
					DieWithUsage(name);
				world.Resize(GBFinePoint(world.Right(), kBackgroundTileSize * dimension));
				break;
			case 's':
				if ( ! ParseInteger(arg + 2, world.seedLimit) )
					DieWithUsage(name);
				break;
			case 'H':
				dumpHtml = true;
				break;
			default:
				DieWithUsage(name);
		}
	} else {
	#if USE_MAC_IO
		Str255 fname;
		ToPascalString(arg, fname);
		FSSpec fs;
		if ( FSMakeFSSpec(0, 0, fname, &fs) )
			continue;
		GBSide * side = GBSideReader::Load(fs);
	#else
		GBSide * side = GBSideReader::Load(arg);
	#endif
		if ( side ) {
			world.AddSide(side);
			cout << "#side " << side->Name() << endl;
			for (const GBRobotType * type = side->GetFirstType(); type; type = type->next)
				cout << "\t#type " << type->Name()
					<< "\tcost " << ToString(type->Cost(), 0)
					<< "\tmass " << ToString(type->Mass(), 1) << endl;
		}
	}
}

static void DieWithUsage(const char * name) {
	cerr << "Usage: " << name << " [-t100] [-l4500] sides...\n"
		<< "Options (example numbers are defaults):\n"
		<< "  -t1     run a 1-round tournament\n"
		<< "  -l18000 limit rounds to 18000 frames\n"
		<< "  -b500   print biomass totals every 500 frames\n"
		<< "  -w10    world is 10 tiles wide\n"
		<< "  -h10    world is 10 tiles high\n"
		<< "  -s10    10 sides per round\n"
		<< "  -H      dump scores in HTML\n"
		<< "Grobots is free software distributed under the GNU General\n"
		<< "Public License." << endl;
	exit(EXIT_FAILURE);
}

static void PrintStatistics(const GBWorld & world) {
	cout << world.CurrentFrame();
	for ( const GBSide * s = world.Sides(); s; s = s->next )
		cout << '\t' << s->Scores().Biomass();
	cout << "\ttotal " << world.RobotValue() << endl;
}
