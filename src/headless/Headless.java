package headless;

import java.util.Date;
import java.util.List;

import sides.RobotType;
import sides.Side;
import sides.SideReader;
import simulation.GBObjectWorld;
import simulation.GBWorld;
import support.FinePoint;
import support.StringUtilities;
import exception.GBError;

public class Headless {
	// GBHeadless.cpp
	// main for command-line Grobots.
	// Grobots (c) 2002-2004 Devon and Warren Schudy
	// Distributed under the GNU General Public License.

	static boolean dumpHtml = false;

	public static void main(String[] argv) {
		if (argv.length < 2)
			DieWithUsage(argv[0]);
		try {
			GBWorld world = new GBWorld();
			world.timeLimit = 18000;
			int statsPeriod = 500;
			// get args
			for (int i = 0; i < argv.length; ++i)
				ProcessArg(argv[i], world, statsPeriod, argv[0]);
			// run
			Date start = new Date();
			int totalFrames = 0;
			int round = 1;
			System.out.println("\n#round " + round);
			world.AddSeeds();
			world.running = true;
			do {
				if (statsPeriod != 0 && world.CurrentFrame() % statsPeriod == 0)
					PrintStatistics(world);
				world.SimulateOneFrame();
				if (world.RoundOver()) {
					PrintStatistics(world);
					totalFrames += world.CurrentFrame();
					world.EndRound();
					if (world.tournament)
						System.out.println("#round " + ++round);
				}
			} while (world.running);
			// print final statistics
			System.out
					.println("#results: \nscore\terror\tsurvival\tearly-death\tlate-death\tearly-score\tfraction\tkills\trounds\tname");
			List<Side> sides = world.Sides();
			for (int i = 0; i < sides.size(); ++i) {
				Side s = sides.get(i);
				System.out.println(StringUtilities.toPercentString(s
						.TournamentScores().BiomassFraction(), 0)
						+ "\t"
						+ StringUtilities.toPercentString(s.TournamentScores()
								.BiomassFractionError(), 0)
						+ "\t"
						+ StringUtilities.toPercentString(s.TournamentScores()
								.SurvivalNotSterile(), 0)
						+ "\t\t"
						+ StringUtilities.toPercentString(s.TournamentScores()
								.EarlyDeathRate(), 0)
						+ "\t\t"
						+ StringUtilities.toPercentString(s.TournamentScores()
								.LateDeathRate(), 0)
						+ "\t\t"
						+ StringUtilities.toPercentString(s.TournamentScores()
								.EarlyBiomassFraction(), 0)
						+ "\t\t"
						+ StringUtilities.toPercentString(s.TournamentScores()
								.SurvivalBiomassFraction(), 0)
						+ "\t\t"
						+ StringUtilities.toPercentString(s.TournamentScores()
								.KilledFraction(), 0)
						+ "\t"
						+ s.TournamentScores().rounds + "\t" + s.Name() + "\n");
			}
			System.out.println("#total\t\t"
					+ StringUtilities.toPercentString(world.TournamentScores()
							.SurvivalNotSterile(), 0)
					+ "\t\t"
					+ StringUtilities.toPercentString(world.TournamentScores()
							.EarlyDeathRate(), 0)
					+ "\t\t"
					+ StringUtilities.toPercentString(world.TournamentScores()
							.LateDeathRate(), 0) + "\t\t\t\t\t\t\t"
					+ world.TournamentScores().rounds + "\n#end");
			Date end = new Date();
			long dt = (end.getTime() - start.getTime()) / 1000;
			System.err.println(totalFrames + " frames");
			if (dt != 0)
				System.err.println(" in " + dt + " s (" + (totalFrames / dt)
						+ " fps)");
			if (dumpHtml)
				world.DumpTournamentScores(dumpHtml);
			// clean up
			// if ( SoundActive() ) CleanupSound();
			System.exit(0);
		} catch (GBError err) {
			GBError.FatalError("Uncaught GBError: " + err.toString());
		} catch (Exception e) {
			e.printStackTrace(System.out);
			GBError.FatalError("Uncaught java exception: " + e.toString()
					+ "\nTrace:\n" + e.getStackTrace());
		}
		System.exit(1);
	}

	static void ProcessArg(String arg, GBWorld world, int statsPeriod,
			String name) {
		try {
			if ('-' == arg.charAt(0)) {
				int dimension;
				switch (arg.charAt(1)) {
				case 't':
					world.tournament = true;
					world.tournamentLength = StringUtilities.parseInt(arg
							.substring(2));
					System.out.println("#tournament " + world.tournamentLength);
					break;
				case 'S':
					// SetupSound();
					// SetSoundActive(true);
					break;
				case 'l':
					world.timeLimit = StringUtilities
							.parseInt(arg.substring(2));
					break;
				case 'b':
					statsPeriod = StringUtilities.parseInt(arg.substring(2));
					break;
				case 'w':
					dimension = StringUtilities.parseInt(arg.substring(2));
					world.Resize(new FinePoint(
							GBObjectWorld.kBackgroundTileSize * dimension,
							world.Top()));
					break;
				case 'h':
					dimension = StringUtilities.parseInt(arg.substring(2));
					world.Resize(new FinePoint(world.Right(),
							GBObjectWorld.kBackgroundTileSize * dimension));
					break;
				case 's':
					world.seedLimit = StringUtilities
							.parseInt(arg.substring(2));
					break;
				case 'H':
					dumpHtml = true;
					break;
				default:
					DieWithUsage(name);
				}
			} else {
				Side side = SideReader.Load(arg);
				if (side != null) {
					world.AddSide(side);
					System.out.print("#side " + side.Name());
					for (int i = 1; i <= side.CountTypes(); ++i) {
						RobotType type = side.GetType(i);
						System.out.print("\t#type " + type.name + "\tcost "
								+ Math.round(type.Cost()) + "\tmass "
								+ Math.round(type.Mass() * 10) / 10);
					}
					System.out.println("");
				}
			}
		} catch (Exception e) {
			DieWithUsage(name);
		}
	}

	static void DieWithUsage(String name) {
		System.err
				.println("Usage: "
						+ name
						+ " [-t100] [-l4500] sides...\n"
						+ "Options (example numbers are defaults):\n"
						+ "  -t1     run a 1-round tournament\n"
						+ "  -l18000 limit rounds to 18000 frames\n"
						+ "  -b500   print biomass totals every 500 frames\n"
						+ "  -w10    world is 10 tiles wide\n"
						+ "  -h10    world is 10 tiles high\n"
						+ "  -s10    10 sides per round\n"
						+ "  -H      dump scores in HTML\n"
						+ "Grobots is free software distributed under the GNU General\n"
						+ "Public License.");
		System.exit(1);
	}

	public static void PrintStatistics(GBWorld world) {
		System.out.print(world.CurrentFrame());
		List<Side> sides = world.Sides();
		for (int i = 0; i < sides.size(); ++i)
			System.out.print("\t" + sides.get(i).Scores().Biomass());
		System.out.print("\ttotal " + world.RobotValue() + "\n");
	}

}
