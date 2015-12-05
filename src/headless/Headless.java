/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package headless;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import sides.RobotType;
import sides.Side;
import sides.SideReader;
import simulation.GBGame;
import simulation.GBObjectWorld;
import support.FinePoint;
import support.StringUtilities;
import exception.GBError;

public class Headless {
	// GBHeadless.cpp
	// main for command-line Grobots.
	// Grobots (c) 2002-2004 Devon and Warren Schudy
	// Distributed under the GNU General Public License.

	static boolean dumpHtml = false;
	int statsPeriod = 500;
	GBGame game;

	public Headless() {
		game = new GBGame();
		statsPeriod = 500;
	}

	public void run() {
		Date start = new Date();
		int totalFrames = 0;
		int round = 1;
		System.out.println("\n#round " + round);
		game.addSeeds();
		game.running = true;
		do {
			if (statsPeriod != 0 && game.currentFrame() % statsPeriod == 0)
				printStatistics(game);
			game.advanceFrame();
			if (game.roundOver()) {
				printStatistics(game);
				totalFrames += game.currentFrame();
				game.EndRound();
				if (game.tournament)
					System.out.println("#round " + ++round);
			}
		} while (game.running);
		System.out
				.println("#results: \nscore\terror\tsurvival\tearly-death\tlate-death\tearly-score\tfraction\tkills\trounds\tname");
		List<Side> sides = game.sides;
		for (int i = 0; i < sides.size(); ++i) {
			Side s = sides.get(i);
			System.out.println(StringUtilities.toPercentString(s
					.getTournamentScores().getBiomassFraction(), 0)
					+ "\t"
					+ StringUtilities.toPercentString(s.getTournamentScores()
							.getBiomassFractionError(), 0)
					+ "\t"
					+ StringUtilities.toPercentString(s.getTournamentScores()
							.getSurvivalNotSterile(), 0)
					+ "\t\t"
					+ StringUtilities.toPercentString(s.getTournamentScores()
							.EarlyDeathRate(), 0)
					+ "\t\t"
					+ StringUtilities.toPercentString(s.getTournamentScores()
							.getLateDeathRate(), 0)
					+ "\t\t"
					+ StringUtilities.toPercentString(s.getTournamentScores()
							.getEarlyBiomassFraction(), 0)
					+ "\t\t"
					+ StringUtilities.toPercentString(s.getTournamentScores()
							.getSurvivalBiomassFraction(), 0)
					+ "\t\t"
					+ StringUtilities.toPercentString(s.getTournamentScores()
							.getKilledFraction(), 0)
					+ "\t"
					+ s.getTournamentScores().rounds + "\t" + s.getName() + "\n");
		}
		System.out.println("#total\t\t"
				+ StringUtilities.toPercentString(game.getTournamentScores()
						.getSurvivalNotSterile(), 0)
				+ "\t\t"
				+ StringUtilities.toPercentString(game.getTournamentScores()
						.EarlyDeathRate(), 0)
				+ "\t\t"
				+ StringUtilities.toPercentString(game.getTournamentScores()
						.getLateDeathRate(), 0) + "\t\t\t\t\t\t\t"
				+ game.getTournamentScores().rounds + "\n#end");
		Date end = new Date();
		long dt = (end.getTime() - start.getTime()) / 1000;
		System.out.println(totalFrames + " frames");
		if (dt != 0)
			System.out.println(" in " + dt + " s (" + (totalFrames / dt)
					+ " fps)");
		if (dumpHtml)
			try {
				game.dumpTournamentScores(dumpHtml);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}

	public static void main(String[] argv) {
		if (argv.length < 2)
			dieWithUsage("Not enough arguments");
		try {
			Headless sim = new Headless();
			// get args
			for (int i = 0; i < argv.length; ++i)
				processArg(argv[i], sim, argv[0]);
			// run
			sim.run();
			// print final statistics
			// clean up
			// if ( SoundActive() ) CleanupSound();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace(System.out);
			GBError.FatalError("Uncaught exception: " + e.getMessage()
					+ "\nTrace:\n" + Arrays.toString(e.getStackTrace()));
		}
		System.exit(1);
	}

	static void processArg(String arg, Headless sim, String name) {
		try {
			if ('-' == arg.charAt(0)) {
				int dimension;
				switch (arg.charAt(1)) {
				case 't':
					sim.game.tournament = true;
					sim.game.tournamentLength = StringUtilities.parseInt(arg
							.substring(2));
					System.out.println("#tournament "
							+ sim.game.tournamentLength);
					break;
				case 'S':
					// SetupSound();
					// SetSoundActive(true);
					break;
				case 'l':
					sim.game.timeLimit = StringUtilities.parseInt(arg
							.substring(2));
					break;
				case 'b':
					sim.statsPeriod = StringUtilities
							.parseInt(arg.substring(2));
					break;
				case 'w':
					dimension = StringUtilities.parseInt(arg.substring(2));
					sim.game.resize(new FinePoint(
							GBObjectWorld.kBackgroundTileSize * dimension,
							sim.game.getWorld().getTop()));
					break;
				case 'h':
					dimension = StringUtilities.parseInt(arg.substring(2));
					sim.game.resize(new FinePoint(sim.game.getWorld().getRight(),
							GBObjectWorld.kBackgroundTileSize * dimension));
					break;
				case 's':
					sim.game.seedLimit = StringUtilities.parseInt(arg
							.substring(2));
					break;
				case 'H':
					dumpHtml = true;
					break;
				default:
					dieWithUsage(name);
				}
			} else {
				Side side = SideReader.loadFromFile(arg);
				if (side != null) {
					sim.game.addSide(side);
					System.out.print("#side " + side.getName());
					for (int i = 1; i <= side.getTypeCount(); ++i) {
						RobotType type = side.getRobotType(i);
						System.out.print("\t#type " + type.name + "\tcost "
								+ Math.round(type.getCost()) + "\tmass "
								+ Math.round(type.getMass() * 10) / 10);
					}
					System.out.println("");
				}
			}
		} catch (Exception e) {
			dieWithUsage(name);
		}
	}

	static void dieWithUsage(String name) {
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

	public static void printStatistics(GBGame world) {
		System.out.print(world.currentFrame());
		List<Side> sides = world.sides;
		for (int i = 0; i < sides.size(); ++i)
			System.out.print("\t" + sides.get(i).getScores().getBiomass());
		System.out.print("\ttotal " + world.getRobotValue() + "\n");
	}

}
